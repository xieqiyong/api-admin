package com.hz.api.admin.common.Result;

import com.hz.api.admin.common.exception.ErrorCode;

import java.io.Serializable;

public class ResultInfo<T> implements Serializable {

    private static final long serialVersionUID = -5934587881162778794L;

    public String message;

    public String code;

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T data;

    public ResultInfo(String message){
        this.message = message;
    }

    public ResultInfo(){

    }

    public static <T> ResultInfo<T> success(String message){
        ResultInfo resultInfo = new ResultInfo(message);
        resultInfo.setCode(ErrorCode.操作成功.getCode());
        return resultInfo;
    }

    public static <T> ResultInfo<T> success(T data){
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(ErrorCode.操作成功.getCode());
        resultInfo.setData(data);
        return resultInfo;
    }

    public static <T> ResultInfo<T> success(){
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(ErrorCode.操作成功.getCode());
        return resultInfo;
    }

    public static <T> ResultInfo<T> buildFail(String message){
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(ErrorCode.操作失败.getCode());
        resultInfo.setMessage(message);

        return resultInfo;
    }

    public static <T> ResultInfo<T> buildFail(){
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(ErrorCode.操作失败.getCode());
        return resultInfo;
    }
}
