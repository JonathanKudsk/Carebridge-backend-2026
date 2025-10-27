package com.carebridge.services;

import com.carebridge.dao.UserDAO;
import com.carebridge.models.User;
import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public void saveUser(User user) {
        userDAO.save(user);
    }
}
