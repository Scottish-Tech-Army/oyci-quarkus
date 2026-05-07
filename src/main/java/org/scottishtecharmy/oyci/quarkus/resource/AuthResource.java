package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.dto.LoginRequest;
import org.scottishtecharmy.oyci.quarkus.dto.LoginResponse;
import org.scottishtecharmy.oyci.quarkus.dto.RegisterRequest;
import org.scottishtecharmy.oyci.quarkus.service.AuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public LoginResponse login(LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        authService.register(request);
        return Response.status(Response.Status.CREATED).build();
    }
}

