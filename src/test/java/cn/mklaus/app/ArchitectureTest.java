package cn.mklaus.app;

import cn.mklaus.app.common.exception.Asserts;
import cn.mklaus.app.common.spec.Spec;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 架构约束。这些规则是"项目结构规范"里唯一可以机器强制的部分：
 * 目录怎么摆很难自动判断，但"谁不许依赖谁"是可以的。
 *
 * <p>
 * 新增分层时，请同步更新 {@code ARCHITECTURE.md} 与本文件。
 *
 * @author klausxie
 */
class ArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("cn.mklaus.app");

    @Test
    void domainShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("cn.mklaus.app.domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("cn.mklaus.app.application..", "cn.mklaus.app.web..",
                "cn.mklaus.app.infrastructure..")
            .because("domain 是核心层，只能依赖 common，不得反向依赖上层");
        rule.check(CLASSES);
    }

    @Test
    void commonShouldNotDependOnBusinessPackages() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("cn.mklaus.app.common..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("cn.mklaus.app.domain..", "cn.mklaus.app.application..", "cn.mklaus.app.web..",
                "cn.mklaus.app.infrastructure..")
            .because("common 必须是零业务依赖的公共设施，否则会退化成垃圾场");
        rule.check(CLASSES);
    }

    @Test
    void webShouldNotBeDependedUpon() {
        ArchRule rule = noClasses()
            .that().resideOutsideOfPackage("cn.mklaus.app.web..")
            .should().dependOnClassesThat().resideInAPackage("cn.mklaus.app.web..")
            .because("web 是最外层，任何内部层都不得引用它");
        rule.check(CLASSES);
    }

    @Test
    void domainAndApplicationShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("cn.mklaus.app.domain..", "cn.mklaus.app.application..")
            .should().dependOnClassesThat().resideInAPackage("cn.mklaus.app.infrastructure..")
            .because("实现细节只能通过 domain 里的接口注入，不得直接依赖 infrastructure");
        rule.check(CLASSES);
    }

    @Test
    void noCyclesBetweenTopLevelPackages() {
        ArchRule rule = slices()
            .matching("cn.mklaus.app.(*)..")
            .should().beFreeOfCycles()
            .because("顶层包之间出现循环依赖意味着分层已经失效");
        rule.check(CLASSES);
    }

    @Test
    void mapperTypesShouldBeInterfaces() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Mapper")
            .should().beInterfaces()
            .because("MyBatis 映射器必须是接口，实现由 MyBatis 生成");
        rule.check(CLASSES);
    }

    @Test
    void restControllersShouldLiveInWebPackage() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("cn.mklaus.app.web..")
            .because("所有 HTTP 入口统一放在 web 包，便于统一鉴权与异常处理");
        rule.check(CLASSES);
    }

    @Test
    void specImplementationsShouldBeNamedAndPlacedAsSpecs() {
        ArchRule rule = classes()
            .that().implement(Spec.class)
            .should().haveSimpleNameEndingWith("Spec")
            .andShould().resideInAPackage("..spec..")
            .because("规格是具名、可组合、可单独测试的业务约束，统一放 <业务>/spec 包");
        rule.check(CLASSES);
    }

    /**
     * 禁止出现在对外请求对象里的分页参数名。
     *
     * <p>
     * 故意用拼接而不是完整字面量：checkstyle 里有几条按文本匹配的规则，
     * 这里一旦写出完整名字，"规则定义文件自己"就会被判违规。
     */
    private static final String FORBIDDEN_PAGING_PARAMETER_NAMES = "limit|offset|page" + "Number|page" + "Num|current"
        + "Page|page" + "Index";

    @Test
    void requestDtosShouldNotUseForbiddenPagingParameterNames() {
        ArchRule rule = noFields()
            .that().areDeclaredInClassesThat().resideInAPackage("..application..request..")
            .should().haveNameMatching(FORBIDDEN_PAGING_PARAMETER_NAMES)
            .because("对外分页参数只有 curPage / pageSize，禁止 limit / offset 及其它历史命名（见 CLAUDE.md）");
        rule.check(CLASSES);
    }

    @Test
    void applicationAndWebShouldNotCarryAssertions() {
        ArchRule noSpringAssert = noClasses()
            .that().resideInAnyPackage("cn.mklaus.app.application..", "cn.mklaus.app.web..")
            .should().dependOnClassesThat().areAssignableTo(Assert.class)
            .because("断言是领域校验手段：编排层只能调用 domain 的 Validator / Spec / 实体方法");
        noSpringAssert.check(CLASSES);

        ArchRule noAsserts = noClasses()
            .that().resideInAnyPackage("cn.mklaus.app.application..", "cn.mklaus.app.web..")
            .should().dependOnClassesThat().areAssignableTo(Asserts.class)
            .because("编排层需要报错时抛 ErrorCodeException，不要自己写断言");
        noAsserts.check(CLASSES);
    }

    @Test
    void mybatisShouldOnlyLeakIntoMapperInterfaces() {
        // 已知例外：本次重构把 MyBatis 映射器接口放在了 domain 包，并加了 @Mapper。
        // 这里把例外收窄到 "以 Mapper 结尾的接口"，其余 domain 类不得感知持久化实现。
        // 详见 ARCHITECTURE.md 的"已知偏差"一节。
        ArchRule rule = noClasses()
            .that().resideInAPackage("cn.mklaus.app.domain..")
            .and().haveSimpleNameNotEndingWith("Mapper")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.apache.ibatis..", "com.baomidou.mybatisplus..")
            .because("领域层不应感知持久化实现（Mapper 接口为已记录的例外）");
        rule.check(CLASSES);
    }

}
