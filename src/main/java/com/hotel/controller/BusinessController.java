package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BookingService bookingService;

    // 1. Поиск доступных номеров
    @GetMapping("/rooms/available")
    public List<Room> findAvailableRooms(
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Long hotelId) {
        return bookingService.findAvailableRooms(checkIn, checkOut, roomType, hotelId);
    }

    // 2. Отмена бронирования с возвратом
    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Void> cancelBookingWithRefund(@PathVariable Long id) {
        bookingService.cancelBookingWithRefund(id);
        return ResponseEntity.ok().build();
    }

    // 3. Статистика отеля
    @GetMapping("/hotels/{id}/statistics")
    public Map<String, Object> getHotelStatistics(@PathVariable Long id) {
        return bookingService.getHotelStatistics(id);
    }

    // 4. Смена номера
    @PutMapping("/bookings/{id}/change-room")
    public Booking changeRoom(@PathVariable Long id, @RequestParam Long newRoomId) {
        return bookingService.changeRoom(id, newRoomId);
    }

    // 5. История бронирований гостя
    @GetMapping("/guests/{id}/booking-history")
    public List<Booking> getGuestBookingHistory(@PathVariable Long id) {
        return bookingService.getGuestBookingHistory(id);
    }
}