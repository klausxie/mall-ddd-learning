package cn.mklaus.app.common.spec;

import cn.mklaus.app.common.exception.ErrorCodeException;

/**
 * 规格的断言入口：把违规原因转成项目统一的错误码异常。
 *
 * <p>
 * 放在 {@code common.spec} 而不是 {@code common.exception}，是为了让依赖方向保持单向：
 * 规格依赖错误码，错误码不反过来依赖规格。
 *
 * @author klaus
 * @since 2026/9/28
 */
public final class Specs {

    private Specs() {
    }

    /**
     * 规格不满足时抛出 {@link ErrorCodeException}，错误码与错误信息来自规格自身。
     */
    public static <T> void assertSatisfied(Spec<? super T> spec, T target) {
        spec.violation(target).ifPresent(violation -> {
            throw new ErrorCodeException(violation.getErrorCode(), violation.getArgs());
        });
    }

}
