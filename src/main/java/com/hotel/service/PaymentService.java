package com.hotel.service;

import com.hotel.dto.PaymentDTO;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.model.Booking;
import com.hotel.model.Payment;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Transactional
    public Payment createPayment(PaymentDTO paymentDTO) {
        Booking booking = bookingRepository.findById(paymentDTO.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + paymentDTO.getBookingId()));

        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setMethod(paymentDTO.getMethod());
        payment.setBooking(booking);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment confirmPayment(Long paymentId, String transactionId) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus("COMPLETED");
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());

        // Активируем связанную бронь
        Booking booking = payment.getBooking();
        booking.setStatus("CONFIRMED");

        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }
}