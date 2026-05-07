package org.scottishtecharmy.oyci.quarkus.dto;

import java.util.List;

public class StaffSuitabilityDTO {

    public Long staffId;
    public String name;
    public String email;
    public String status; // "PERFECT_MATCH" or "WARNING"
    public List<String> warnings;

    public StaffSuitabilityDTO(Long staffId, String name, String email, String status, List<String> warnings) {
        this.staffId = staffId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.warnings = warnings;
    }
}

