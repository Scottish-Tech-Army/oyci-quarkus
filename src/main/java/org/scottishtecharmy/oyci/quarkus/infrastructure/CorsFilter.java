package org.scottishtecharmy.oyci.quarkus.infrastructure;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Handles CORS for the frontend. Origin is configurable via cors.allowed-origin in application.properties.
 * PreMatching ensures OPTIONS preflight requests are intercepted before resource matching.
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    @ConfigProperty(name = "cors.allowed-origin", defaultValue = "http://localhost:5173")
    String allowedOrigin;

    @Override
    public void filter(ContainerRequestContext request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            request.abortWith(Response.ok()
                    .header("Access-Control-Allow-Origin", allowedOrigin)
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization")
                    .header("Access-Control-Max-Age", "86400")
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!response.getHeaders().containsKey("Access-Control-Allow-Origin")) {
            response.getHeaders().add("Access-Control-Allow-Origin", allowedOrigin);
            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
        }
    }
}
