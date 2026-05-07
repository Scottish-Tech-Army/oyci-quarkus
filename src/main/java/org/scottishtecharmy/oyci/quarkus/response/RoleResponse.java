package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long roleId;
    private String name;
    private String roleType;

    public static RoleResponse from(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleResponse(
                role.getRoleId(),
                role.getName(),
                role.getRoleType()
        );
    }
}

