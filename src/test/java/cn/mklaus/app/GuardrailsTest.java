package cn.mklaus.app;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 护栏自检：验证"规范的来源"本身没有被为了通过检查而改弱。
 *
 * <p>
 * 为什么需要它：`checkstyle.xml`、`pom.xml` 里的检查插件、CI 配置，正是"检查不通过时最容易顺手改掉"的
 * 几样东西。Claude Code 的 PreToolUse 钩子只能拦住那一种工具、那一类编辑（用 Bash 改、换一个 harness
 * 就失效）。本测试跑在 `./mvnw verify` 里，与 CI 用的是同一条命令，因此是唯一 **harness 无关** 的兜底。
 *
 * <p>
 * 关于 Checkstyle 的检查方式：**不看配置文件里有没有那些规则文本，而是真的跑一遍规则**。
 * 文本级检查有两个已实测的绕过手法——把规则整段删掉、只把它的匹配字面量留在注释里；
 * 或者加一个 {@code <SuppressionFilter>} 把全部规则压掉（此时配置文本一字未改，规则却全失效）。
 * 所以这里把 {@code resources/guardrails/bad-source.txt} 复制成 .java 交给 Checkstyle，
 * 要求它逐条报出禁令；再拿一份合法代码要求零报告。
 *
 * <p>
 * 已知边界：`credentialsShouldNotBeCommittableOrHardcoded` 是文本级的，只覆盖本仓库已知的两种形态；
 * 门槛检查比对的是数值（≥ 0.70 / ≥ 0.80），所以"收紧门槛"不会被误拦。
 *
 * @author klaus
 * @since 2026/9/29
 */
class GuardrailsTest {

    private static final Path CHECKSTYLE_CONFIG = Path.of("config/checkstyle.xml");

    /**
     * 故意违规的样本必须报出这些信息（各条对应 CLAUDE.md 的一条禁令）。
     *
     * <p>
     * 故意用拼接而非完整字面量：checkstyle 里有几条按文本匹配的规则，源码里一旦写出完整名字，
     * "规则清单自己"就会被判违规——ArchitectureTest 里同理。
     */
    private static final List<String> EXPECTED_VIOLATIONS = List.of(
        "禁止 " + "System." + "out / " + "System." + "err",
        "禁止 " + "printStackTrace()",
        "禁止 " + "@" + "Autowired 注入",
        "禁止 " + "new " + "Date()",
        "只允许 GET / POST",
        "分页参数必须使用 curPage");

    @Test
    void ruleFilesShouldBePresent() {
        assertTrue(Files.exists(CHECKSTYLE_CONFIG), "config/checkstyle.xml 不见了");
        assertTrue(Files.exists(Path.of("config/eclipse-formatter.xml")), "config/eclipse-formatter.xml 不见了");
    }

    @Test
    void checkstyleShouldRejectTheDeliberatelyBadSource() throws Exception {
        List<String> messages = checkstyleMessages(readResource("/guardrails/bad-source.txt"));

        for (String expected : EXPECTED_VIOLATIONS) {
            assertTrue(messages.stream().anyMatch(message -> message.contains(expected)),
                () -> "故意违规的代码没有被报出「" + expected + "」：这条规则已经失效"
                    + "（被删除、被 Suppression 压制、或 severity 不再是 error）。实际报出：" + messages);
        }
    }

    @Test
    void checkstyleShouldAcceptTheCleanSource() throws Exception {
        List<String> messages = checkstyleMessages(readResource("/guardrails/clean-source.txt"));

        assertTrue(messages.isEmpty(), () -> "一份完全合法的代码被报了错，说明规则配置本身出了问题：" + messages);
    }

    @Test
    void pomShouldStillRunTheChecks() {
        String pom = read("pom.xml");
        for (String artifact : List.of("spotless-maven-plugin", "maven-checkstyle-plugin", "jacoco-maven-plugin",
            "archunit")) {
            assertContains(pom, "<artifactId>" + artifact + "</artifactId>", artifact + " 从 pom.xml 里消失了");
        }
        assertContains(pom, "<configLocation>config/checkstyle.xml</configLocation>",
            "Checkstyle 不再指向仓库里的 config/checkstyle.xml");
        assertContains(pom, "config/eclipse-formatter.xml", "Spotless 不再指向仓库里的 eclipse-formatter.xml");
        assertContains(pom, "<violationSeverity>error</violationSeverity>", "Checkstyle 的 violationSeverity 不是 error");

        int checkGoals = countOf(pom, "<goal>check</goal>");
        assertTrue(checkGoals >= 2, "spotless / checkstyle 的 check 目标少了（当前只有 " + checkGoals + " 个）");
        assertFalse(pom.contains("<skip>true</skip>"), "pom.xml 里出现了 <skip>true</skip>：检查会被静默跳过");
    }

