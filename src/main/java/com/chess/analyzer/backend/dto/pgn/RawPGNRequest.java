package com.chess.analyzer.backend.dto.pgn;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RawPGNRequest extends PGNRequest {
    private String rawPGN;
    @Override
    public String getPGN() {
        return rawPGN;
    }
}
