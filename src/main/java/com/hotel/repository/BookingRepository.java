package com.hotel.repository;

import com.hotel.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestId(Long guestId);
    List<Booking> findByRoomId(Long roomId);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND ((b.checkInDate <= :checkInDate AND b.checkOutDate > :checkInDate) " +
            "OR (b.checkInDate < :checkOutDate AND b.checkOutDate >= :checkOutDate) " +
            "OR (b.checkInDate >= :checkInDate AND b.checkOutDate <= :checkOutDate))")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                                          @Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);
}