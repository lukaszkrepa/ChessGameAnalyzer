package com.chess.analyzer.backend.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class ApiMeta {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String version = "1.0";

    public ApiMeta() {
        this.timestamp = LocalDateTime.now();
    }
}
