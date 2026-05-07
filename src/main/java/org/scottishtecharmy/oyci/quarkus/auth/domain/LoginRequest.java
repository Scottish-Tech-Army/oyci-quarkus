package org.scottishtecharmy.oyci.quarkus.auth.domain;

public record LoginRequest(
        String username,
        String role
) {
}
