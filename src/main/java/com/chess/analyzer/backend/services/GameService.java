package com.chess.analyzer.backend.services;

import com.chess.analyzer.backend.dto.GameDTO;

import java.util.List;

public interface GameService {
    public void saveGame(GameDTO game);
    public GameDTO getGameById(String id);

    public void deleteGame(String id);

    public boolean gameExists(String id);

    public List<GameDTO> getAllGames();

    public List<GameDTO> getGamesByWhitePlayer(String name);
}
