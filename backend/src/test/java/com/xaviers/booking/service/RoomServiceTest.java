package com.xaviers.booking.service;

import com.xaviers.booking.model.Building;
import com.xaviers.booking.model.Room;
import com.xaviers.booking.model.RoomType;
import com.xaviers.booking.repository.BookingRepository;
import com.xaviers.booking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock RoomRepository roomRepository;
    @Mock BookingRepository bookingRepository;

    private RoomService service() {
        return new RoomService(roomRepository, bookingRepository);
    }

    @Test
    void searchDelegatesToRepository() {
        var page = new PageImpl<>(List.<Room>of());
        when(roomRepository.search(Building.MAIN, "Ground", RoomType.CLASSROOM, "LR", Pageable.unpaged())).thenReturn(page);

        assertThat(service().search(Building.MAIN, "Ground", RoomType.CLASSROOM, "LR", Pageable.unpaged())).isSameAs(page);
    }

    @Test
    void availabilityMarksApprovedSlotsAsBooked() {
        Room room = new Room();
        room.setId(1L);
        room.setName("LR01");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(1L, LocalDate.of(2026, 1, 1), com.xaviers.booking.model.BookingStatus.APPROVED))
                .thenReturn(List.of());

        var result = service().availability(1L, LocalDate.of(2026, 1, 1));

        assertThat(result.slots()).hasSize(RoomService.SLOTS.size());
        assertThat(result.slots()).allMatch(slot -> slot.status().equals("FREE"));
    }

    @Test
    void pendingCountUsesPendingStatus() {
        when(bookingRepository.countByRoomIdAndBookingDateAndStartTimeAndStatusIn(
                eq(1L), any(), eq(LocalTime.of(9, 0)), anyList())).thenReturn(3L);

        assertThat(service().pendingCount(1L, LocalDate.of(2026, 1, 1), LocalTime.of(9, 0))).isEqualTo(3L);
    }
}
