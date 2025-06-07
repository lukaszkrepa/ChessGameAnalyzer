package com.chess.analyzer.backend.services.impl;

import com.chess.analyzer.backend.config.MyAppEnvConfig;
import com.chess.analyzer.backend.dto.GameDTO;
import com.chess.analyzer.backend.dto.pgn.PGNRequest;
import com.chess.analyzer.backend.services.S3PgnService;
import com.github.bhlangonijr.chesslib.game.Game;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Service
public class S3PgnServiceImpl implements S3PgnService {

    final S3Client s3Client;

    final MyAppEnvConfig myAppEnvConfig;

    public S3PgnServiceImpl(S3Client s3Client, MyAppEnvConfig myAppEnvConfig) {
        this.s3Client = s3Client;
        this.myAppEnvConfig = myAppEnvConfig;
    }

    @Override
    public String uploadToS3(String key, PGNRequest pgnRequest) {
        System.out.println("Uploading to S3 bucket: " + myAppEnvConfig.getAwsBucket());
        File file = pgnRequest.getPGNFile();
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(myAppEnvConfig.getAwsBucket())
                        .key(key)
                        .build(),
                RequestBody.fromFile(file)
        );
        return key;
    }


}
