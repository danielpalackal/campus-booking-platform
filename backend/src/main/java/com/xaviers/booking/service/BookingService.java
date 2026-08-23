package com.xaviers.booking.service;

import com.xaviers.booking.dto.BookingRequestDto;
import com.xaviers.booking.dto.BookingResponseDto;
import com.xaviers.booking.exception.*;
import com.xaviers.booking.model.*;
import com.xaviers.booking.repository.BookingRepository;
import com.xaviers.booking.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final EmailService emailService;
    @PersistenceContext
    private EntityManager entityManager;
    private EntityManager getEntityManager() { return entityManager; }

    public BookingService(BookingRepository bookingRepo, RoomRepository roomRepo, EmailService emailService) {
        this.bookingRepo = bookingRepo; this.roomRepo = roomRepo; this.emailService = emailService;
    }

    private BookingResponseDto toDto(Booking b) {
        return new BookingResponseDto(
                b.getId(), b.getRoom().getId(), b.getRoom().getName(),
                b.getRoom().getBuilding().name(), b.getRoom().getFloor(),
                b.getRoom().getRoomType().name(),
                b.getUser().getId(), b.getUser().getName(), b.getUser().getEmail(), b.getUser().getRole().name(),
                b.getBookingDate().toString(), b.getStartTime().toString(),
                b.getStatus().name(), b.getPurpose(), b.getRejectionReason(),
                b.getApprovedBy() != null ? b.getApprovedBy().getId() : null,
                b.getApprovedBy() != null ? b.getApprovedBy().getName() : null
        );
    }

    @Transactional
    public BookingResponseDto create(User user, BookingRequestDto req) {
        Room room = roomRepo.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        if (!RoomService.SLOTS.contains(req.startTime())) {
            throw new IllegalArgumentException("Invalid slot. Valid slots are 07:00-19:00 hourly");
        }

        if (room.getRoomType() == RoomType.LAB && user.getRole() == Role.STUDENT) {
            throw new UnauthorizedRoleException("Students cannot book labs");
        }

        ZoneId ist = ZoneId.of("Asia/Kolkata");
        ZonedDateTime now = ZonedDateTime.now(ist);
        LocalDateTime slotDateTime = LocalDateTime.of(req.bookingDate(), req.startTime());
        ZonedDateTime slotZdt = slotDateTime.atZone(ist);
        if (slotZdt.isBefore(now)) throw new IllegalArgumentException("Cannot book a past slot");
        if (slotZdt.isAfter(now.plusHours(48))) throw new IllegalArgumentException("Can only book up to 48 hours in advance");

        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();
        long active = bookingRepo.countActiveFuture(user.getId(), today, nowTime);
        if (active >= 5) throw new BookingCapExceededException("Booking limit reached (max 5 active future bookings)");

        boolean conflict = bookingRepo.existsByRoomIdAndBookingDateAndStartTimeAndStatusIn(
                room.getId(), req.bookingDate(), req.startTime(), List.of(BookingStatus.PENDING, BookingStatus.APPROVED));
        if (conflict) throw new SlotUnavailableException("Slot already booked");

        Booking b = new Booking();
        b.setRoom(room);
        b.setUser(user);
        b.setBookingDate(req.bookingDate());
        b.setStartTime(req.startTime());
        b.setPurpose(req.purpose());

        boolean instantApprove = false;
        if (room.getRoomType() == RoomType.COURT) instantApprove = true;
        else if (room.getRoomType() == RoomType.LAB) instantApprove = true;
        else if (room.getRoomType() == RoomType.CLASSROOM && (user.getRole() == Role.TEACHER || user.getRole() == Role.SUPERADMIN)) instantApprove = true;

        if (instantApprove) {
            b.setStatus(BookingStatus.APPROVED);
            b.setApprovedBy(user);
        } else {
            b.setStatus(BookingStatus.PENDING);
        }

        b = bookingRepo.save(b);

        if (b.getStatus() == BookingStatus.PENDING) {
            emailService.send(user.getEmail(), "Booking Pending — " + room.getName(),
                    "Your request for " + room.getName() + " on " + req.bookingDate() + " at " + req.startTime() + " is pending teacher approval.");
        } else {
            emailService.send(user.getEmail(), "Booking Confirmed — " + room.getName(),
                    "Your booking for " + room.getName() + " on " + req.bookingDate() + " at " + req.startTime() + " is confirmed.");
        }
        return toDto(b);
    }

    public List<BookingResponseDto> myBookings(User user) {
        return bookingRepo.findByUserIdOrderByBookingDateDescStartTimeDesc(user.getId()).stream().map(this::toDto).toList();
    }

    public List<BookingResponseDto> pendingQueue() {
        return bookingRepo.findByStatusOrderByCreatedAtAsc(BookingStatus.PENDING).stream().map(this::toDto).toList();
    }

    @Transactional
    public BookingResponseDto approve(User teacher, Long bookingId) {
        int updated = bookingRepo.approveIfPending(bookingId, BookingStatus.APPROVED, teacher.getId());
        if (updated == 0) {
            Booking existing = bookingRepo.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
            if (existing.getStatus() != BookingStatus.PENDING) throw new SlotUnavailableException("Booking already handled by another teacher");
            throw new SlotUnavailableException("Booking already handled");
        }
        bookingRepo.flush();
        jakarta.persistence.EntityManager em = getEntityManager();
        em.clear();
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        emailService.send(b.getUser().getEmail(), "Booking Approved — " + b.getRoom().getName(),
                "Your booking for " + b.getRoom().getName() + " on " + b.getBookingDate() + " at " + b.getStartTime() + " has been approved by " + teacher.getName() + ".");
        return toDto(b);
    }

    @Transactional
    public BookingResponseDto reject(User teacher, Long bookingId, String reason) {
        int updated = bookingRepo.rejectIfPending(bookingId, reason, teacher.getId());
        if (updated == 0) throw new SlotUnavailableException("Booking already handled by another teacher");
        bookingRepo.flush();
        getEntityManager().clear();
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        String body = "Your booking for " + b.getRoom().getName() + " on " + b.getBookingDate() + " at " + b.getStartTime() + " was rejected by " + teacher.getName() + ".";
        if (reason != null && !reason.isBlank()) body += "\nReason: " + reason;
        emailService.send(b.getUser().getEmail(), "Booking Rejected — " + b.getRoom().getName(), body);
        return toDto(b);
    }

    @Transactional
    public void cancel(User user, Long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        boolean isOwner = b.getUser().getId().equals(user.getId());
        boolean isSuper = user.getRole() == Role.SUPERADMIN;
        if (!isOwner && !isSuper) throw new UnauthorizedRoleException("Can only cancel own bookings");
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED) throw new IllegalArgumentException("Booking already cancelled/rejected");
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        LocalDateTime slot = LocalDateTime.of(b.getBookingDate(), b.getStartTime());
        if (!slot.atZone(ist).isAfter(ZonedDateTime.now(ist))) throw new IllegalArgumentException("Cannot cancel a slot that has already started");
        b.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(b);
        emailService.send(b.getUser().getEmail(), "Booking Cancelled — " + b.getRoom().getName(),
                "Your booking for " + b.getRoom().getName() + " on " + b.getBookingDate() + " at " + b.getStartTime() + " has been cancelled.");
        if (b.getApprovedBy() != null && !b.getApprovedBy().getId().equals(user.getId())) {
            emailService.send(b.getApprovedBy().getEmail(), "Booking Cancelled — " + b.getRoom().getName(),
                    b.getUser().getName() + " cancelled their booking for " + b.getRoom().getName() + " on " + b.getBookingDate() + " at " + b.getStartTime() + ".");
        }
    }
}
