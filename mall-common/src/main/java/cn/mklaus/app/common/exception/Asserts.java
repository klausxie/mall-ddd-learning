package cn.mklaus.app.common.exception;

/**
 * @author klausxie
 * @since 2023/11/4
 */
public class Asserts {

    public static void state(boolean condition, ErrorCode errorCode, Object ...args) {
        if (!condition) {
            throw new ErrorCodeException(errorCode, args);
        }
    }

}
