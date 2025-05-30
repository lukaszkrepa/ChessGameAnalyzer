package com.chess.analyzer.backend.controllers;

import com.chess.analyzer.backend.dto.pgn.FilePGNRequest;
import com.chess.analyzer.backend.dto.pgn.PGNRequest;
import com.chess.analyzer.backend.dto.pgn.RawPGNRequest;
import com.chess.analyzer.backend.mappers.ApiResponseMapper;
import com.chess.analyzer.backend.utils.ApiError;
import com.chess.analyzer.backend.utils.ApiResponse;
import com.chess.analyzer.backend.utils.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pgn")
public class PGNController {

    private final ApiResponseMapper<String> apiResponseMapper;

    public PGNController(ApiResponseMapper<String> apiResponseMapper) {
        this.apiResponseMapper = apiResponseMapper;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> handleFileUpload(
            HttpServletRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                ApiError error = ApiError.builder()
                        .errorCode(ErrorCode.MISSING_DATA)
                        .message("File is required but not provided.")
                        .debugMessage("Multipart file is empty.")
                        .path(request.getRequestURI())
                        .errors(List.of("file: must not be empty"))
                        .build();
                return apiResponseMapper.mapTo(ApiResponse.error(error));
            }
            if (!file.getOriginalFilename().endsWith(".pgn")){
                ApiError error = ApiError.builder()
                        .errorCode(ErrorCode.MISSING_DATA)
                        .message("Wrong file type provided.")
                        .debugMessage("File must end with .pgn")
                        .path(request.getRequestURI())
                        .errors(List.of("file: must end with .pgn"))
                        .build();
                return apiResponseMapper.mapTo(ApiResponse.error(error));
            }

            String content = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            PGNRequest pgnRequest = new FilePGNRequest(content);
            return apiResponseMapper.mapTo(ApiResponse.success("Received PGN: " + pgnRequest.getPGN(), 200));

        } catch (Exception e) {
            ApiError error = ApiError.builder()
                    .errorCode(ErrorCode.INTERNAL_ERROR)
                    .message("Error processing PGN file.")
                    .debugMessage(e.getMessage())
                    .path(request.getRequestURI())
                    .errors(List.of("Exception: " + e.getClass().getSimpleName()))
                    .build();
            return apiResponseMapper.mapTo(ApiResponse.error(error));
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> handleJsonUpload(
            HttpServletRequest request,
            @RequestBody RawPGNRequest rawPGNRequest
    ) {
        try {
            if (rawPGNRequest == null || rawPGNRequest.getRawPGN() == null || rawPGNRequest.getRawPGN().isEmpty()) {
                ApiError error = ApiError.builder()
                        .errorCode(ErrorCode.MISSING_DATA)
                        .message("JSON body is required but missing.")
                        .debugMessage("Null or empty rawPGN received.")
                        .path(request.getRequestURI())
                        .errors(List.of("rawPGN: must not be null or empty"))
                        .build();
                return apiResponseMapper.mapTo(ApiResponse.error(error));
            }

            return apiResponseMapper.mapTo(ApiResponse.success("Received PGN: " + ((PGNRequest) rawPGNRequest).getPGN(), 200));

        } catch (Exception e) {
            ApiError error = ApiError.builder()
                    .errorCode(ErrorCode.INTERNAL_ERROR)
                    .message("Error processing JSON PGN.")
                    .debugMessage(e.getMessage())
                    .path(request.getRequestURI())
                    .errors(List.of("Exception: " + e.getClass().getSimpleName()))
                    .build();
            return apiResponseMapper.mapTo(ApiResponse.error(error));
        }
    }
}
