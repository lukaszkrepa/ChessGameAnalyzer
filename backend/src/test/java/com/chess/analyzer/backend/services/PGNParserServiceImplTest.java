package com.chess.analyzer.backend.services;

import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.services.impl.PGNParserServiceImpl;
import com.chess.analyzer.backend.exception.PGNParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class PGNParserServiceImplTest {

    private PGNParserServiceImpl pgnParserService;

    @BeforeEach
    void setUp() {
        pgnParserService = new PGNParserServiceImpl();
    }

    @Test
    @DisplayName("Should successfully parse valid PGN")
    void testValidPGN() throws PGNParseException {
        String validPGN = """
            [Event "Casual Game"]
            [Site "Chess.com"]
            [Date "2024.01.20"]
            [White "Player1"]
            [Black "Player2"]
            [Result "1-0"]
            
            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1-0
            """;
        GameDTO result = pgnParserService.parsePGN(validPGN);

        assertNotNull(result);
        assertEquals("Casual Game", result.getEvent());
        assertEquals("Chess.com", result.getSite());
        assertEquals("2024-01-20", result.getDate().toString());
        assertEquals("Player1", result.getWhitePlayer());
        assertEquals("Player2", result.getBlackPlayer());
        assertEquals("1-0", result.getResult());
    }

    @Test
    @DisplayName("Should handle PGN with missing metadata")
    void testValidPGNWithoutMetadata() throws PGNParseException {
        String pgnWithMissingMetadata = """
            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1-0
            """;

        GameDTO result = pgnParserService.parsePGN(pgnWithMissingMetadata);

        assertNotNull(result);
        assertNotNull(result.getMoves());
        assertFalse(result.getMoves().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception for invalid PGN format")
    void testInvalidPGN() {
        String invalidPGN = "This is not a valid PGN format";

        assertThrows(PGNParseException.class, () -> {
            pgnParserService.parsePGN(invalidPGN);
        });
    }

    @Test
    @DisplayName("Should handle PGN with special characters in metadata")
    void testValidPGNWithSpecialChars() throws PGNParseException {
        // Given
        String pgnWithSpecialChars = """
            [Event "Player's Tournament"]
            [Site "St. Petersburg's Chess Club"]
            [Date "2024.01.20"]
            [White "O'Connor, John"]
            [Black "D'Angelo, Peter"]
            [Result "1/2-1/2"]
            
            1. e4 e5 2. Nf3 Nc6 1/2-1/2
            """;

        GameDTO result = pgnParserService.parsePGN(pgnWithSpecialChars);

        assertNotNull(result);
        assertEquals("Player's Tournament", result.getEvent());
        assertEquals("O'Connor, John", result.getWhitePlayer());
        assertEquals("1/2-1/2", result.getResult());
    }

    @Test
    @DisplayName("Should handle PGN with comments and annotations")
    void testValidPGNWithCommentsAndAnnotations() throws PGNParseException {
        String pgnWithComments = """
            [Event "Annotated Game"]
            [Site "Online"]
            
            1. e4 {Best by test} e5 2. Nf3 Nc6 {The most natural response} 3. Bb5! a6 1-0
            """;

        GameDTO result = pgnParserService.parsePGN(pgnWithComments);

        assertNotNull(result);
        assertFalse(result.getMoves().isEmpty());
        assertEquals("Annotated Game", result.getEvent());
    }

    @Test
    @DisplayName("Should handle empty PGN string")
    void testEmptyPGN() {
        String emptyPGN = "";

        assertThrows(PGNParseException.class, () -> {
            pgnParserService.parsePGN(emptyPGN);
        });
    }

    @Test
    @DisplayName("Should handle null PGN input")
    void testNullPGN() {
        assertThrows(PGNParseException.class, () -> {
            pgnParserService.parsePGN(null);
        });
    }
}
