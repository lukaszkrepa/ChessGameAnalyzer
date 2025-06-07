package com.chess.analyzer.backend.exception;

public class InvalidPGNJsonException extends RuntimeException{
    public InvalidPGNJsonException(String message) {
        super(message);
    }
}
