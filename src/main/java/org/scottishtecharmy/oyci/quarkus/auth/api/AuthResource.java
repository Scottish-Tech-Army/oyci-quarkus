package org.scottishtecharmy.oyci.quarkus.auth.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.auth.domain.LoginRequest;
import org.scottishtecharmy.oyci.quarkus.auth.domain.LoginResponse;
import org.scottishtecharmy.oyci.quarkus.models.ErrorResponse;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    // Known staff accounts: username (lowercase) → display name
    private static final Map<String, String> STAFF_ACCOUNTS = Map.of(
            "aileen", "Aileen Campbell",
            "gregor", "Gregor Macleod"
    );

    // Known admin accounts: username (lowercase) → display name
    private static final Map<String, String> ADMIN_ACCOUNTS = Map.of(
            "admin", "Administrator"
    );

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.role())) {
            return badRequest("username and role are required");
        }

        String username = request.username().trim().toLowerCase();
        String role = request.role().trim().toLowerCase();

        return switch (role) {
            case "staff" -> authenticateStaff(username, request.username().trim());
            case "admin" -> authenticateAdmin(username, request.username().trim());
            default -> badRequest("role must be 'admin' or 'staff'");
        };
    }

    private Response authenticateStaff(String usernameLower, String usernameOriginal) {
        String displayName = STAFF_ACCOUNTS.get(usernameLower);
        if (displayName == null) {
            return unauthorized("No staff account found for username '" + usernameOriginal + "'. Try: aileen or gregor");
        }
        return Response.ok(new LoginResponse(usernameOriginal, "staff", displayName)).build();
    }

    private Response authenticateAdmin(String usernameLower, String usernameOriginal) {
        String displayName = ADMIN_ACCOUNTS.getOrDefault(usernameLower, toTitleCase(usernameOriginal) + " (Admin)");
        return Response.ok(new LoginResponse(usernameOriginal, "admin", displayName)).build();
    }

    private static String toTitleCase(String value) {
        if (value == null || value.isBlank()) return value;
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(message))
                .build();
    }

    private static Response unauthorized(String message) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(message))
                .build();
    }
}
