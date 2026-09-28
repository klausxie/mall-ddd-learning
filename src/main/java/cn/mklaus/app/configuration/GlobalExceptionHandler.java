package cn.mklaus.app.configuration;

import cn.mklaus.app.common.exception.BaseException;
import cn.mklaus.app.common.exception.CommonErrorCode;
import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.common.model.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：保证任何失败都返回统一的 {@link Response} 形状（HTTP 状态恒为 200，靠 body.code 区分）。
 *
 * <p>
 * 覆盖范围：业务错误码异常、领域自定义异常、{@code @Valid} 参数校验失败、
 * 其它运行时异常，以及兜底。兜底只在日志里保留堆栈，不把内部细节回给客户端。
 *
 * @author klausxie
 * @since 2023/11/4
 */
@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorCodeException.class)
    public Response<Void> handleErrorCodeException(ErrorCodeException e) {
        log.warn("ErrorCode exception catch: 【{}】-> {}", e.getErrorCode().getCode(), e.getMessage());
        return Response.error(e.getErrorCode().getCode(), e.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public Response<Void> handleBaseException(BaseException e) {
        log.warn("Base exception catch: {}", e.getMessage());
        return Response.error(e.getMessage());
    }

    /**
     * {@code @Valid} 参数校验失败：取第一条字段错误的信息。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse(CommonErrorCode.PARAMETER_ILLEGAL.getTemplate());
        log.warn("参数校验失败: {}", message);
        return Response.error(CommonErrorCode.PARAMETER_ILLEGAL.getCode(), message);
    }

    /**
     * Assert.hasText / isTrue / notNull 抛这个，消息本身就是给用户看的。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument catch: {}", e.getMessage());
        return Response.error(CommonErrorCode.PARAMETER_ILLEGAL.getCode(), e.getMessage());
    }

    /**
     * Assert.state 抛这个，表示业务状态不满足。
     */
    @ExceptionHandler(IllegalStateException.class)
    public Response<Void> handleIllegalStateException(IllegalStateException e) {
        log.warn("Illegal state catch: {}", e.getMessage());
        return Response.error(CommonErrorCode.STATE_ILLEGAL.getCode(), e.getMessage());
    }

    /**
     * 兜底：记录完整堆栈，只回统一文案。
     */
    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception e) {
        log.error("Unexpected exception catch", e);
        return Response.error(CommonErrorCode.SYSTEM_ERROR.getCode(), CommonErrorCode.SYSTEM_ERROR.getTemplate());
    }

}
