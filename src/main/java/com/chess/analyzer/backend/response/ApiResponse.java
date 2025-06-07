package com.chess.analyzer.backend.response;

import com.chess.analyzer.backend.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ApiMeta meta;
    private ApiError error;
    private int statusCode;

    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>(true, data, new ApiMeta(), null, statusCode);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(false, null, new ApiMeta(), error, error.getErrorCode().getHttpStatusCode());
    }
}

