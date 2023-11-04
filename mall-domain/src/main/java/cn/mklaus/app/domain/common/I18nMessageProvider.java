package cn.mklaus.app.domain.common;

/**
 * @author klausxie
 * @since 2023/11/4
 */
public interface I18nMessageProvider {

    /**
     * 构建国际化信息
     *
     * @param i18nCode 国际化代码
     * @param args 参数列表
     * @return 国际化信息
     */
    String buildMessage(String i18nCode, Object[] args);

}
