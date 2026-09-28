package cn.mklaus.app.common.spec;

import cn.mklaus.app.common.exception.ErrorCode;
import cn.mklaus.app.common.exception.Violation;

import java.util.Optional;

/**
 * 规格（Specification）：把一条业务约束建模成具名、可组合、可单独测试的对象。
 *
 * <p>
 * 与 {@link java.util.function.Predicate} 的区别在于：不满足时会给出「违规原因」，
 * 因此可以直接驱动错误码返回（见 {@link Specs#assertSatisfied}）。
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

    /**
     * 约束是否满足。
     */
    default boolean isSatisfiedBy(T t) {
        return violation(t).isEmpty();
    }

    /**
     * 两者都满足才通过；不通过时返回第一条违规原因。
     */
    default Spec<T> and(Spec<T> next) {
        return t -> {
            Optional<Violation> first = violation(t);
            return first.isPresent() ? first : next.violation(t);
        };
    }

    /**
     * 满足其一即通过；两边都违反时才返回违规原因（取第一条）。
     */
    default Spec<T> or(Spec<T> next) {
        return t -> {
            Optional<Violation> first = violation(t);
            boolean bothViolated = first.isPresent() && next.violation(t).isPresent();
            return bothViolated ? first : Optional.empty();
        };
    }

    /**
     * 取反：内层满足时视为违规。违规原因无法从内层推导，由调用方指定。
     */
    default Spec<T> not(ErrorCode whenSatisfied) {
        return t -> violation(t).isPresent() ? Optional.empty() : Optional.of(Violation.of(whenSatisfied));
    }

}
