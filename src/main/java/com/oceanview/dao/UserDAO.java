package com.oceanview.dao;

import com.oceanview.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {

    boolean create(User user) throws SQLException;

    boolean update(User user) throws SQLException;

    boolean delete(int id) throws SQLException;

    User findById(int id) throws SQLException;

    List<User> findAll() throws SQLException;

    User findByUsername(String username) throws SQLException;

    User authenticate(String username, String passwordHash) throws SQLException;
}
