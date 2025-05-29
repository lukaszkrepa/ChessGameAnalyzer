package com.chess.analyzer.backend.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/pingpong")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}

