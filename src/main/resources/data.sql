-- Отели
INSERT INTO hotels (name, location, stars, created_at) VALUES ('Grand Hotel', 'Moscow', 5, CURRENT_TIMESTAMP);
INSERT INTO hotels (name, location, stars, created_at) VALUES ('Beach Resort', 'Sochi', 4, CURRENT_TIMESTAMP);
INSERT INTO hotels (name, location, stars, created_at) VALUES ('City Hotel', 'Saint Petersburg', 3, CURRENT_TIMESTAMP);

-- Номера для Grand Hotel
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('101', 'DELUXE', 200.0, true, 1);
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('102', 'STANDARD', 120.0, true, 1);
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('103', 'SUITE', 350.0, true, 1);
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('201', 'DELUXE', 180.0, true, 1);

-- Номера для Beach Resort
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('301', 'SUITE', 400.0, true, 2);
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('302', 'DELUXE', 250.0, true, 2);
INSERT INTO rooms (number, type, price_per_night, is_available, hotel_id) VALUES ('303', 'STANDARD', 150.0, true, 2);

-- Гости
INSERT INTO guests (email, full_name, phone, created_at) VALUES ('ivan.ivanov@example.com', 'Ivan Ivanov', '+79161234567', CURRENT_TIMESTAMP);
INSERT INTO guests (email, full_name, phone, created_at) VALUES ('maria.petrova@example.com', 'Maria Petrova', '+79167654321', CURRENT_TIMESTAMP);
INSERT INTO guests (email, full_name, phone, created_at) VALUES ('alex.smirnov@example.com', 'Alex Smirnov', '+79169998877', CURRENT_TIMESTAMP);

-- Бронирования
INSERT INTO bookings (check_in_date, check_out_date, status, created_at, guest_id, room_id) VALUES ('2024-12-01', '2024-12-05', 'CONFIRMED', CURRENT_TIMESTAMP, 1, 1);
INSERT INTO bookings (check_in_date, check_out_date, status, created_at, guest_id, room_id) VALUES ('2024-12-10', '2024-12-15', 'PENDING', CURRENT_TIMESTAMP, 2, 3);

-- Платежи
INSERT INTO payments (amount, method, status, transaction_id, paid_at, booking_id) VALUES (1000.0, 'CARD', 'COMPLETED', 'txn_12345', CURRENT_TIMESTAMP, 1);
INSERT INTO payments (amount, method, status, booking_id) VALUES (1200.0, 'CASH', 'PENDING', 2);