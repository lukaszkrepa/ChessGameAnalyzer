package com.chess.analyzer.backend.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {

    private final OAuth2AuthorizedClientService clientService;

    public TestAuthController(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/private")
    public String privateEndpoint(@AuthenticationPrincipal Object principal) {
        if (principal instanceof OidcUser oidcUser) {
            return "Hello (OIDC), " + oidcUser.getEmail();
        } else if (principal instanceof Jwt jwt) {
            return "Hello (JWT), " + jwt.getClaimAsString("username");
        } else {
            return "Hello, anonymous";
        }
    }

    @GetMapping("/token")
    public String getAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        return "Access Token: " + accessToken;
    }
}
