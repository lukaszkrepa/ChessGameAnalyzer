package com.chess.analyzer.backend.repositories;

import com.chess.analyzer.backend.dto.GameDTO;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GameRepository {

    private final DynamoDbTable<GameDTO> gameTable;

    public GameRepository(DynamoDbEnhancedClient enhancedClient) {
        this.gameTable = enhancedClient.table("games", TableSchema.fromBean(GameDTO.class));
    }

    public void save(GameDTO game) {
        gameTable.putItem(game);
    }

    public Optional<GameDTO> getById(String id) {
        return Optional.ofNullable(gameTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }

    public void deleteById(String id) {
        gameTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }

    public boolean existsById(String id) {
        return getById(id).isPresent();
    }

    public List<GameDTO> findAll() {
        return gameTable.scan().items().stream().collect(Collectors.toList());
    }

    public List<GameDTO> findByWhitePlayer(String whitePlayer) {
        return findAll().stream()
                .filter(game -> whitePlayer.equals(game.getWhitePlayer()))
                .collect(Collectors.toList());
    }
}
