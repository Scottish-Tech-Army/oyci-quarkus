package org.scottishtecharmy.oyci.quarkus.scheduling.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.oyci.quarkus.scheduling.domain.SchedulePeriod;
import org.scottishtecharmy.oyci.quarkus.scheduling.domain.SchedulePeriodStatus;

import java.util.List;

@Path("/schedule-periods")
@Produces(MediaType.APPLICATION_JSON)
public class SchedulePeriodResource {

    @GET
    public List<SchedulePeriod> getSchedulePeriods() {
        return List.of(
                new SchedulePeriod("period-2026-spring", "Spring 2026", "SPRING", 2026, "2026-01-12", "2026-04-30", SchedulePeriodStatus.ACTIVE),
                new SchedulePeriod("period-2026-summer", "Summer 2026", "SUMMER", 2026, "2026-05-01", "2026-08-31", SchedulePeriodStatus.DRAFT)
        );
    }
}
