package com.chess.analyzer.backend.services;

import com.chess.analyzer.backend.dto.pgn.PGNRequest;
import org.springframework.stereotype.Service;

@Service
public interface S3PgnService {

    String uploadToS3(String key, PGNRequest pgnRequest);

}
