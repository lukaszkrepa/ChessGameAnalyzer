package com.chess.analyzer.backend.services;

import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.dto.pgn.PGNRequest;
import com.chess.analyzer.backend.exception.PGNParseException;

public interface PGNUploadService {
    public GameDTO processPGNUpload(PGNRequest pgnRequest, String userId, String s3Key) throws PGNParseException;
}
