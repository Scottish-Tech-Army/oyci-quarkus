package org.scottishtecharmy.oyci.quarkus.auth.domain;

public record LoginResponse(
        String username,
        String role,
        String displayName
) {
}
