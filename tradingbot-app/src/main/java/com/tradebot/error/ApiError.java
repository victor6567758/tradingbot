package com.tradebot.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@JsonTypeInfo(
    include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiError {

    private static final String GENERAL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private final HttpStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = GENERAL_DATETIME_FORMAT)
    private final LocalDateTime timestamp;

    private final String message;

    private String debugMessage;

    public ApiError(HttpStatus status, String message) {
        this.status = status;
        timestamp = LocalDateTime.now();
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, Throwable exception) {
        this(status, message);
        this.debugMessage = ExceptionUtils.getMessage(exception);
    }
}
