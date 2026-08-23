package com.xaviers.booking.dto;

public record BookingResponseDto(
        Long id,
        Long roomId,
        String roomName,
        String building,
        String floor,
        String roomType,
        Long userId,
        String userName,
        String userEmail,
        String userRole,
        String bookingDate,
        String startTime,
        String status,
        String purpose,
        String rejectionReason,
        Long approvedBy,
        String approvedByName
) {}
