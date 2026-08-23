package com.xaviers.booking.controller;

import com.xaviers.booking.dto.BookingRequestDto;
import com.xaviers.booking.model.User;
import com.xaviers.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @Valid @RequestBody BookingRequestDto req) {
        return ResponseEntity.ok(bookingService.create(user, req));
    }

    @GetMapping("/me")
    public ResponseEntity<?> myBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.myBookings(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        bookingService.cancel(user, id);
        return ResponseEntity.ok(Map.of("message", "Cancelled"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('TEACHER','SUPERADMIN')")
    public ResponseEntity<?> pending() {
        return ResponseEntity.ok(bookingService.pendingQueue());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('TEACHER','SUPERADMIN')")
    public ResponseEntity<?> approve(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.approve(user, id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TEACHER','SUPERADMIN')")
    public ResponseEntity<?> reject(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(bookingService.reject(user, id, reason));
    }
}
