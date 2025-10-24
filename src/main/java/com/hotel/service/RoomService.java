package com.hotel.service;

import com.hotel.dto.RoomDTO;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public Room createRoom(RoomDTO roomDTO) {
        System.out.println("=== Creating room for hotel ID: " + roomDTO.getHotelId());

        // НАЙТИ ОТЕЛЬ!
        Hotel hotel = hotelRepository.findById(roomDTO.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + roomDTO.getHotelId()));

        System.out.println("Hotel found: " + hotel.getName());

        Room room = new Room();
        room.setNumber(roomDTO.getNumber());
        room.setType(roomDTO.getType());
        room.setPricePerNight(roomDTO.getPricePerNight());
        room.setHotel(hotel);  // ← ВАЖНО: установить отель!
        room.setIsAvailable(true);

        System.out.println("Saving room...");
        Room savedRoom = roomRepository.save(room);
        System.out.println("=== Room created successfully ===");
        return savedRoom;
    }

    public Room updateRoom(Long id, RoomDTO roomDTO) {
        Room room = getRoomById(id);
        if (roomDTO.getNumber() != null) room.setNumber(roomDTO.getNumber());
        if (roomDTO.getType() != null) room.setType(roomDTO.getType());
        if (roomDTO.getPricePerNight() != null) room.setPricePerNight(roomDTO.getPricePerNight());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }
}