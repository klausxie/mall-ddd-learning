package cn.mklaus.app.configuration;

import cn.mklaus.app.common.exception.ErrorCodeException;
import cn.mklaus.app.common.exception.MallException;
import cn.mklaus.app.common.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // private final I18nMessageProvider i18nMessageProvider;

    @ExceptionHandler(MallException.class)
    public Response<Void> handleMallException(MallException e) {
        log.warn("Mall exception catch: {}", e.getMessage());
        return Response.error(e.getMessage());
    }

    @ExceptionHandler(ErrorCodeException.class)
    public Response<Void> handleErrorCodeException(ErrorCodeException e) {
        log.warn("ErrorCode exception catch: 【{}】-> {}", e.getErrorCode().getCode(), e.getMessage());
        // 国际化处理 String message = i18nMessageProvider.getMessage(e.getErrorCode(), e.getArgs());
        return Response.error(e.getErrorCode().getCode(), e.getMessage());
    }

}
