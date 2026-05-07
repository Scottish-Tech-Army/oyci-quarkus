package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.scottishtecharmy.oyci.quarkus.model.Qualification;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualificationResponse {

    private Long qualificationId;
    private String name;
    private String description;

    public static QualificationResponse from(Qualification qualification) {
        return new QualificationResponse(
                qualification.getQualificationId(),
                qualification.getName(),
                qualification.getDescription()
        );
    }
}

