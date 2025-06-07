package com.chess.analyzer.backend.mappers;

import com.chess.analyzer.backend.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiResponseMapper<T> implements Mapper<ApiResponse<T>, ResponseEntity<ApiResponse<T>>> {

    @Override
    public ResponseEntity<ApiResponse<T>> mapTo(ApiResponse<T> source) {
        HttpStatus status = HttpStatus.resolve(source.getStatusCode());
        return ResponseEntity.status(status).body(source);
    }

    @Override
    public ApiResponse<T> mapFrom(ResponseEntity<ApiResponse<T>> source) {
        ApiResponse<T> body = source.getBody();
        body.setStatusCode(source.getStatusCodeValue());
        return body;
    }
}
