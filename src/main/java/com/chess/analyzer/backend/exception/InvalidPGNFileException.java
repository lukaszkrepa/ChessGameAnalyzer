package com.chess.analyzer.backend.exception;

public class InvalidPGNFileException extends RuntimeException {
    public InvalidPGNFileException(String message) {
        super(message);
    }
}
