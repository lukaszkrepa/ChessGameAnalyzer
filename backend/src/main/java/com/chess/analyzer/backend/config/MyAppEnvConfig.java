package com.chess.analyzer.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "myapp")
public class MyAppEnvConfig {

    private String awsBucket;
    private String cognitoClientSecret;
    private String dynamodbEndpoint;
    private String awsRegion;
    private String dynamodbAccessKey;
    private String dynamodbSecretKey;
    private String dynamodbSessionToken;

    public String getAwsBucket() {
        return requireNonEmpty(awsBucket, "AWS_BUCKET");
    }
    public String getCognitoClientSecret() {
        return requireNonEmpty(cognitoClientSecret, "COGNITO_CLIENT_SECRET");
    }
    public String getDynamodbEndpoint() {
        return requireNonEmpty(dynamodbEndpoint, "DYNAMODB_ENDPOINT");
    }
    public String getAwsRegion() {
        return requireNonEmpty(awsRegion, "AWS_REGION");
    }
    public String getDynamodbAccessKey() {
        return requireNonEmpty(dynamodbAccessKey, "DYNAMODB_ACCESS_KEY");
    }
    public String getDynamodbSecretKey() {
        return requireNonEmpty(dynamodbSecretKey, "DYNAMODB_SECRET_KEY");
    }
    public String getDynamodbSessionToken() {
        return requireNonEmpty(dynamodbSessionToken, "DYNAMODB_SESSION_TOKEN");
    }
    public String getDynamodbSessionToken1() {
        return dynamodbSessionToken;
    }

    private String requireNonEmpty(String value, String name) {
        if (value == null || value.trim().isEmpty() || value.startsWith("${")) {
            System.err.println("FATAL: Required environment variable for " + name + " is not set.");
            System.exit(1);
        }
        return value;
    }
}
