package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.StaffType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffTypeResponse {

    private Long staffTypeId;
    private String name;
    private String description;

    public static StaffTypeResponse from(StaffType staffType) {
        return new StaffTypeResponse(
                staffType.getStaffTypeId(),
                staffType.getName(),
                staffType.getDescription()
        );
    }
}

