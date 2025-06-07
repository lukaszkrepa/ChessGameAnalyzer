package com.chess.analyzer.backend.dto.pgn;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Setter
@Getter
public class RawPGNRequest extends PGNRequest {
    private String rawPGN;
    private File tempFile;

    public void initializeFile() {
        this.tempFile = createTempFile(rawPGN);
    }

    @Override
    public String getPGN() {
        return rawPGN;
    }

    @Override
    public File getPGNFile() {
        if (tempFile == null && rawPGN != null) {
            initializeFile();
        }
        return tempFile;
    }

    private File createTempFile(String content) {
        try {
            File file = File.createTempFile("raw-pgn-", ".pgn");
            Files.writeString(file.toPath(), content);
            file.deleteOnExit(); // optional cleanup
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp PGN file from raw string", e);
        }
    }
}
