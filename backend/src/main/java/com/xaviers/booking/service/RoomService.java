package com.xaviers.booking.service;

import com.xaviers.booking.dto.RoomAvailabilityDto;
import com.xaviers.booking.model.*;
import com.xaviers.booking.repository.BookingRepository;
import com.xaviers.booking.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class RoomService {
    private final RoomRepository roomRepo;
    private final BookingRepository bookingRepo;

    public static final List<LocalTime> SLOTS = List.of(
            LocalTime.of(7,0), LocalTime.of(8,0), LocalTime.of(9,0),
            LocalTime.of(10,0), LocalTime.of(11,0), LocalTime.of(12,0),
            LocalTime.of(13,0), LocalTime.of(14,0), LocalTime.of(15,0),
            LocalTime.of(16,0), LocalTime.of(17,0), LocalTime.of(18,0),
            LocalTime.of(19,0)
    );

    public RoomService(RoomRepository roomRepo, BookingRepository bookingRepo) {
        this.roomRepo = roomRepo; this.bookingRepo = bookingRepo;
    }

    public Page<Room> search(Building building, String floor, RoomType type, String search, Pageable pageable) {
        return roomRepo.search(building, floor, type, search, pageable);
    }

    public List<Room> findByBuilding(Building building) {
        return roomRepo.search(building, null, null, null, Pageable.unpaged()).getContent();
    }

    public RoomAvailabilityDto availability(Long roomId, LocalDate date) {
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
        List<Booking> approved = bookingRepo.findByRoomIdAndBookingDateAndStatus(roomId, date, BookingStatus.APPROVED);
        Map<LocalTime, Booking> map = new HashMap<>();
        for (Booking b : approved) map.put(b.getStartTime(), b);
        List<RoomAvailabilityDto.SlotDto> slots = new ArrayList<>();
        for (LocalTime t : SLOTS) {
            Booking b = map.get(t);
            if (b != null) {
                slots.add(new RoomAvailabilityDto.SlotDto(t.toString(), "BOOKED", b.getUser().getName(), b.getUser().getRole().name()));
            } else {
                slots.add(new RoomAvailabilityDto.SlotDto(t.toString(), "FREE", null, null));
            }
        }
        return new RoomAvailabilityDto(roomId, room.getName(), date.toString(), slots);
    }

    public long pendingCount(Long roomId, LocalDate date, LocalTime slot) {
        return bookingRepo.countByRoomIdAndBookingDateAndStartTimeAndStatusIn(
                roomId, date, slot, List.of(BookingStatus.PENDING));
    }
}
