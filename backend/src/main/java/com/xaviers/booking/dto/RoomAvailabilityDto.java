package com.xaviers.booking.dto;

import java.util.List;

public record RoomAvailabilityDto(
        Long roomId,
        String roomName,
        String date,
        List<SlotDto> slots
) {
    public record SlotDto(String startTime, String status, String bookedBy, String bookedByRole) {}
}
