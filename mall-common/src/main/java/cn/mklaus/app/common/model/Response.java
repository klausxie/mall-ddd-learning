package cn.mklaus.app.common.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author klausxie
 * @since 2023/11/4
 */
@Getter
@ToString
public class Response<T> implements Serializable {

    private static final String SUCCESS_MSG = "ok";
    private static final int SUCCESS_CODE = 0;
    private static final int ERROR_CODE = 1;

    private int code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    private Response() {
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isOk() {
        return code == SUCCESS_CODE;
    }

    public static <T> Response<T> ok() {
        Response<T> response = new Response<>();
        response.code = SUCCESS_CODE;
        response.message = SUCCESS_MSG;
        return response;
    }

    public static <T> Response<T> ok(T data) {
        Response<T> response = new Response<>();
        response.code = SUCCESS_CODE;
        response.message = SUCCESS_MSG;
        response.data = data;
        return response;
    }

    public static <T> Response<T> error(String errMsg) {
        return error(ERROR_CODE, errMsg);
    }

    public static <T> Response<T> error(int errCode, String errMsg) {
        Response<T> response = new Response<>();
        response.code = errCode;
        response.message = errMsg;
        return response;
    }
}