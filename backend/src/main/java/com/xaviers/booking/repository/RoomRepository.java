package com.xaviers.booking.repository;

import com.xaviers.booking.model.Building;
import com.xaviers.booking.model.Room;
import com.xaviers.booking.model.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select r from Room r where
        (:building is null or r.building = :building)
        and (:floor is null or r.floor = :floor)
        and (:type is null or r.roomType = :type)
        and (:minCapacity is null or r.roomType = com.xaviers.booking.model.RoomType.COURT or r.capacity >= :minCapacity)
        and (:search is null or lower(r.name) like lower(concat('%', :search, '%')))
        """)
    Page<Room> search(
        @Param("building") Building building,
        @Param("floor") String floor,
        @Param("type") RoomType type,
        @Param("minCapacity") Integer minCapacity,
        @Param("search") String search,
        Pageable pageable);
}
