package com.hotel.repository;

import com.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    List<Room> findByTypeAndIsAvailableTrue(String type);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.isAvailable = true")
    List<Room> findAvailableRoomsByHotelId(@Param("hotelId") Long hotelId);

    Long countByHotelId(Long hotelId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.isAvailable = true")
    Long countAvailableRoomsByHotelId(@Param("hotelId") Long hotelId);
}