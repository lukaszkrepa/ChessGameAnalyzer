package com.chess.analyzer.backend.dto.pgn;

import java.io.File;

public abstract class PGNRequest {
    public abstract String getPGN();
    public abstract File getPGNFile();
}
