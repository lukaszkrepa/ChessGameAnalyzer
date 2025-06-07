package com.chess.analyzer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CognitoLogoutHandler cognitoLogoutHandler = new CognitoLogoutHandler();

        http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/pingpong").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/private", true)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()) //
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(cognitoLogoutHandler)
                );

        return http.build();
    }
}
