package org.scottishtecharmy.oyci.quarkus.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;
import org.scottishtecharmy.oyci.quarkus.dto.LoginRequest;
import org.scottishtecharmy.oyci.quarkus.dto.LoginResponse;
import org.scottishtecharmy.oyci.quarkus.dto.RegisterRequest;
import org.scottishtecharmy.oyci.quarkus.entity.User;
import org.scottishtecharmy.oyci.quarkus.enums.Role;

import java.util.Set;

@ApplicationScoped
public class AuthService {

    public LoginResponse login(LoginRequest request) {
        User user = User.findByEmail(request.email);
        if (user == null || !verifyPassword(request.password, user.passwordHash)) {
            throw new NotAuthorizedException("Invalid credentials");
        }

        String token = Jwt.issuer("https://oyci.scottishtecharmy.org")
                .subject(String.valueOf(user.id))
                .groups(Set.of(user.role.name()))
                .claim("name", user.name)
                .claim("email", user.email)
                .sign();

        return new LoginResponse(token, user.name, user.email, user.role.name());
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (User.findByEmail(request.email) != null) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.name = request.name;
        user.email = request.email;
        user.passwordHash = hashPassword(request.password);
        user.role = Role.PARTICIPANT;
        user.dateOfBirth = request.dateOfBirth;
        user.persist();
    }

    public String hashPassword(String plaintext) {
        return BCrypt.withDefaults().hashToString(12, plaintext.toCharArray());
    }

    private boolean verifyPassword(String plaintext, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(plaintext.toCharArray(), hash);
        return result.verified;
    }
}

