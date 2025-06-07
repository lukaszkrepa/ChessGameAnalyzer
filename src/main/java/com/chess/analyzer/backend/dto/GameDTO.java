package com.chess.analyzer.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class GameDTO {
    private String id = UUID.randomUUID().toString(); // Default initialization
    private String userId;
    private String whitePlayer;
    private String blackPlayer;
    private String result;
    private String event;
    private String site;
    private String round;
    private List<String> moves;
    private List<String> uciMoves;
    private List<String> fens;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate date;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = (id == null) ? UUID.randomUUID().toString() : id;
    }

    // Custom builder to ensure ID is always set
    public static class GameDTOBuilder {
        private String id = UUID.randomUUID().toString();

        public GameDTOBuilder id(String id) {
            this.id = (id == null) ? UUID.randomUUID().toString() : id;
            return this;
        }
    }
}
