package cn.mklaus.app;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 护栏自检：验证"规范的来源"没有被为了通过检查而改弱，与具体 harness / 钩子无关。
 *
 * <p>
 * 关键是**行为式而非文本式**。文本式（查配置文件里有没有那些规则文字）实测有两种绕过：
 * 把规则整段删掉、只把匹配字面量留在注释里；或加一个 {@code <SuppressionFilter>} 全量压制
 * （配置一字未改，规则全失效）。所以这里把 {@code resources/guardrails/bad-source.txt}
 * 复制成 .java 交给 Checkstyle，要求它逐条报出禁令；再拿一份合法代码要求零报告。
 *
 * <p>
 * 已知边界：凭证与门槛那两条仍是文本级，只覆盖本仓库已知的形态。
 *
 * @author klaus
 * @since 2026/9/29
 */
class GuardrailsTest {

    private static final Path CHECKSTYLE_CONFIG = Path.of("config/checkstyle.xml");

    /**
     * 故意违规的样本必须报出这些信息（各对应 CLAUDE.md 的一条禁令）。
     *
     * <p>
     * 故意用拼接而非完整字面量：checkstyle 按文本匹配，源码里写出完整名字会"自己违规自己"。
     */
    private static final List<String> EXPECTED_VIOLATIONS = List.of(
        "禁止 " + "System." + "out / " + "System." + "err",
        "禁止 " + "printStackTrace()",
        "禁止 " + "@" + "Autowired 注入",
        "禁止 " + "new " + "Date()",
        "只允许 GET / POST",
        "分页参数必须使用 curPage");

    @Test
    void checkstyleShouldRejectBadCodeAndAcceptCleanCode() throws Exception {
        List<String> messages = checkstyleMessages("/guardrails/bad-source.txt");
        for (String expected : EXPECTED_VIOLATIONS) {
            assertTrue(messages.stream().anyMatch(message -> message.contains(expected)),
                () -> "故意违规的代码没被报出「" + expected + "」：这条规则已失效"
                    + "（被删除、被 Suppression 压制、或 severity 不再是 error）。实际报出：" + messages);
        }

        List<String> clean = checkstyleMessages("/guardrails/clean-source.txt");
        assertTrue(clean.isEmpty(), () -> "一份完全合法的代码被报了错，说明规则配置本身出了问题：" + clean);
    }

    @Test
    void buildShouldStillRunTheChecks() {
        String pom = read("pom.xml");
        for (String artifact : List.of("spotless-maven-plugin", "maven-checkstyle-plugin", "jacoco-maven-plugin",
            "archunit")) {
            assertContains(pom, "<artifactId>" + artifact + "</artifactId>", artifact + " 从 pom.xml 里消失了");
        }
        assertContains(pom, "<configLocation>config/checkstyle.xml</configLocation>",
            "Checkstyle 不再指向仓库里的 config/checkstyle.xml");
        assertContains(pom, "config/eclipse-formatter.xml", "Spotless 不再指向仓库里的 eclipse-formatter.xml");
        assertContains(pom, "<violationSeverity>error</violationSeverity>",
            "violationSeverity 不是 error：Checkstyle 违规不再让构建失败");
        assertContains(pom, "<append>false</append>",
            "jacoco 的 append 被打开了：门槛会被跨构建的历史数据喂饱（实测过）");
        assertFalse(pom.contains("<skip>true</skip>"), "pom.xml 里出现了 <skip>true</skip>：本地检查会被静默跳过");

        // 比数值而不是比字面量：调高门槛（收紧）不该被误判成"调低"
        assertThresholdAtLeast(pom, "jacoco.domain.line.coverage.min", 0.70, "domain 覆盖率地板");
        assertThresholdAtLeast(pom, "jacoco.line.coverage.min", 0.80, "全局覆盖率门槛");

        String workflow = read(".github/workflows/verify.yml");
        assertContains(workflow, "-Pcoverage-check", "CI 不再跑覆盖率门槛");
        for (String flag : List.of("-Dspotless.check.skip=false", "-Dcheckstyle.skip=false")) {
            assertContains(workflow, flag,
                "CI 缺少防跳过参数 " + flag + "：.mvn/maven.config 里的 skip 就没人压得住了（实测过）");
        }
    }

    @Test
    void credentialsShouldNotBeCommittableOrHardcoded() {
        assertContains(read(".gitignore"), "application-local.yaml",
            ".gitignore 不再忽略 application-local.yaml：本地凭证文件会被提交进仓库");

        for (String config : List.of("src/main/resources/application.yaml",
            "src/main/resources/application-local.yaml.example")) {
            for (String line : read(config).split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("password:")) {
                    continue;
                }
                String value = trimmed.substring("password:".length()).trim();
                assertTrue(value.startsWith("${") || value.startsWith("<") || value.startsWith("'<")
                    || value.startsWith("\"<"),
                    config + " 里的 password 是字面量而不是占位符（" + value.length()
                        + " 字符）。凭证一旦提交进仓库就删不掉、只能换——请改成 ${APP_DB_PASSWORD:} 这类占位符");
            }
        }
    }

    /**
     * 把样本复制成 {@code .java} 交给 Checkstyle（与 checkstyle 插件同一份配置），返回它报出的信息。
     */
    private static List<String> checkstyleMessages(String resource) throws Exception {
        Path tempDir = Files.createTempDirectory("guardrails-probe");
        Path file = tempDir.resolve("GuardrailsProbe.java");
        Files.writeString(file, readResource(resource), StandardCharsets.UTF_8);

        ByteArrayOutputStream log = new ByteArrayOutputStream();
        Checker checker = new Checker();
        try {
            checker.setModuleClassLoader(Checker.class.getClassLoader());
            // 与插件一致：相对路径的模块（如 SuppressionFilter 的抑制文件）要能解析
            checker.setBasedir(Path.of("").toAbsolutePath().toString());
            checker.addListener(new DefaultLogger(log, AutomaticBean.OutputStreamOptions.NONE));
            checker.configure(ConfigurationLoader.loadConfiguration(
                CHECKSTYLE_CONFIG.toAbsolutePath().toString(), new PropertiesExpander(System.getProperties())));
            checker.process(List.of(file.toFile()));
        } finally {
            checker.destroy();
            deleteRecursively(tempDir);
        }
        return log.toString(StandardCharsets.UTF_8).lines().filter(line -> line.contains("[ERROR]")).toList();
    }

    private static void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static String readResource(String name) throws IOException {
        try (InputStream in = GuardrailsTest.class.getResourceAsStream(name)) {
            assertTrue(in != null, "找不到测试资源 " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertThresholdAtLeast(String pom, String property, double floor, String what) {
        Matcher matcher = Pattern.compile("<" + property + ">([0-9.]+)</" + property + ">").matcher(pom);
        assertTrue(matcher.find(), () -> what + "（" + property + "）在 pom.xml 里找不到");
        double actual = Double.parseDouble(matcher.group(1));
        assertTrue(actual >= floor, () -> what + "被调低了：" + property + " = " + actual + " < " + floor);
    }

    private static String read(String relativePath) {
        Path path = Path.of(relativePath);
        assertTrue(Files.exists(path), () -> "护栏自检找不到 " + relativePath
            + "：请在项目根目录下用 ./mvnw verify 运行（当前目录 " + Path.of("").toAbsolutePath() + "）");
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取 " + relativePath + " 失败", e);
        }
    }

    private static void assertContains(String content, String needle, String message) {
        assertTrue(content.contains(needle), () -> message + "；未找到：" + needle);
    }

}
