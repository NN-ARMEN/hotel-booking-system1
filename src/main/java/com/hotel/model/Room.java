package com.hotel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hotel_id", "number"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hotel", "bookings"})
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String type;

    @Column(name = "price_per_night", nullable = false)
    private Double pricePerNight;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public Room() {}

    public Room(String number, String type, Double pricePerNight, Hotel hotel) {
        this.number = number;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.hotel = hotel;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getNumber() { return number; }
    public String getType() { return type; }
    public Double getPricePerNight() { return pricePerNight; }
    public Boolean getIsAvailable() { return isAvailable; }
    public Hotel getHotel() { return hotel; }
    public List<Booking> getBookings() { return bookings; }

    // Сеттеры (добавьте эти методы)
    public void setId(Long id) { this.id = id; }
    public void setNumber(String number) { this.number = number; }
    public void setType(String type) { this.type = type; }
    public void setPricePerNight(Double pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}