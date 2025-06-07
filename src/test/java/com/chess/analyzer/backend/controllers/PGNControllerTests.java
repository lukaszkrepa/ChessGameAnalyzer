package com.chess.analyzer.backend.controllers;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PGNControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Upload empty json")
    void shouldRejectEmptyJson() throws Exception {
        String json = "{}";

        mockMvc.perform(post("/api/pgn/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(authentication(mockJwtToken()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Upload empty rawPGN json")
    void shouldRejectEmptyPgnJson() throws Exception {
        String json = """
        {
            "rawPGN": ""
        }
        """;

        mockMvc.perform(post("/api/pgn/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(authentication(mockJwtToken()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Upload valid PGN json")
    void shouldAcceptValidPgnJson() throws Exception {
        String json = """
        {
            "rawPGN": "[Result \\"1-0\\"]\\n1. e4 e5 2. Nf3 1-0"
        }
        """;

        mockMvc.perform(post("/api/pgn/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(authentication(mockJwtToken()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.moves").isArray())
                .andExpect(jsonPath("$.data.moves[0]").value("e4"))
                .andExpect(jsonPath("$.data.moves[1]").value("e5"))
                .andExpect(jsonPath("$.data.moves[2]").value("Nf3"))
                .andExpect(jsonPath("$.data.whitePlayer").value("Unknown"))
                .andExpect(jsonPath("$.data.blackPlayer").value("Unknown"))
                .andExpect(jsonPath("$.data.result").value("1-0"));
    }

    @Test
    @DisplayName("Upload valid .pgn file")
    void shouldAcceptValidPgnFile() throws Exception {
        String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 1-0";
        MockMultipartFile file = new MockMultipartFile(
                "file", "game.pgn", MediaType.TEXT_PLAIN_VALUE, pgn.getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.moves").isArray())
                .andExpect(jsonPath("$.data.moves[0]").value("e4"))
                .andExpect(jsonPath("$.data.moves[1]").value("e5"))
                .andExpect(jsonPath("$.data.moves[2]").value("Nf3"))
                .andExpect(jsonPath("$.data.moves[3]").value("Nc6"))
                .andExpect(jsonPath("$.data.moves[4]").value("Bb5"));
    }

    @Test
    @DisplayName("Upload valid .pgn file with complete game information")
    void shouldParseCompleteGameInformation() throws Exception {
        String completePgn = """
                [Event "Casual Game"]
                [Site "Chess.com"]
                [Date "2023.12.20"]
                [White "Player1"]
                [Black "Player2"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 1-0
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "complete_game.pgn", MediaType.TEXT_PLAIN_VALUE, completePgn.getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.moves").isArray())
                .andExpect(jsonPath("$.data.moves").isNotEmpty())
                .andExpect(jsonPath("$.data.whitePlayer").value("Player1"))
                .andExpect(jsonPath("$.data.blackPlayer").value("Player2"))
                .andExpect(jsonPath("$.data.result").value("1-0"))
                .andExpect(jsonPath("$.data.event").value("Casual Game"))
                .andExpect(jsonPath("$.data.date").value("2023-12-20"))
                .andExpect(jsonPath("$.data.site").value("Chess.com"));
    }

    @Test
    @DisplayName("Upload invalid .pgn file")
    void shouldRejectInvalidPgnFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.pgn", MediaType.TEXT_PLAIN_VALUE, "invalid pgn content".getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Upload malformed .pgn file")
    void shouldRejectMalformedPgnFile() throws Exception {
        String malformedPgn = """
                [Event "Casual Game"
                [White "Player1"]
                1. e4 e5 2. ???
                """;  // Missing brackets and invalid moves

        MockMultipartFile file = new MockMultipartFile(
                "file", "malformed.pgn", MediaType.TEXT_PLAIN_VALUE, malformedPgn.getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }
    @Test
    @DisplayName("Upload not a .pgn file")
    void shouldRejectNonPgnFile() throws Exception {
        String notPgnContent = "This is not a PGN file";
        MockMultipartFile file = new MockMultipartFile(
                "file", "not_pgn.txt", MediaType.TEXT_PLAIN_VALUE, notPgnContent.getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    private static JwtAuthenticationToken mockJwtToken() {
        Jwt jwt = new Jwt(
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "test-user",
                        "username", "test",
                        "scope", "openid email"
                )
        );

        return new JwtAuthenticationToken(jwt, List.of(() -> "ROLE_USER"));
    }
}
