package org.scottishtecharmy.oyci.quarkus.masterdata.application;

import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.oyci.quarkus.masterdata.domain.UnavailabilityPeriod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UnavailabilityStore {

    private final List<UnavailabilityPeriod> periods = new ArrayList<>();

    public synchronized UnavailabilityPeriod add(String staffId, String startDate, String endDate, String reason) {
        UnavailabilityPeriod period = new UnavailabilityPeriod(
                UUID.randomUUID().toString(), staffId, startDate, endDate,
                reason == null ? "" : reason
        );
        periods.add(period);
        return period;
    }

    public synchronized List<UnavailabilityPeriod> findByStaffId(String staffId) {
        return periods.stream()
                .filter(p -> p.staffId().equals(staffId))
                .sorted((a, b) -> a.startDate().compareTo(b.startDate()))
                .toList();
    }

    public synchronized boolean delete(String staffId, String id) {
        return periods.removeIf(p -> p.id().equals(id) && p.staffId().equals(staffId));
    }

    /** Returns true if the given date falls within any unavailability period for the staff member. */
    public boolean isUnavailableOn(String staffId, String datePart) {
        if (datePart == null || datePart.isBlank()) return false;
        LocalDate date;
        try {
            date = LocalDate.parse(datePart);
        } catch (Exception e) {
            return false;
        }
        return findByStaffId(staffId).stream().anyMatch(p -> {
            try {
                LocalDate start = LocalDate.parse(p.startDate());
                LocalDate end   = LocalDate.parse(p.endDate());
                return !date.isBefore(start) && !date.isAfter(end);
            } catch (Exception e) {
                return false;
            }
        });
    }
}
