package com.chess.analyzer.backend.repositories;

import com.chess.analyzer.backend.config.LocalstackTestConfig;
import com.chess.analyzer.backend.dto.GameDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = {LocalstackTestConfig.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameRepositoryTests {

    @Container
    static final LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                    .withServices(LocalStackContainer.Service.DYNAMODB);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("aws.dynamodb.endpoint",
                () -> localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString());
    }

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<GameDTO> gameTable;

    private GameDTO testGame;

    @BeforeAll
    void setupTable() {
        gameTable = enhancedClient.table("games", TableSchema.fromBean(GameDTO.class));
        gameTable.createTable();
    }

    @BeforeEach
    void setUp() {
        gameTable.scan().items().forEach(gameTable::deleteItem);
        testGame = GameDTO.builder()
                .whitePlayer("Player1")
                .blackPlayer("Player2")
                .round("1")
                .site("Chess.com")
                .event("Daily Match")
                .result("1-0")
                .build();
    }

    @Test
    void shouldSaveAndRetrieveGame() {
        gameRepository.save(testGame);
        GameDTO retrieved = gameRepository.getById(testGame.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals("Player1", retrieved.getWhitePlayer());
        assertEquals("Player2", retrieved.getBlackPlayer());
    }

    @Test
    void shouldDeleteGame() {
        gameRepository.save(testGame);
        assertTrue(gameRepository.existsById(testGame.getId()));
        gameRepository.deleteById(testGame.getId());
        assertFalse(gameRepository.existsById(testGame.getId()));
    }

    @Test
    void shouldFindAllGames() {
        gameRepository.save(testGame);
        List<GameDTO> games = gameRepository.findAll();
        assertFalse(games.isEmpty());
    }

    @Test
    void shouldFindByWhitePlayer() {
        testGame.setWhitePlayer("SpecialPlayer");
        gameRepository.save(testGame);
        List<GameDTO> found = gameRepository.findByWhitePlayer("SpecialPlayer");
        assertFalse(found.isEmpty());
        assertEquals("SpecialPlayer", found.get(0).getWhitePlayer());
    }



}