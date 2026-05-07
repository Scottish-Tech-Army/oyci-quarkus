package org.scottishtecharmy.oyci.quarkus.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for staff availability time slots
 * Parses time from preferred shift strings like "Morning 8:30am - 3:30pm"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private String startTime;
    private String endTime;

    /**
     * Creates AvailabilityResponse from a preferred shift time string
     * Example: "Morning 8:30am - 3:30pm" -> {startTime: "08:30am", endTime: "03:30pm"}
     * Example: "Full Day 8:30am - 9:30pm" -> {startTime: "08:30am", endTime: "09:30pm"}
     */
    public static AvailabilityResponse fromShiftTime(String shiftTime) {
        if (shiftTime == null || shiftTime.trim().isEmpty()) {
            return null;
        }

        try {
            // Split by " - " to get start and end times
            // Example: "Morning 8:30am - 3:30pm" -> ["Morning 8:30am", "3:30pm"]
            String[] parts = shiftTime.split(" - ");
            if (parts.length != 2) {
                return null;
            }

            // Extract end time (already clean)
            String endTime = parts[1].trim();

            // Extract start time (remove the shift name prefix)
            // "Morning 8:30am" -> find the time part after the last space
            String firstPart = parts[0].trim();
            String[] firstPartSplit = firstPart.split("\\s+");
            String startTime = firstPartSplit[firstPartSplit.length - 1]; // Get last word (the time)

            return new AvailabilityResponse(startTime, endTime);
        } catch (Exception e) {
            // If parsing fails, return null
            return null;
        }
    }
}

