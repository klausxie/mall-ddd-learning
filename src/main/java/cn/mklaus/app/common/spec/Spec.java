package cn.mklaus.app.common.spec;

import cn.mklaus.app.common.exception.Violation;

import java.util.Optional;

/**
 * 规格（Specification）：把一条业务约束建模成具名、可单独测试的对象。
 *
 * <p>
 * 与 {@link java.util.function.Predicate} 的区别在于：不满足时会给出「违规原因」，
 * 因此可以直接驱动错误码返回（见 {@link Specs#assertSatisfied}）。
 *
 * <p>
 * 只保留 {@link #violation(Object)} 一个方法：模板里没有任何规则需要把多条约束按业务拼装，
 * 与其预置一套没人用的组合子（还会让人误以为"规格必须组合着写"），不如需要时再加。
 *
 * <p>
 * 使用约定见 ARCHITECTURE.md：规格放在 {@code domain/<业务>/spec} 下，只表达纯业务规则、
 * 保持无状态、不访问存储；需要查库的规则留在 {@code XxxValidator}。
 *
 * @author klaus
 * @since 2026/9/28
 */
@FunctionalInterface
public interface Spec<T> {

    /**
     * 满足约束返回 empty，否则返回违规原因。
     */
    Optional<Violation> violation(T t);

}
