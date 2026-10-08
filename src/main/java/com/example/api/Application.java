package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RestController
    static class ApiController {

        @GetMapping("/api/hello")
        String sayHello(@AuthenticationPrincipal Jwt jwt) {
            return String.format("Hello, %s!", jwt.getSubject());
        }

        @GetMapping("/api/whoami")
        Map<String, Object> whoami(@AuthenticationPrincipal Jwt jwt) {
            return jwt.getClaims();
        }

        /**
         * Requires the {@code messages.read} scope in addition to a valid token. A token
         * without that scope is rejected with 403, not 401.
         */
        @GetMapping("/api/messages")
        @PreAuthorize("hasAuthority('SCOPE_messages.read')")
        Map<String, Object> messages(@AuthenticationPrincipal Jwt jwt) {
            return Map.of(
                "owner", jwt.getSubject(),
                "messages", List.of(
                    "Your API call was authorized with the messages.read scope.",
                    "This data came from the API on port 8081.",
                    "The caller proved its identity with a Bearer token."
                ));
        }
    }
}
