package com.chess.analyzer.backend.exception;

public class PGNParseException extends RuntimeException {
    public PGNParseException(String message) {
        super(message);
    }

    public PGNParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

