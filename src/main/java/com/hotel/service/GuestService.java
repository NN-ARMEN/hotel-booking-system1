package com.hotel.service;

import com.hotel.dto.GuestDTO;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.model.Guest;
import com.hotel.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Guest getGuestById(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
    }

    public Guest createGuest(GuestDTO guestDTO) {
        Guest guest = new Guest();
        guest.setEmail(guestDTO.getEmail());
        guest.setFullName(guestDTO.getFullName());
        guest.setPhone(guestDTO.getPhone());
        return guestRepository.save(guest);
    }

    public Guest updateGuest(Long id, GuestDTO guestDTO) {
        Guest guest = getGuestById(id);
        if (guestDTO.getEmail() != null) guest.setEmail(guestDTO.getEmail());
        if (guestDTO.getFullName() != null) guest.setFullName(guestDTO.getFullName());
        if (guestDTO.getPhone() != null) guest.setPhone(guestDTO.getPhone());
        return guestRepository.save(guest);
    }

    public void deleteGuest(Long id) {
        Guest guest = getGuestById(id);
        guestRepository.delete(guest);
    }
}