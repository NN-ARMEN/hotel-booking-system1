package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HotelDTO {
    private Long id;

    @NotBlank(message = "Hotel name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Stars rating is required")
    private Integer stars;

    public HotelDTO() {}

    public HotelDTO(String name, String location, Integer stars) {
        this.name = name;
        this.location = location;
        this.stars = stars;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
}