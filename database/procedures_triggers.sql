-- ============================================
-- OceanView Reservation System - Advanced SQL Features
-- ============================================
-- Stored Procedures, Triggers, and Transactions
-- Created: February 24, 2026
-- ============================================

USE oceanview_db;

-- Set delimiter for stored procedures and triggers
DELIMITER $$

-- ============================================
-- STORED PROCEDURE: calculate_total_bill
-- Description: Calculates the total bill for a reservation
-- Parameters:
--   - p_room_id: ID of the room
--   - p_check_in_date: Check-in date
--   - p_check_out_date: Check-out date
--   - OUT p_total_amount: Calculated total amount
--   - OUT p_num_nights: Number of nights
-- ============================================
DROP PROCEDURE IF EXISTS calculate_total_bill$$

CREATE PROCEDURE calculate_total_bill(
    IN p_room_id INT,
    IN p_check_in_date DATE,
    IN p_check_out_date DATE,
    OUT p_total_amount DECIMAL(10, 2),
    OUT p_num_nights INT
)
BEGIN
    DECLARE v_price_per_night DECIMAL(10, 2);
    
    -- Validate dates
    IF p_check_out_date <= p_check_in_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Check-out date must be after check-in date';
    END IF;
    
    -- Get room price
    SELECT price_per_night INTO v_price_per_night
    FROM rooms
    WHERE id = p_room_id;
    
    -- Check if room exists
    IF v_price_per_night IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Room not found';
    END IF;
    
    -- Calculate number of nights
    SET p_num_nights = DATEDIFF(p_check_out_date, p_check_in_date);
    
    -- Calculate total amount
    SET p_total_amount = v_price_per_night * p_num_nights;
    
END$$

-- ============================================
-- TRIGGER: prevent_overlapping_booking
-- Description: Prevents overlapping reservations for the same room
-- Trigger Type: BEFORE INSERT on reservations
-- ============================================
DROP TRIGGER IF EXISTS prevent_overlapping_booking$$

CREATE TRIGGER prevent_overlapping_booking
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
    DECLARE v_overlap_count INT;
    
    -- Check for overlapping reservations
    SELECT COUNT(*) INTO v_overlap_count
    FROM reservations
    WHERE room_id = NEW.room_id
    AND (
        -- New reservation starts during existing reservation
        (NEW.check_in_date >= check_in_date AND NEW.check_in_date < check_out_date)
        OR
        -- New reservation ends during existing reservation
        (NEW.check_out_date > check_in_date AND NEW.check_out_date <= check_out_date)
        OR
        -- New reservation completely contains existing reservation
        (NEW.check_in_date <= check_in_date AND NEW.check_out_date >= check_out_date)
    );
    
    -- If overlapping reservation exists, prevent insertion
    IF v_overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Room is already booked for the selected dates. Please choose different dates or another room.';
    END IF;
    
END$$

-- ============================================
-- TRIGGER: prevent_overlapping_booking_update
-- Description: Prevents overlapping reservations when updating
-- Trigger Type: BEFORE UPDATE on reservations
-- ============================================
DROP TRIGGER IF EXISTS prevent_overlapping_booking_update$$

CREATE TRIGGER prevent_overlapping_booking_update
BEFORE UPDATE ON reservations
FOR EACH ROW
BEGIN
    DECLARE v_overlap_count INT;
    
    -- Check for overlapping reservations (excluding current reservation)
    SELECT COUNT(*) INTO v_overlap_count
    FROM reservations
    WHERE room_id = NEW.room_id
    AND id != NEW.id  -- Exclude current reservation
    AND (
        -- New reservation starts during existing reservation
        (NEW.check_in_date >= check_in_date AND NEW.check_in_date < check_out_date)
        OR
        -- New reservation ends during existing reservation
        (NEW.check_out_date > check_in_date AND NEW.check_out_date <= check_out_date)
        OR
        -- New reservation completely contains existing reservation
        (NEW.check_in_date <= check_in_date AND NEW.check_out_date >= check_out_date)
    );
    
    -- If overlapping reservation exists, prevent update
    IF v_overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Room is already booked for the selected dates. Please choose different dates or another room.';
    END IF;
    
