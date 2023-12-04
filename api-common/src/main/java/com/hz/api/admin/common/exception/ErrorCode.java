package com.hz.api.admin.common.exception;

public enum ErrorCode {

    操作成功("00000", "success"),
    操作失败("-1", "fail");

    public String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String code;

    ErrorCode(String code ,String message){
        this.code = code;
        this.message = message;
    }
}
