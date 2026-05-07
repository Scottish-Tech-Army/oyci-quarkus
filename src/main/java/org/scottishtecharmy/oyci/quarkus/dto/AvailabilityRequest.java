package org.scottishtecharmy.oyci.quarkus.dto;

import java.time.LocalTime;

public class AvailabilityRequest {
    public int dayOfWeek;
    public LocalTime startTime;
    public LocalTime endTime;
}

