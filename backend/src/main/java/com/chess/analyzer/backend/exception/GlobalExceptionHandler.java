package com.chess.analyzer.backend.exception;

import com.chess.analyzer.backend.mappers.ApiResponseMapper;
import com.chess.analyzer.backend.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiResponseMapper<?> mapper;

    public GlobalExceptionHandler(ApiResponseMapper<?> mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(InvalidPGNFileException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleInvalidFile(InvalidPGNFileException ex, HttpServletRequest request) {
        return mapper.mapTo(ApiResponse.error(ApiError.builder()
                .errorCode(ErrorCode.MISSING_DATA)
                .message("Invalid PGN file.")
                .debugMessage(ex.getMessage())
                .path(request.getRequestURI())
                .errors(List.of("file: invalid"))
                .build()));
    }

    @ExceptionHandler(InvalidPrincipalException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleInvalidPrincipal(InvalidPrincipalException ex, HttpServletRequest request) {
        return mapper.mapTo(ApiResponse.error(ApiError.builder()
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .message("Authentication error.")
                .debugMessage(ex.getMessage())
                .path(request.getRequestURI())
                .errors(List.of("principal: unknown type"))
                .build()));
    }

    @ExceptionHandler(InvalidPGNJsonException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleInvalidJson(InvalidPGNJsonException ex, HttpServletRequest request) {
        return mapper.mapTo(ApiResponse.error(ApiError.builder()
                .errorCode(ErrorCode.MISSING_DATA)
                .message("JSON body is required but missing.")
                .debugMessage(ex.getMessage())
                .path(request.getRequestURI())
                .errors(List.of("rawPGN: invalid"))
                .build()));
    }

    @ExceptionHandler(PGNParseException.class)
    public ResponseEntity<? extends ApiResponse<?>> handlePGNParseError(PGNParseException ex, HttpServletRequest request) {
        return mapper.mapTo(ApiResponse.error(ApiError.builder()
                .errorCode(ErrorCode.MISSING_DATA)
                .message("Error parsing PGN.")
                .debugMessage(ex.getMessage())
                .path(request.getRequestURI())
                .errors(List.of("PGN: invalid"))
                .build()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<? extends ApiResponse<?>> handleGenericError(Exception ex, HttpServletRequest request) {
        return mapper.mapTo(ApiResponse.error(ApiError.builder()
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .message("Unexpected server error.")
                .debugMessage(ex.getMessage())
                .path(request.getRequestURI())
                .errors(List.of("Exception: " + ex.getClass().getSimpleName()))
                .build()));
    }
}
