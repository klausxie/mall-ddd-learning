package cn.mklaus.app;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 跨端契约对账：前端声明的每个接口路径，后端都要真的暴露（不需要数据库，也不需要起前端）。
 *
 * <p>
 * 为什么值得有：前后端同仓之后，"唯一契约"就是这些路径字符串。改接口名忘了改前端，
 * 以前只能等上线 404——而机器可以在**构建期**发现。这是跨端唯一能机器兜住的东西。
 *
 * <p>
 * 两边都从**真实文件**解析，所以新增接口会自动纳入：后端读 {@code web} 包下所有
 * {@code *Controller.java} 的 mapping 注解，前端读 {@code ../frontend/src/api/paths.ts} 的路径字面量。
 *
 * <p>
 * 只断言"前端 ⊆ 后端"：后端暴露而前端没用到的接口是正常的（给别的客户端用）。
 * 解析器自己也设了数量底线——否则哪天文件结构变了，这个测试会"看着还在、其实什么都没查"。
 */
class ApiContractTest {

    private static final Path BACKEND_SOURCE = Path.of("src/main/java");

    /** 前端那份"唯一路径清单"。相对 backend/ 的上一级。 */
    private static final Path FRONTEND_PATHS = Path.of("../frontend/src/api/paths.ts");

    /** 类级前缀，如 {@code @RequestMapping("address")}。 */
    private static final Pattern CLASS_MAPPING = Pattern.compile("@RequestMapping\\s*\\(\\s*[\"']([^\"']*)[\"']");

    /** 方法级路径，如 {@code @PostMapping("create")}。 */
    private static final Pattern METHOD_MAPPING = Pattern.compile(
        "@(?:Get|Post|Put|Delete|Request)Mapping\\s*\\(\\s*[\"']([^\"']*)[\"']");

    /** paths.ts 里的路径字面量：只要以 / 开头的字符串。 */
    private static final Pattern PATH_LITERAL = Pattern.compile("[\"'](/[A-Za-z0-9/_-]*)[\"']");

    /** 两边的数量底线，用来兜住"解析器悄悄失配"。 */
    private static final int MIN_BACKEND_PATHS = 5;

    private static final int MIN_FRONTEND_PATHS = 5;

    @Test
    void frontendPathsShouldExistInBackend() {
        Set<String> backend = backendPaths();
        Set<String> frontend = frontendPaths();

        assertTrue(backend.size() >= MIN_BACKEND_PATHS,
            "只从 Controller 解析出 " + backend.size() + " 个路径（预期至少 " + MIN_BACKEND_PATHS
                + "）：解析逻辑或目录结构变了，这个对账已经失效。当前解析到：" + backend);
        assertTrue(frontend.size() >= MIN_FRONTEND_PATHS,
            "只从 " + FRONTEND_PATHS + " 解析出 " + frontend.size() + " 个路径（预期至少 " + MIN_FRONTEND_PATHS
                + "）：路径清单的写法变了，这个对账已经失效。当前解析到：" + frontend);

        Set<String> missing = new TreeSet<>(frontend);
        missing.removeAll(backend);
        assertEquals(Set.of(), missing,
            "前端声明了后端并不存在的接口路径：" + missing + "\n后端实际暴露：" + backend
                + "\n改接口路径时，后端 Controller 与前端的 src/api/paths.ts 必须同时改。");
    }

    /** {@code web} 包下所有 Controller 的 mapping，拼成完整路径（类级前缀 + 方法级路径）。 */
    private static Set<String> backendPaths() {
        Set<String> paths = new LinkedHashSet<>();
        try (Stream<Path> files = Files.walk(BACKEND_SOURCE)) {
            for (Path file : files.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith("Controller.java"))
                .filter(path -> path.toString().replace('\\', '/').contains("/web/"))
                .sorted()
                .toList()) {
                collectControllerPaths(Files.readString(file, StandardCharsets.UTF_8), paths);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return paths;
    }

    private static void collectControllerPaths(String source, Set<String> paths) {
        // 类声明之前的部分才可能带类级 @RequestMapping
        int classStart = source.indexOf("public class ");
        String header = classStart < 0 ? "" : source.substring(0, classStart);
        String body = classStart < 0 ? source : source.substring(classStart);

        Matcher classMapping = CLASS_MAPPING.matcher(header);
        String prefix = classMapping.find() ? classMapping.group(1) : "";

        Matcher methodMapping = METHOD_MAPPING.matcher(body);
        while (methodMapping.find()) {
            paths.add(join(prefix, methodMapping.group(1)));
        }
    }

    /** 拼路径并归一化：`address` + `create` → `/address/create`；任一为空也能得到合理结果。 */
    private static String join(String prefix, String suffix) {
        String left = prefix == null ? "" : prefix.strip();
        String right = suffix == null ? "" : suffix.strip();
        left = left.replaceAll("^/+", "").replaceAll("/+$", "");
        right = right.replaceAll("^/+", "").replaceAll("/+$", "");
        return "/" + String.join("/", java.util.Arrays.stream(new String[] { left, right })
            .filter(part -> !part.isEmpty()).toList());
    }

    /** 前端 {@code paths.ts} 里声明的所有路径。 */
    private static Set<String> frontendPaths() {
        assertTrue(Files.isRegularFile(FRONTEND_PATHS),
            "找不到 " + FRONTEND_PATHS.toAbsolutePath()
                + "：前端的接口路径清单是跨端契约的一半，删掉它等于关掉这个对账。");
        String source;
        try {
            source = Files.readString(FRONTEND_PATHS, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Set<String> paths = new LinkedHashSet<>();
        Matcher literal = PATH_LITERAL.matcher(source);
        while (literal.find()) {
            paths.add(literal.group(1));
        }
        return paths;
    }

}
