package com.hotel.repository;

import com.hotel.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE " +
            "p.booking.room.hotel.id = :hotelId AND p.status = 'COMPLETED'")
    Double getTotalRevenueByHotelId(@Param("hotelId") Long hotelId);
}