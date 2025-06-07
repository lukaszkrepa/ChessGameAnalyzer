package com.chess.analyzer.backend.services.impl;

import com.chess.analyzer.backend.services.PGNParserService;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.exception.PGNParseException;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Board;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PGNParserServiceImpl implements PGNParserService {

    public GameDTO parsePGN(String pgnContent) throws PGNParseException  {
        try {
            File tempFile = File.createTempFile("pgn_upload", ".pgn");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(pgnContent);
            }

            PgnHolder pgnHolder = new PgnHolder(tempFile.getAbsolutePath());
            pgnHolder.loadPgn();

            List<Game> games = pgnHolder.getGames();
            if (games.isEmpty()) {
                throw new PGNParseException("No games found in PGN content");
            }

            Game game = games.get(0);

            if (game == null) {
                throw new PGNParseException("Parsed game is null");
            }
            if (extractMoves(game).isEmpty()) {
                throw new PGNParseException("No moves found in the game");
            }

            return GameDTO.builder()
                    .whitePlayer(normalizePlayerName(game.getWhitePlayer().getName()))
                    .blackPlayer(normalizePlayerName(game.getBlackPlayer().getName()))
                    .result(normalizeResult(game.getResult()))
                    .event(normalizeText(game.getRound().getEvent().getName()))
                    .site(normalizeText(game.getRound().getEvent().getSite()))
                    .date(parseDate(game.getDate()))
                    .round(normalizeText(String.valueOf(game.getRound().getNumber())))
                    .moves(extractMoves(game))
                    .uciMoves(extractUciMoves(game))
                    .fens(extractFens(game))
                    .build();

        } catch (Exception e) {
            throw new PGNParseException("Error parsing PGN content: " + e.getMessage(), e);
        }
    }

    private String normalizePlayerName(String name) {
        return (name == null || name.trim().isEmpty()) ? "Unknown" : name.trim();
    }

    private String normalizeResult(GameResult result) {
        if (result == null) return "*";
        return switch (result) {
            case WHITE_WON -> "1-0";
            case BLACK_WON -> "0-1";
            case DRAW -> "1/2-1/2";
            default -> "*";
        };
    }

    private String normalizeText(String text) {
        return (text == null || text.trim().isEmpty()) ? "?" : text.trim();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            String normalizedDate = dateStr.replace(".", "-").replace("?", "01");
            return LocalDate.parse(normalizedDate, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.warn("Unable to parse date: {}", dateStr);
            return null;
        }
    }

    private List<String> extractMoves(Game game) {
        return game.getHalfMoves().stream()
                .map(Move::getSan)
                .filter(san -> san != null && !san.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> extractUciMoves(Game game) {
        return game.getHalfMoves().stream()
                .map(Move::toString)
                .collect(Collectors.toList());
    }

    private List<String> extractFens(Game game) {
        List<String> fens = new ArrayList<>();
        Board board = new Board();
        for (Move move : game.getHalfMoves()) {
            board.doMove(move);
            fens.add(board.getFen());
        }
        return fens;
    }
}
