package com.carebridge;

import com.carebridge.models.User;
import com.carebridge.services.UserService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    @Test
    void canSaveAndFetchUser() {
        UserService service = new UserService();
        User user = new User("Alice", "alice@example.com");
        service.saveUser(user);
        assertTrue(service.getAllUsers().size() > 0);
    }
}
