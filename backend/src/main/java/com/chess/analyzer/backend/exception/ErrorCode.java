package com.chess.analyzer.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    VALIDATION_FAILED("VAL_001", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USR_404", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR("SYS_500", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("AUTH_401", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH_403", HttpStatus.FORBIDDEN),
    MISSING_DATA("DATA_400", HttpStatus.BAD_REQUEST);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
