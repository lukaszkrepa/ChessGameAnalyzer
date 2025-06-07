package com.chess.analyzer.backend.services;

import com.chess.analyzer.backend.config.DynamoDBTestConfig;
import com.chess.analyzer.backend.config.LocalstackTestConfig;
import com.chess.analyzer.backend.dto.GameDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({LocalstackTestConfig.class, DynamoDBTestConfig.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceImplTests {

    @Autowired
    private GameService gameService;

    private GameDTO sampleGame;

    @Autowired
    private  DynamoDbEnhancedClient enhancedClient;
    @BeforeAll
    void setupTable() {
        DynamoDbTable<GameDTO> gameTable = enhancedClient.table("games", TableSchema.fromBean(GameDTO.class));
        gameTable.createTable();
    }
    @BeforeEach
    void setUp() {
        sampleGame = new GameDTO();
        sampleGame.setId("game001");
        sampleGame.setWhitePlayer("Magnus Carlsen");
        sampleGame.setBlackPlayer("Fabiano Caruana");
        sampleGame.setResult("1-0");
        sampleGame.setEvent("World Championship");
    }

    @Test
    void shouldSaveAndRetrieveGame() {
        gameService.saveGame(sampleGame);

        GameDTO fetched = gameService.getGameById("game001");
        assertEquals("Magnus Carlsen", fetched.getWhitePlayer());
    }

    @Test
    void shouldReturnTrueIfGameExists() {
        gameService.saveGame(sampleGame);
        assertTrue(gameService.gameExists("game001"));
    }

    @Test
    void shouldDeleteGame() {
        gameService.saveGame(sampleGame);
        gameService.deleteGame("game001");
        assertFalse(gameService.gameExists("game001"));
    }

    @Test
    void shouldReturnAllGames() {
        gameService.saveGame(sampleGame);

        GameDTO second = new GameDTO();
        second.setId("game002");
        second.setWhitePlayer("Hikaru Nakamura");
        second.setBlackPlayer("Ian Nepomniachtchi");
        second.setResult("0-1");
        second.setEvent("Candidates");

        gameService.saveGame(second);

        List<GameDTO> allGames = gameService.getAllGames();
        assertEquals(2, allGames.size());
    }

    @Test
    void shouldFindGamesByWhitePlayer() {
        gameService.saveGame(sampleGame);

        List<GameDTO> magnusGames = gameService.getGamesByWhitePlayer("Magnus Carlsen");
        assertEquals(1, magnusGames.size());
        assertEquals("game001", magnusGames.get(0).getId());
    }
}
