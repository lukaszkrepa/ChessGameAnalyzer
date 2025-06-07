package com.chess.analyzer.backend.exception;

public class InvalidPrincipalException extends RuntimeException {
    public InvalidPrincipalException() {
        super("Unknown principal type.");
    }
}
