package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RoomDTO {
    private Long id;

    @NotBlank(message = "Room number is required")
    private String number;

    @NotBlank(message = "Room type is required")
    private String type;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price must be positive")
    private Double pricePerNight;

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    public RoomDTO() {}

    public RoomDTO(String number, String type, Double pricePerNight, Long hotelId) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.hotelId = hotelId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(Double pricePerNight) { this.pricePerNight = pricePerNight; }
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
}