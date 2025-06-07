package com.chess.analyzer.backend.services.impl;

import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.dto.pgn.PGNRequest;
import com.chess.analyzer.backend.services.PGNParserService;
import com.chess.analyzer.backend.services.PGNUploadService;
import com.chess.analyzer.backend.services.S3PgnService;
import com.chess.analyzer.backend.exception.PGNParseException;
import org.springframework.stereotype.Service;

@Service
public class PGNUploadServiceImpl implements PGNUploadService {
    private final S3PgnService s3PgnService;
    private final GameServiceImpl gameServiceImpl;
    private final PGNParserService pgnParserService;

    public PGNUploadServiceImpl(S3PgnService s3PgnService, GameServiceImpl gameServiceImpl, PGNParserService pgnParserService) {
        this.s3PgnService = s3PgnService;
        this.gameServiceImpl = gameServiceImpl;
        this.pgnParserService = pgnParserService;
    }
    public GameDTO processPGNUpload(PGNRequest pgnRequest, String userId, String s3Key) throws PGNParseException {
        try {
            GameDTO game = pgnParserService.parsePGN(pgnRequest.getPGN());
            game.setUserId(userId);
            gameServiceImpl.saveGame(game);
            s3PgnService.uploadToS3(s3Key, pgnRequest);
            return game;
        } catch (Exception e) {
            throw new PGNParseException("Error processing PGN upload: " + e.getMessage(), e);
        }

    }
}
