package com.xaviers.booking.repository;

import com.xaviers.booking.model.Booking;
import com.xaviers.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select count(b) from Booking b where b.user.id = :userId and b.status in ('PENDING','APPROVED') and (b.bookingDate > :today or (b.bookingDate = :today and b.startTime >= :nowTime))")
    long countActiveFuture(@Param("userId") Long userId, @Param("today") LocalDate today, @Param("nowTime") LocalTime nowTime);

    boolean existsByRoomIdAndBookingDateAndStartTimeAndStatusIn(Long roomId, LocalDate date, LocalTime startTime, List<BookingStatus> statuses);

    List<Booking> findByRoomIdAndBookingDateAndStatus(Long roomId, LocalDate date, BookingStatus status);
    List<Booking> findByRoomIdAndBookingDateAndStatusIn(Long roomId, LocalDate date, List<BookingStatus> statuses);

    List<Booking> findByStatusOrderByCreatedAtAsc(BookingStatus status);

    List<Booking> findByUserIdOrderByBookingDateDescStartTimeDesc(Long userId);

    long countByRoomIdAndBookingDateAndStartTimeAndStatusIn(Long roomId, LocalDate date, LocalTime startTime, List<BookingStatus> statuses);

    @Modifying
    @Query("update Booking b set b.status = :newStatus, b.approvedBy.id = :approverId where b.id = :id and b.status = 'PENDING'")
    int approveIfPending(@Param("id") Long id, @Param("newStatus") BookingStatus newStatus, @Param("approverId") Long approverId);

    @Modifying
    @Query("update Booking b set b.status = 'REJECTED', b.rejectionReason = :reason, b.approvedBy.id = :approverId where b.id = :id and b.status = 'PENDING'")
    int rejectIfPending(@Param("id") Long id, @Param("reason") String reason, @Param("approverId") Long approverId);
}
