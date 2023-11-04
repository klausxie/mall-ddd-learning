package cn.mklaus.app.common.exception;

/**
 * 错误码接口
 *
 * @author klausxie
 * @since 2023/11/4
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    int getCode();

    /**
     * 获取错误信息模版
     *
     * @return 错误信息模版
     */
    String getTemplate();

}
