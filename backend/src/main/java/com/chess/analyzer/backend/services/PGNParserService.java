package com.chess.analyzer.backend.services;

import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.exception.PGNParseException;

public interface PGNParserService {
    public GameDTO parsePGN(String pgnContent) throws PGNParseException;
}
