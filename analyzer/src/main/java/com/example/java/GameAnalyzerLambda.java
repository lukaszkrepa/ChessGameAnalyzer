package com.example.java;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameAnalyzerLambda implements RequestHandler<DynamodbEvent, String> {

    private static final String TABLE_NAME = "games";
    private static final String STOCKFISH_TMP_PATH = "/tmp/stockfish";

    @Override
    public String handleRequest(DynamodbEvent event, Context context) {
        try {
            ensureStockfishExecutable();
            DynamoDbClient ddb = DynamoDbClient.create();

            for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
                Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage =
                        record.getDynamodb().getNewImage();

                if (newImage == null || !newImage.containsKey("id") || !newImage.containsKey("fens") || !newImage.containsKey("uciMoves"))
                    continue;

                String gameId = newImage.get("id").getS();

                List<String> fens = newImage.get("fens").getL().stream().map(attr -> attr.getS()).toList();
                List<String> moves = newImage.get("uciMoves").getL().stream().map(attr -> attr.getS()).toList();

                List<AttributeValue> analysis = new ArrayList<>();

                for (int i = 0; i < fens.size(); i++) {
                    String move = moves.get(i);
                    String fen = fens.get(i);
                    String rawEval = evaluateWithStockfish(fen);

                    String scoreType = "N/A";
                    int score = 0;
                    int depth = 0;

                    try {
                        for (String part : rawEval.split(" ")) {
                            if (part.equals("cp") || part.equals("mate")) {
                                scoreType = part;
                            } else if (scoreType.equals("cp") || scoreType.equals("mate")) {
                                score = Integer.parseInt(part);
                                break;
                            }
                        }

                        if (rawEval.contains("depth")) {
                            String[] parts = rawEval.split(" ");
                            for (int j = 0; j < parts.length - 1; j++) {
                                if (parts[j].equals("depth")) {
                                    depth = Integer.parseInt(parts[j + 1]);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to parse eval: " + rawEval);
                    }

                    analysis.add(AttributeValue.fromM(Map.of(
                            "move", AttributeValue.fromS(move),
                            "fen", AttributeValue.fromS(fen),
                            "scoreType", AttributeValue.fromS(scoreType),
                            "score", AttributeValue.fromN(String.valueOf(score)),
                            "depth", AttributeValue.fromN(String.valueOf(depth))
                    )));
                }

                ddb.updateItem(UpdateItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .key(Map.of("id", AttributeValue.fromS(gameId)))
                        .attributeUpdates(Map.of(
                                "analysis", AttributeValueUpdate.builder()
                                        .value(AttributeValue.fromL(analysis))
                                        .action(AttributeAction.PUT)
                                        .build()
                        ))
                        .build());
            }

            return "Processed stream records.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private void ensureStockfishExecutable() throws IOException {
        File stockfish = new File(STOCKFISH_TMP_PATH);
        if (!stockfish.exists()) {
            InputStream in = GameAnalyzerLambda.class.getResourceAsStream("/stockfish");
            if (in == null) throw new FileNotFoundException("Stockfish binary not found in resources");
            Files.copy(in, stockfish.toPath());
            stockfish.setExecutable(true);
        }
    }

    private String evaluateWithStockfish(String fen) throws IOException {
        Process engine;
        try {
            engine = new ProcessBuilder(STOCKFISH_TMP_PATH).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new IOException("Failed to start Stockfish binary: " + e.getMessage(), e);
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(engine.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(engine.getInputStream()));

        writer.write("uci\n");
        writer.flush();
        waitFor(reader, "uciok");

        writer.write("isready\n");
        writer.flush();
        waitFor(reader, "readyok");

        writer.write("position fen " + fen + "\n");
        writer.write("go depth 12\n");
        writer.flush();

        String line;
        String bestEval = "N/A";
        while ((line = reader.readLine()) != null) {
            if (line.contains("score")) bestEval = line;
            if (line.startsWith("bestmove")) break;
        }

        writer.write("quit\n");
        writer.flush();
        engine.destroy();

        return bestEval;
    }

    private void waitFor(BufferedReader reader, String expected) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(expected)) break;
        }
    }
}
