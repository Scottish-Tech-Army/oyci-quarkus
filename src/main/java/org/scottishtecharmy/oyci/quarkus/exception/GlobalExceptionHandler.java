package org.scottishtecharmy.oyci.quarkus.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Exception caught: " + exception.getClass().getName(), exception);

        if (exception instanceof ResourceNotFoundException) {
            return handleResourceNotFoundException((ResourceNotFoundException) exception);
        }

        if (exception instanceof BusinessValidationException) {
            return handleBusinessValidationException((BusinessValidationException) exception);
        }

        if (exception instanceof ConstraintViolationException) {
            return handleConstraintViolationException((ConstraintViolationException) exception);
        }

        if (exception instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) exception);
        }

        return handleGenericException(exception);
    }

    private Response handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                Response.Status.NOT_FOUND.getStatusCode(),
                "Resource Not Found",
                ex.getMessage(),
                getPath()
        );
        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }

    private Response handleBusinessValidationException(BusinessValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Business Validation Error",
                ex.getMessage(),
                getPath()
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

    private Response handleConstraintViolationException(ConstraintViolationException ex) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse.ValidationError(
                        getFieldName(violation),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        errorResponse.setError("Validation Failed");
        errorResponse.setMessage("Invalid input parameters");
        errorResponse.setPath(getPath());
        errorResponse.setValidationErrors(validationErrors);

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

    private Response handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Invalid Request",
                ex.getMessage(),
                getPath()
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

    private Response handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                getPath()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }

    private String getPath() {
        return uriInfo != null ? uriInfo.getPath() : "unknown";
    }

    private String getFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}

