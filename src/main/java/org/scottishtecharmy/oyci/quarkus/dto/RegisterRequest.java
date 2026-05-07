package org.scottishtecharmy.oyci.quarkus.dto;

import java.time.LocalDate;

public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public LocalDate dateOfBirth;
}

