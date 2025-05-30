package com.chess.analyzer.backend.controllers;

import com.chess.analyzer.backend.mappers.ApiResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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

@WebMvcTest(PGNController.class)
@ContextConfiguration(classes = {
        PGNController.class,
        PGNControllerTest.TestConfig.class,
        com.chess.analyzer.backend.config.SecurityConfig.class
})
class PGNControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Upload valid .pgn file")
    void shouldAcceptValidPgnFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "game.pgn", MediaType.TEXT_PLAIN_VALUE, "1. e4 e5".getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.data").value("Received PGN: 1. e4 e5"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("Upload empty .pgn file")
    void shouldRejectEmptyPgnFile() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file", "game.pgn", MediaType.TEXT_PLAIN_VALUE, "".getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").isNotEmpty())
                .andExpect(jsonPath("$.error.errorCode").value("MISSING_DATA"))
                .andExpect(jsonPath("$.error.errors[0]").value("file: must not be empty"))
                .andExpect(jsonPath("$.error.timestamp").isNotEmpty());
    }
    @Test
    @DisplayName("Upload not a .pgn file")
    void shouldRejectNotPgnFile() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file", "game.pfg", MediaType.TEXT_PLAIN_VALUE, "1. e4 e5".getBytes());

        mockMvc.perform(multipart("/api/pgn/upload")
                        .file(file)
                        .with(authentication(mockJwtToken()))
                        .with(csrf())
                )
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").isNotEmpty())
                .andExpect(jsonPath("$.error.errorCode").value("MISSING_DATA"))
                .andExpect(jsonPath("$.error.errors[0]").value("file: must end with .pgn"))
                .andExpect(jsonPath("$.error.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Upload valid pgn json")
    void shouldAcceptValidPgnJson() throws Exception {
        String json = """
        {
            "rawPGN": "1. e4 e5"
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
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.data").value("Received PGN: 1. e4 e5"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @DisplayName("Upload empty json")
    void shouldRejectEmptyJson() throws Exception {
        String json = "";

        mockMvc.perform(post("/api/pgn/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(authentication(mockJwtToken()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
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
                .andExpect(jsonPath("$.error").isNotEmpty())
                .andExpect(jsonPath("$.error.errors[0]").value("rawPGN: must not be null or empty"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }


    @Configuration
    static class TestConfig {
        @Bean
        public ApiResponseMapper<String> apiResponseMapper() {
            return new ApiResponseMapper<>();
        }
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