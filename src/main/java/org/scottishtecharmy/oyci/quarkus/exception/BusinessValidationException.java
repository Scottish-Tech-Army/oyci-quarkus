package org.scottishtecharmy.oyci.quarkus.exception;

public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}

