package com.hotel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "rooms"})
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    private Integer stars;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    public Hotel() {}

    public Hotel(String name, String location, Integer stars) {
        this.name = name;
        this.location = location;
        this.stars = stars;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public Integer getStars() { return stars; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<Room> getRooms() { return rooms; }

    // Сеттеры (добавьте эти методы)
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setStars(Integer stars) { this.stars = stars; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
}