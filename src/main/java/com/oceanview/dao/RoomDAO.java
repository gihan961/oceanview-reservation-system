package com.oceanview.dao;

import com.oceanview.model.Room;
import java.sql.SQLException;
import java.util.List;

public interface RoomDAO {

    boolean create(Room room) throws SQLException;

    boolean update(Room room) throws SQLException;

    boolean delete(int id) throws SQLException;

    Room findById(int id) throws SQLException;

    List<Room> findAll() throws SQLException;

    List<Room> findByStatus(String status) throws SQLException;

    List<Room> findByRoomType(String roomType) throws SQLException;

    boolean updateStatus(int roomId, String status) throws SQLException;
}