    @Test
    void coverageThresholdsShouldNotBeLowered() {
        String pom = read("pom.xml");
        // 比数值而不是比字面量：调高门槛（收紧）不该被误判成"调低"
        assertThresholdAtLeast(pom, "jacoco.domain.line.coverage.min", 0.70, "domain 覆盖率地板");
        assertThresholdAtLeast(pom, "jacoco.line.coverage.min", 0.80, "全局覆盖率门槛");
        assertContains(pom, "<append>false</append>",
            "jacoco 的 append 被打开了：jacoco.exec 会跨构建累加，覆盖率门槛会被历史数据喂饱（实测过）");
    }

    @Test
    void credentialsShouldNotBeCommittableOrHardcoded() {
        assertContains(read(".gitignore"), "application-local.yaml",
            ".gitignore 不再忽略 application-local.yaml：本地凭证文件会被提交进仓库");

        assertPasswordIsPlaceholder("src/main/resources/application.yaml");
        assertPasswordIsPlaceholder("src/main/resources/application-local.yaml.example");
    }

    @Test
    void ciShouldNotBeWeakened() {
        String workflow = read(".github/workflows/verify.yml");
        assertContains(workflow, "-Pcoverage-check", "CI 不再跑覆盖率门槛");
        assertContains(workflow, "MALL_DB_PASSWORD", "CI 不再跑真库测试");
        for (String flag : List.of("-Dspotless.check.skip=false", "-Dcheckstyle.skip=false")) {
            assertContains(workflow, flag,
                "CI 缺少防跳过参数 " + flag + "：pom.xml 或 .mvn/maven.config 里一旦出现 skip 就没人拦得住了");
        }
    }

    /**
     * 把样本复制成 {@code .java} 交给 Checkstyle（与插件同一份配置），返回它报出的信息。
     */
    private static List<String> checkstyleMessages(String source) throws Exception {
        Path tempDir = Files.createTempDirectory("guardrails-probe");
        Path file = tempDir.resolve("GuardrailsProbe.java");
        Files.writeString(file, source, StandardCharsets.UTF_8);

        ViolationCollector collector = new ViolationCollector();
        Checker checker = new Checker();
        try {
            checker.setModuleClassLoader(Checker.class.getClassLoader());
            // 与 checkstyle 插件一致：相对路径的模块（如 SuppressionFilter 的抑制文件）要能解析
            checker.setBasedir(Path.of("").toAbsolutePath().toString());
            checker.addListener(collector);
            checker.configure(loadCheckstyleConfig());
            checker.process(List.of(file.toFile()));
        } finally {
            checker.destroy();
            deleteRecursively(tempDir);
        }
        return collector.messages;
    }

    private static Configuration loadCheckstyleConfig() throws Exception {
        assertTrue(Files.exists(CHECKSTYLE_CONFIG), "找不到 " + CHECKSTYLE_CONFIG.toAbsolutePath());
        return ConfigurationLoader.loadConfiguration(CHECKSTYLE_CONFIG.toAbsolutePath().toString(),
            new PropertiesExpander(System.getProperties()));
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

    /**
     * 数据源密码必须是占位符（{@code ${...}} 或 {@code <...>}），不能是字面量。
     */
    private static void assertPasswordIsPlaceholder(String relativePath) {
        for (String line : read(relativePath).split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("password:")) {
                continue;
            }
            String value = trimmed.substring("password:".length()).trim();
            assertTrue(value.startsWith("${") || value.startsWith("<") || value.startsWith("'<")
                || value.startsWith("\"<"),
                relativePath + " 里的 password 是字面量而不是占位符（" + value.length()
                    + " 字符）。凭证一旦提交进仓库，改文件是删不掉的，只能换凭证 + 清历史——"
                    + "请改成 ${MALL_DB_PASSWORD:} 这类占位符");
        }
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

    private static int countOf(String content, String needle) {
        int count = 0;
        for (int index = content.indexOf(needle); index >= 0; index = content.indexOf(needle,
            index + needle.length())) {
            count++;
        }
        return count;
    }

    /**
     * 收集 Checkstyle 报出的每一条信息。
     */
    private static final class ViolationCollector implements AuditListener {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void auditStarted(AuditEvent event) {
        }

        @Override
        public void auditFinished(AuditEvent event) {
        }

        @Override
        public void fileStarted(AuditEvent event) {
        }

        @Override
        public void fileFinished(AuditEvent event) {
        }

        @Override
        public void addError(AuditEvent event) {
            messages.add(String.valueOf(event.getMessage()));
        }

        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            messages.add("解析失败: " + throwable);
        }
    }

}
