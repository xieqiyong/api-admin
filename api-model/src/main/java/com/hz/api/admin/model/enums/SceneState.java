package com.hz.api.admin.model.enums;

public enum SceneState {

    NEW(1, "未开始"),
    RUNNING(2, "进行中"),
    EXCEPTION(3, "执行一次"),
    END(4, "结束");

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private int code;

    SceneState(int code, String message){
        this.code = code;
        this.message = message;
    }
}