END$$

-- ============================================
-- STORED PROCEDURE: create_reservation_transaction
-- Description: Creates a new reservation using transactions
-- Parameters: All reservation details
-- Returns: reservation_id or 0 on error
-- ============================================
DROP PROCEDURE IF EXISTS create_reservation_transaction$$

CREATE PROCEDURE create_reservation_transaction(
    IN p_reservation_number VARCHAR(20),
    IN p_guest_name VARCHAR(100),
    IN p_address TEXT,
    IN p_contact_number VARCHAR(20),
    IN p_room_id INT,
    IN p_check_in_date DATE,
    IN p_check_out_date DATE,
    OUT p_reservation_id INT,
    OUT p_total_amount DECIMAL(10, 2),
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_num_nights INT;
    DECLARE v_room_status VARCHAR(20);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        -- Rollback transaction on error
        ROLLBACK;
        SET p_reservation_id = 0;
        SET p_status = 'ERROR';
        SET p_message = 'Failed to create reservation. Please try again.';
    END;
    
    -- Start transaction
    START TRANSACTION;
    
    -- Check if room exists and get status
    SELECT status INTO v_room_status
    FROM rooms
    WHERE id = p_room_id
    FOR UPDATE;  -- Lock the row
    
    IF v_room_status IS NULL THEN
        SET p_reservation_id = 0;
        SET p_status = 'ERROR';
        SET p_message = 'Room not found';
        ROLLBACK;
    ELSEIF v_room_status = 'MAINTENANCE' THEN
        SET p_reservation_id = 0;
        SET p_status = 'ERROR';
        SET p_message = 'Room is under maintenance';
        ROLLBACK;
    ELSE
        -- Calculate total bill
        CALL calculate_total_bill(
            p_room_id,
            p_check_in_date,
            p_check_out_date,
            p_total_amount,
            v_num_nights
        );
        
        -- Insert reservation
        INSERT INTO reservations (
            reservation_number,
            guest_name,
            address,
            contact_number,
            room_id,
            check_in_date,
            check_out_date,
            total_amount
        ) VALUES (
            p_reservation_number,
            p_guest_name,
            p_address,
            p_contact_number,
            p_room_id,
            p_check_in_date,
            p_check_out_date,
            p_total_amount
        );
        
        -- Get the inserted reservation ID
        SET p_reservation_id = LAST_INSERT_ID();
        
        -- Update room status to RESERVED
        UPDATE rooms
        SET status = 'RESERVED'
        WHERE id = p_room_id;
        
        -- Commit transaction
        COMMIT;
        
        SET p_status = 'SUCCESS';
        SET p_message = CONCAT('Reservation created successfully. Total: $', p_total_amount, ' for ', v_num_nights, ' night(s)');
    END IF;
    
END$$

-- ============================================
-- STORED PROCEDURE: cancel_reservation_transaction
-- Description: Cancels a reservation using transactions
-- ============================================
DROP PROCEDURE IF EXISTS cancel_reservation_transaction$$

CREATE PROCEDURE cancel_reservation_transaction(
    IN p_reservation_id INT,
    OUT p_status VARCHAR(20),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_room_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        -- Rollback transaction on error
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Failed to cancel reservation. Please try again.';
    END;
    
    -- Start transaction
    START TRANSACTION;
    
    -- Get room ID from reservation
    SELECT room_id INTO v_room_id
    FROM reservations
    WHERE id = p_reservation_id
    FOR UPDATE;
    
    IF v_room_id IS NULL THEN
        SET p_status = 'ERROR';
        SET p_message = 'Reservation not found';
        ROLLBACK;
    ELSE
        -- Delete reservation (will cascade due to foreign key)
        DELETE FROM reservations
        WHERE id = p_reservation_id;
        
        -- Update room status back to AVAILABLE
        UPDATE rooms
        SET status = 'AVAILABLE'
        WHERE id = v_room_id;
        
        -- Commit transaction
        COMMIT;
        
        SET p_status = 'SUCCESS';
        SET p_message = 'Reservation cancelled successfully';
    END IF;
    
END$$

-- ============================================
-- STORED PROCEDURE: check_room_availability
-- Description: Checks if a room is available for given dates
-- ============================================
DROP PROCEDURE IF EXISTS check_room_availability$$

CREATE PROCEDURE check_room_availability(
    IN p_room_id INT,
    IN p_check_in_date DATE,
    IN p_check_out_date DATE,
    OUT p_is_available BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_overlap_count INT;
    DECLARE v_room_status VARCHAR(20);
    
    -- Get room status
    SELECT status INTO v_room_status
    FROM rooms
    WHERE id = p_room_id;
    
    IF v_room_status IS NULL THEN
        SET p_is_available = FALSE;
        SET p_message = 'Room not found';
    ELSEIF v_room_status = 'MAINTENANCE' THEN
        SET p_is_available = FALSE;
        SET p_message = 'Room is under maintenance';
    ELSE
        -- Check for overlapping reservations
        SELECT COUNT(*) INTO v_overlap_count
        FROM reservations
        WHERE room_id = p_room_id
        AND (
            (p_check_in_date >= check_in_date AND p_check_in_date < check_out_date)
            OR
            (p_check_out_date > check_in_date AND p_check_out_date <= check_out_date)
            OR
            (p_check_in_date <= check_in_date AND p_check_out_date >= check_out_date)
        );
        
        IF v_overlap_count > 0 THEN
            SET p_is_available = FALSE;
            SET p_message = 'Room is already booked for selected dates';
        ELSE
            SET p_is_available = TRUE;
            SET p_message = 'Room is available';
        END IF;
    END IF;
    
END$$

-- Reset delimiter
DELIMITER ;

-- ============================================
-- USAGE EXAMPLES
-- ============================================

-- Example 1: Calculate total bill
-- CALL calculate_total_bill(1, '2026-03-01', '2026-03-05', @total, @nights);
-- SELECT @total AS total_amount, @nights AS number_of_nights;

-- Example 2: Check room availability
-- CALL check_room_availability(1, '2026-03-01', '2026-03-05', @available, @msg);
-- SELECT @available AS is_available, @msg AS message;

-- Example 3: Create reservation with transaction
-- CALL create_reservation_transaction(
--     'RES20260224003',
--     'Alice Johnson',
--     '789 Pine Road, Village, Country',
--     '+1-555-0103',
--     3,
--     '2026-03-10',
--     '2026-03-15',
--     @res_id,
--     @total,
--     @status,
--     @msg
-- );
-- SELECT @res_id AS reservation_id, @total AS total_amount, @status AS status, @msg AS message;

-- Example 4: Cancel reservation with transaction
-- CALL cancel_reservation_transaction(1, @status, @msg);
-- SELECT @status AS status, @msg AS message;

-- Example 5: Test overlapping booking prevention (should fail)
-- INSERT INTO reservations (
--     reservation_number, guest_name, address, contact_number,
--     room_id, check_in_date, check_out_date, total_amount
-- ) VALUES (
--     'RES20260224004', 'Bob Smith', '321 Elm St', '+1-555-0104',
--     7, '2026-02-25', '2026-02-28', 225.00
-- );
-- This should fail because room 7 is already booked from 2026-02-24 to 2026-02-27

-- ============================================
-- VERIFY STORED PROCEDURES AND TRIGGERS
-- ============================================

-- View all stored procedures
-- SHOW PROCEDURE STATUS WHERE Db = 'oceanview_db';

-- View all triggers
-- SHOW TRIGGERS FROM oceanview_db;

-- View specific procedure code
-- SHOW CREATE PROCEDURE calculate_total_bill;
-- SHOW CREATE PROCEDURE create_reservation_transaction;

-- View specific trigger code
-- SHOW CREATE TRIGGER prevent_overlapping_booking;
