package com.hotel.service;

import com.hotel.dto.HotelDTO;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.model.Hotel;
import com.hotel.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
    }

    public Hotel createHotel(HotelDTO hotelDTO) {
        Hotel hotel = new Hotel();
        hotel.setName(hotelDTO.getName());
        hotel.setLocation(hotelDTO.getLocation());
        hotel.setStars(hotelDTO.getStars());
        return hotelRepository.save(hotel);
    }

    public Hotel updateHotel(Long id, HotelDTO hotelDTO) {
        Hotel hotel = getHotelById(id);
        if (hotelDTO.getName() != null) hotel.setName(hotelDTO.getName());
        if (hotelDTO.getLocation() != null) hotel.setLocation(hotelDTO.getLocation());
        if (hotelDTO.getStars() != null) hotel.setStars(hotelDTO.getStars());
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        Hotel hotel = getHotelById(id);
        hotelRepository.delete(hotel);
    }
}