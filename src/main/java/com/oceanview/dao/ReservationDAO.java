package com.oceanview.dao;

import com.oceanview.model.Reservation;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface ReservationDAO {

    boolean create(Reservation reservation) throws SQLException;

    boolean update(Reservation reservation) throws SQLException;

    boolean delete(int id) throws SQLException;

    Reservation findById(int id) throws SQLException;

    List<Reservation> findAll() throws SQLException;

    Reservation findByReservationNumber(String reservationNumber) throws SQLException;

    List<Reservation> findByGuestName(String guestName) throws SQLException;

    List<Reservation> findByRoomId(int roomId) throws SQLException;

    List<Reservation> findByDateRange(Date startDate, Date endDate) throws SQLException;

    List<Reservation> findCheckInsToday() throws SQLException;

    List<Reservation> findCheckOutsToday() throws SQLException;
}
