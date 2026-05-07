package org.scottishtecharmy.oyci.quarkus;

import io.smallrye.jwt.build.Jwt;

import java.util.Set;

public class TestTokens {

    public static String adminToken() {
        return Jwt.issuer("https://oyci.scottishtecharmy.org")
                .subject("1")
                .groups(Set.of("ADMIN"))
                .claim("name", "System Admin")
                .claim("email", "admin@oyci.org")
                .sign();
    }

    public static String staffToken(Long staffId) {
        return Jwt.issuer("https://oyci.scottishtecharmy.org")
                .subject(String.valueOf(staffId))
                .groups(Set.of("STAFF"))
                .claim("name", "Test Staff")
                .claim("email", "staff@oyci.org")
                .sign();
    }

    public static String participantToken(Long participantId) {
        return Jwt.issuer("https://oyci.scottishtecharmy.org")
                .subject(String.valueOf(participantId))
                .groups(Set.of("PARTICIPANT"))
                .claim("name", "Test Participant")
                .claim("email", "participant@oyci.org")
                .sign();
    }
}
