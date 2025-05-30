package com.chess.analyzer.backend.dto.pgn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilePGNRequest extends PGNRequest {
    private final String content;

    public FilePGNRequest(String content) {
        this.content = content;
    }

    @Override
    public String getPGN() {
        return content;
    }
}