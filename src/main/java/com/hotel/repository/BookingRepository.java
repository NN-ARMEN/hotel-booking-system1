package com.hotel.repository;

import com.hotel.model.Booking;
import com.hotel.model.Room;  // Добавить этот импорт
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

    // Метод для поиска доступных номеров
    @Query("SELECT r FROM Room r WHERE r.isAvailable = true " +
            "AND (:roomType IS NULL OR r.type = :roomType) " +
            "AND (:hotelId IS NULL OR r.hotel.id = :hotelId) " +
            "AND r.id NOT IN (SELECT b.room.id FROM Booking b WHERE " +
            "b.status IN ('PENDING', 'CONFIRMED') AND " +
            "((b.checkInDate <= :checkIn AND b.checkOutDate > :checkIn) OR " +
            "(b.checkInDate < :checkOut AND b.checkOutDate >= :checkOut) OR " +
            "(b.checkInDate >= :checkIn AND b.checkOutDate <= :checkOut)))")
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("roomType") String roomType,
                                  @Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId")
    Long countByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.id = :hotelId AND b.status IN ('PENDING', 'CONFIRMED')")
    Long countActiveBookingsByHotelId(@Param("hotelId") Long hotelId);

    List<Booking> findByGuestIdOrderByCreatedAtDesc(Long guestId);
}