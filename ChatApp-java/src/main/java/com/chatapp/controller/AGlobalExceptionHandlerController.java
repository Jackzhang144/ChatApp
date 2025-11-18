package com.chatapp.controller;

import com.chatapp.entity.enums.ResponseCodeEnum;
import com.chatapp.entity.vo.ResponseVO;
import com.chatapp.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class AGlobalExceptionHandlerController extends ABaseController {

    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception exception, HttpServletRequest request) {
        log.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), exception);
        return switch (exception) {
            case NoHandlerFoundException ignored -> error(ResponseCodeEnum.CODE_404);
            case BusinessException businessException -> businessError(businessException);
            case BindException ignored -> error(ResponseCodeEnum.CODE_600);
            case MethodArgumentTypeMismatchException ignored -> error(ResponseCodeEnum.CODE_600);
            case DuplicateKeyException ignored -> error(ResponseCodeEnum.CODE_601);
            default -> error(ResponseCodeEnum.CODE_500);
        };
    }

    private ResponseVO<Void> businessError(BusinessException exception) {
        ResponseVO<Void> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_ERROR);
        responseVO.setCode(exception.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : exception.getCode());
        responseVO.setInfo(exception.getMessage());
        return responseVO;
    }

    private ResponseVO<Void> error(ResponseCodeEnum responseCodeEnum) {
        ResponseVO<Void> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_ERROR);
        responseVO.setCode(responseCodeEnum.getCode());
        responseVO.setInfo(responseCodeEnum.getMsg());
        return responseVO;
    }
}
