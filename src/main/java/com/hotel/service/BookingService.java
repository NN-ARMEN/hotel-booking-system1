package com.hotel.service;

import com.hotel.dto.BookingDTO;
import com.hotel.exception.BookingConflictException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.model.Booking;
import com.hotel.model.Guest;
import com.hotel.model.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Transactional
    public Booking createBooking(BookingDTO bookingDTO) {
        // Проверка существования гостя и номера
        Guest guest = guestRepository.findById(bookingDTO.getGuestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + bookingDTO.getGuestId()));

        Room room = roomRepository.findById(bookingDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + bookingDTO.getRoomId()));

        // Проверка доступности номера на указанные даты
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                bookingDTO.getRoomId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        if (!overlappingBookings.isEmpty()) {
            throw new BookingConflictException("Room is already booked for the selected dates");
        }

        // Проверка корректности дат
        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate()) ||
                bookingDTO.getCheckOutDate().isEqual(bookingDTO.getCheckInDate())) {
            throw new BookingConflictException("Check-out date must be after check-in date");
        }

        // Создание бронирования
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

        // Обновление полей
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
}
