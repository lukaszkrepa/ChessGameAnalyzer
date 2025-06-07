package com.chess.analyzer.backend.controllers;

import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.dto.pgn.FilePGNRequest;
import com.chess.analyzer.backend.dto.pgn.PGNRequest;
import com.chess.analyzer.backend.dto.pgn.RawPGNRequest;
import com.chess.analyzer.backend.exception.*;
import com.chess.analyzer.backend.mappers.ApiResponseMapper;
import com.chess.analyzer.backend.services.impl.PGNUploadServiceImpl;
import com.chess.analyzer.backend.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pgn")
public class PGNController {

    private final ApiResponseMapper<GameDTO> apiResponseMapper;
    private final PGNUploadServiceImpl pgnService;

    public PGNController(ApiResponseMapper<GameDTO> apiResponseMapper, PGNUploadServiceImpl pgnService) {
        this.apiResponseMapper = apiResponseMapper;
        this.pgnService = pgnService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GameDTO>> handleFileUpload(
            HttpServletRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Object principal
    ) {
        if (file.isEmpty()) {
            throw new InvalidPGNFileException("Multipart file is empty.");
        }
        if (!file.getOriginalFilename().endsWith(".pgn")) {
            throw new InvalidPGNFileException("File must end with .pgn");
        }

        String content;
        try {
            content = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new InvalidPGNFileException("Error processing PGN upload: " + e.getMessage());
        }

        PGNRequest pgnRequest = new FilePGNRequest(content);
        String userID = extractUserId(principal,request);
        String s3Key = generateS3Key(userID);

        GameDTO gameDTO = pgnService.processPGNUpload(pgnRequest, userID, s3Key);
        return apiResponseMapper.mapTo(ApiResponse.success(gameDTO, 200));
    }

    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<GameDTO>> handleJsonUpload(
            HttpServletRequest request,
            @RequestBody RawPGNRequest rawPGNRequest,
            @AuthenticationPrincipal Object principal
    ) {
        if (rawPGNRequest == null){
            throw new InvalidPGNJsonException("Null rawPGN received");
        }

        if (rawPGNRequest.getRawPGN() == null){
            throw new InvalidPGNJsonException("Null rawPGN received");
        }

        if (rawPGNRequest.getRawPGN().isEmpty()){
            throw new InvalidPGNJsonException("Empty rawPGN received");
        }

        String userID = extractUserId(principal,request);
        String s3Key = generateS3Key(userID);
        GameDTO gameDTO = pgnService.processPGNUpload(rawPGNRequest, userID, s3Key);
        return apiResponseMapper.mapTo(ApiResponse.success(gameDTO, 200));
    }
    private String generateS3Key(String userID) {
        return String.format("%s/%s.pgn", userID, UUID.randomUUID());
    }
    private String extractUserId(Object principal, HttpServletRequest request) throws IllegalArgumentException {
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getAttributes().get("sub").toString();
        } else if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        } else {
            throw new IllegalArgumentException("Unknown principal type.");
        }
    }
    private ResponseEntity<ApiResponse<GameDTO>> buildErrorResponse(HttpServletRequest request, String message, String debug, String errorType, ErrorCode code) {
        ApiError error = ApiError.builder()
                .errorCode(code)
                .message(message)
                .debugMessage(debug)
                .path(request.getRequestURI())
                .errors(List.of(errorType))
                .build();
        return apiResponseMapper.mapTo(ApiResponse.error(error));
    }
}
