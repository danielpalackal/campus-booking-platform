package com.xaviers.booking.controller;

import com.xaviers.booking.dto.RoomAvailabilityDto;
import com.xaviers.booking.model.Building;
import com.xaviers.booking.model.Room;
import com.xaviers.booking.model.RoomType;
import com.xaviers.booking.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) { this.roomService = roomService; }

    @GetMapping
    public Page<Room> list(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 100) Pageable pageable) {
        Building b = null;
        if (building != null && !building.isBlank()) b = Building.valueOf(building.toUpperCase());
        RoomType t = null;
        if (type != null && !type.isBlank()) t = RoomType.valueOf(type.toUpperCase());
        return roomService.search(b, floor, t, search, pageable);
    }

    @GetMapping("/{id}/availability")
    public RoomAvailabilityDto availability(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return roomService.availability(id, date);
    }

    @GetMapping("/{id}/pending-count")
    public ResponseEntity<?> pendingCount(@PathVariable Long id,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          @RequestParam String slot) {
        LocalTime lt = LocalTime.parse(slot);
        long count = roomService.pendingCount(id, date, lt);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/buildings")
    public List<String> buildings() {
        return List.of(Building.values()).stream().map(Enum::name).toList();
    }
}
