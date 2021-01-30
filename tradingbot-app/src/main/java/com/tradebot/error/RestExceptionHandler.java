package com.tradebot.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler  {

    private static final String UNEXPECTED_EXCEPTION = "Unexpected exception";

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleRemainingExceptions(
        RuntimeException runTimeException, WebRequest request) {
        return buildResponseEntity(
            new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_EXCEPTION, runTimeException));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
