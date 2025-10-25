-- Clear existing data (optional)
DELETE FROM payments;
DELETE FROM bookings;
DELETE FROM rooms;
DELETE FROM guests;
DELETE FROM hotels;

-- Insert sample hotels
INSERT INTO hotels (id, name, location, stars, created_at) VALUES
(1, 'Grand Hotel', 'Moscow', 5, CURRENT_TIMESTAMP),
(2, 'Beach Resort', 'Sochi', 4, CURRENT_TIMESTAMP);

-- Insert sample rooms
INSERT INTO rooms (id, number, type, price_per_night, is_available, hotel_id) VALUES
(1, '101', 'DELUXE', 200.0, true, 1),
(2, '102', 'STANDARD', 120.0, true, 1),
(3, '201', 'SUITE', 300.0, true, 2),
(4, '202', 'STANDARD', 150.0, true, 2);

-- Insert sample guests
INSERT INTO guests (id, email, full_name, phone, created_at) VALUES
(1, 'armen@petrosyan.com', 'Armen Petrosyan', '+79263880006', CURRENT_TIMESTAMP),
(2, 'ivan@ivanov.com', 'Ivan Ivanova', '+79167778899', CURRENT_TIMESTAMP);

-- Insert sample bookings
INSERT INTO bookings (id, check_in_date, check_out_date, status, created_at, guest_id, room_id) VALUES
(1, '2025-11-10', '2025-11-15', 'CONFIRMED', CURRENT_TIMESTAMP, 1, 1),
(2, '2025-12-01', '2025-12-07', 'CONFIRMED', CURRENT_TIMESTAMP, 1, 3),
(3, '2026-02-01', '2026-02-04', 'CONFIRMED', CURRENT_TIMESTAMP, 2, 2),
(4, '2026-03-10', '2026-03-12', 'CONFIRMED', CURRENT_TIMESTAMP, 2, 4);

-- Insert sample payments
INSERT INTO payments (id, amount, method, status, transaction_id, paid_at, booking_id) VALUES
(1, 1000.0, 'CARD', 'COMPLETED', 'txn_11111', CURRENT_TIMESTAMP, 1),
(2, 1800.0, 'CASH', 'COMPLETED', 'txn_22222', CURRENT_TIMESTAMP, 2),
(3, 360.0, 'CARD', 'COMPLETED', 'txn_33333', CURRENT_TIMESTAMP, 3),
(4, 300.0, 'CASH', 'COMPLETED', 'txn_44444', CURRENT_TIMESTAMP, 4);