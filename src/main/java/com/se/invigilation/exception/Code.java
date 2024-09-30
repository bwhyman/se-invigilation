package com.se.invigilation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Code {
    LOGIN_ERROR(Code.ERROR, "用户名密码错误"),
    BAD_REQUEST(Code.ERROR, "请求错误"),
    UNAUTHORIZED(401, "未登录"),
    TOKEN_EXPIRED(401, "过期请重新登录"),
    FORBIDDEN(403, "无权限"),
    MESSAGE_ERROR(405, "短信发送错误"),
    LOGIN_TOKEN_ERROR(401, "令牌错误请重新登录");
    public static final int ERROR = 400;
    private final int code;
    private final String message;
}
