package com.xaviers.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequestDto(
        @NotNull Long roomId,
        @NotNull LocalDate bookingDate,
        @NotNull LocalTime startTime,
        String purpose
) {}
