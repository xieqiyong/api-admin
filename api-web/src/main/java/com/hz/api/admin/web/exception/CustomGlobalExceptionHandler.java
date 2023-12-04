package com.hz.api.admin.web.exception;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义全局异常处理
 * 1、异常日志
 * 2、请求出参处理
 *
 * @author: junhui.si
 * @date: 2021-03-02 16:49
 **/
@RestControllerAdvice
@Slf4j
public class CustomGlobalExceptionHandler {

    @ExceptionHandler(value = Throwable.class)
    public ResultInfo<Void> exceptionHandler(Throwable e) {
        log.error("发生异常: {}", e);
        return ResultInfo.buildFail();
    }

    /**
     * 处理远程调用异常
     *
     * @param req
     * @param ex
     * @return
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public ResultInfo exceptionHandler(HttpServletRequest req, Exception ex) {
        return ResultInfo.buildFail(ex.getMessage());
    }

    /**
     * 参数校验
     * @param e
     * @return message
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultInfo<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        List<String> messages = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        return ResultInfo.buildFail(messages.get(0));
    }
}
