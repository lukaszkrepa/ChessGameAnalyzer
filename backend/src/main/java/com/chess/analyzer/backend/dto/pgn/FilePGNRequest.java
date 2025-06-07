package com.chess.analyzer.backend.dto.pgn;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Getter
@Setter
public class FilePGNRequest extends PGNRequest {
    private final String content;
    private File tempFile;

    public FilePGNRequest(String content) {
        this.content = content;
        this.tempFile = createTempFile(content);
    }

    @Override
    public String getPGN() {
        return content;
    }

    @Override
    public File getPGNFile() {
        return tempFile;
    }

    private File createTempFile(String content) {
        try {
            File file = File.createTempFile("pgn-", ".pgn");
            Files.writeString(file.toPath(), content);
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp PGN file", e);
        }
    }
}
