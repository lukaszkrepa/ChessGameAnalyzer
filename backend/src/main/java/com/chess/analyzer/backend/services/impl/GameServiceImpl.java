package com.chess.analyzer.backend.services.impl;

import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.repositories.GameRepository;
import com.chess.analyzer.backend.services.GameService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public void saveGame(GameDTO game) {
        gameRepository.save(game);
    }

    @Override
    public GameDTO getGameById(String id) {
        return gameRepository.getById(id).orElseThrow(() -> new RuntimeException("Game not found"));
    }

    @Override
    public void deleteGame(String id) {
        gameRepository.deleteById(id);
    }

    @Override
    public boolean gameExists(String id) {
        return gameRepository.existsById(id);
    }

    @Override
    public List<GameDTO> getAllGames() {
        return gameRepository.findAll();
    }

    @Override
    public List<GameDTO> getGamesByWhitePlayer(String name) {
        return gameRepository.findByWhitePlayer(name);
    }
}
