package com.hotel.service;

import com.hotel.dto.BookingDTO;
import com.hotel.exception.BookingConflictException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.model.Booking;
import com.hotel.model.Guest;
import com.hotel.model.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Transactional
    public Booking createBooking(BookingDTO bookingDTO) {
        Guest guest = guestRepository.findById(bookingDTO.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + bookingDTO.getGuestId()));

        Room room = roomRepository.findById(bookingDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + bookingDTO.getRoomId()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                bookingDTO.getRoomId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        if (!overlappingBookings.isEmpty()) {
            throw new BookingConflictException("Room is already booked for the selected dates");
        }

        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate()) ||
                bookingDTO.getCheckOutDate().isEqual(bookingDTO.getCheckInDate())) {
            throw new BookingConflictException("Check-out date must be after check-in date");
        }

        Booking booking = new Booking();
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setStatus("PENDING");

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBooking(Long id, BookingDTO bookingDTO) {
        Booking existingBooking = getBookingById(id);

        if (bookingDTO.getCheckInDate() != null) {
            existingBooking.setCheckInDate(bookingDTO.getCheckInDate());
        }
        if (bookingDTO.getCheckOutDate() != null) {
            existingBooking.setCheckOutDate(bookingDTO.getCheckOutDate());
        }
        if (bookingDTO.getStatus() != null) {
            existingBooking.setStatus(bookingDTO.getStatus());
        }

        return bookingRepository.save(existingBooking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }

    public List<Booking> getBookingsByGuestId(Long guestId) {
        return bookingRepository.findByGuestId(guestId);
    }

    // БИЗНЕС-ОПЕРАЦИИ

    // 1. Поиск доступных номеров по датам и типу
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, String roomType, Long hotelId) {
        return bookingRepository.findAvailableRooms(checkIn, checkOut, roomType, hotelId);
    }

    // 2. Отмена бронирования с возвратом платежа
    @Transactional
    public void cancelBookingWithRefund(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (!"CANCELLED".equals(booking.getStatus())) {
            booking.setStatus("CANCELLED");

            if (booking.getPayment() != null && "COMPLETED".equals(booking.getPayment().getStatus())) {
                var payment = booking.getPayment();
                payment.setStatus("REFUNDED");
                paymentRepository.save(payment);
            }

            bookingRepository.save(booking);
        }
    }

    // 3. Получение статистики по отелю
    public Map<String, Object> getHotelStatistics(Long hotelId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", roomRepository.countByHotelId(hotelId));
        stats.put("availableRooms", roomRepository.countAvailableRoomsByHotelId(hotelId));
        stats.put("totalBookings", bookingRepository.countByHotelId(hotelId));
        stats.put("activeBookings", bookingRepository.countActiveBookingsByHotelId(hotelId));
        stats.put("revenue", paymentRepository.getTotalRevenueByHotelId(hotelId));
        return stats;
    }

    // 4. Смена номера для бронирования
    @Transactional
    public Booking changeRoom(Long bookingId, Long newRoomId) {
        Booking booking = getBookingById(bookingId);
        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + newRoomId));

        List<Booking> conflicts = bookingRepository.findOverlappingBookings(
                newRoomId, booking.getCheckInDate(), booking.getCheckOutDate());

        conflicts = conflicts.stream()
                .filter(b -> !b.getId().equals(bookingId))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("New room is not available for the selected dates");
        }

        booking.setRoom(newRoom);
        return bookingRepository.save(booking);
    }

    // 5. Получение истории бронирований гостя
    public List<Booking> getGuestBookingHistory(Long guestId) {
        return bookingRepository.findByGuestIdOrderByCreatedAtDesc(guestId);
    }
}