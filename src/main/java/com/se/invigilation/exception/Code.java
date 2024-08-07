package com.se.invigilation.exception;

import lombok.Getter;

@Getter
public enum Code {
    LOGIN_ERROR(401, "用户名密码错误"),
    BAD_REQUEST(400, "请求错误"),
    UNAUTHORIZED(401, "未登录"),
    TOKEN_EXPIRED(401, "过期请重新登录"),
    FORBIDDEN(403, "无权限"),
    MESSAGE_ERROR(405, "短信发送错误"),
    LOGIN_TOKEN_ERROR(401, "令牌错误请重新登录");
    public static final int ERROR = 400;
    private final int code;
    private final String message;

    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
