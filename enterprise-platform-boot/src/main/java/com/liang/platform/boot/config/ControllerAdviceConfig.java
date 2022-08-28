package com.liang.platform.boot.config;

import com.liang.platform.boot.constant.ResultCodeEnum;
import com.liang.platform.boot.entity.Result;
import com.liang.platform.boot.exception.BusinessException;
import com.liang.platform.boot.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ControllerAdviceConfig {

    /**
     * 服务名
     */
    @Value("${spring.application.name}")
    private String serverName;

    /**
     * 微服务系统标识
     */
    private String errorSystem;

    @PostConstruct
    public void init() {
        this.errorSystem = this.serverName + ": ";
    }

    /**
     * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {

    }

    /**
     * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
     */
    @ModelAttribute
    public void addAttributes(Model model) {

    }

    /**
     * 全局异常捕捉处理
     */
    @ExceptionHandler(value = {Exception.class})
    public Result handlerException(Exception exception, HttpServletRequest request) {
        log.error("请求路径uri={},系统内部出现异常:{}", request.getRequestURI(), exception);
        Result result = Result.error(ResultCodeEnum.ERROR, errorSystem + exception.toString());
        return result;
    }

    /**
     * 非法请求异常
     */
    @ExceptionHandler(value = {
            HttpMediaTypeNotAcceptableException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpRequestMethodNotSupportedException.class,
            MissingServletRequestParameterException.class,
            NoHandlerFoundException.class,
            MissingPathVariableException.class,
            HttpMessageNotReadableException.class
    })
    public Result handlerSpringAOPException(Exception exception) {
        return Result.error(ResultCodeEnum.ILLEGAL_REQUEST, errorSystem + exception.getMessage());
    }

    /**
     * 非法请求异常-参数类型不匹配
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public Result handlerSpringAOPException(MethodArgumentTypeMismatchException exception) {
        return Result.error(ResultCodeEnum.PARAM_TYPE_MISMATCH, errorSystem + exception.getMessage());
    }

    /**
     * 非法请求-参数校验
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public Result handlerMethodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        //获取异常字段及对应的异常信息
        StringBuilder stringBuilder = new StringBuilder();
        methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                .map(t -> t.getField() + "=>" + t.getDefaultMessage() + " ")
                .forEach(stringBuilder::append);
        String errorMessage = stringBuilder.toString();
        return Result.error(ResultCodeEnum.PARAM_VALID_ERROR, errorSystem + errorMessage);
    }

    /**
     * 非法请求异常-参数校验
     */
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public Result handlerConstraintViolationException(ConstraintViolationException constraintViolationException) {
        String errorMessage = constraintViolationException.getLocalizedMessage();
        return Result.error(ResultCodeEnum.PARAM_VALID_ERROR, errorSystem + errorMessage);
    }

    /**
     * 自定义业务异常-BusinessException
     */
    @ExceptionHandler(value = {BusinessException.class})
    public Result handlerCustomException(BusinessException exception) {
        String errorMessage = exception.getMsg();
        return Result.error(exception.getCode(), errorSystem + errorMessage);
    }

    /**
     * 自定义系统异常-SystemException
     */
    @ExceptionHandler(value = {SystemException.class})
    public Result handlerCustomException(SystemException exception) {
        String errorMessage = exception.getMsg();
        return Result.error(exception.getCode(), errorSystem + errorMessage);
    }

}