package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.config.Populator;
import com.carebridge.crud.annotations.DynamicDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserDAO userDAO;
    private final ResidentDAO residentDAO;
    private final MappingService mappingService;
    private final Populator populator;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserDAO userDAO, ResidentDAO residentDAO, MappingService mappingService, Populator populator) {
        this.userDAO = userDAO;
        this.residentDAO = residentDAO;
        this.mappingService = mappingService;
        this.populator = populator;
    }

    @GetMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<User> read(@PathVariable Long id) {
        User user = userDAO.read(id);
        if (user == null) {
            throw new ApiRuntimeException(400, "User cannot be null");
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @DynamicDTO
    public List<User> readAll() {
        return userDAO.readAll();
    }

    @PostMapping
    @DynamicDTO
    public ResponseEntity<User> create(@RequestBody Map<String, Object> body) {
        User user = mappingService.toEntity(body, User.class);
        if (body.get("password") != null) {
            user.setPassword((String) body.get("password"));
        }
        User created = userDAO.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = mappingService.toEntity(body, User.class);
        if (body.get("password") != null) {
            user.setPassword((String) body.get("password"));
        }
        User updated = userDAO.update(id, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userDAO.delete(id);
    }

    @GetMapping("/me")
    @DynamicDTO
    public ResponseEntity<User> me(@RequestAttribute(value = "user", required = false) Map<String, Object> jwtUser) {
        if (jwtUser == null) {
            throw new ApiRuntimeException(400, "JWT user cannot be null");
        }
        String email = (String) jwtUser.get("username");
        User user = userDAO.readByEmail(email);
        if (user == null) {
            throw new ApiRuntimeException(404, "User not found");
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/populate")
    public Map<String, String> populate() {
        populator.populate();
        return Map.of("msg", "Database populated");
    }

    @PostMapping("/{id}/link-residents")
    public Map<String, String> linkResidents(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<Number> residentIds = (List<Number>) body.get("residentIds");
        User user = userDAO.read(id);
        if (user == null) {
            throw new ApiRuntimeException(404, "User not found");
        }

        List<Resident> residentsToLink = new ArrayList<>();
        for (Number residentId : residentIds) {
            Resident r = residentDAO.read(residentId.longValue());
            if (r != null) {
                residentsToLink.add(r);
            }
        }

        user.setResidents(residentsToLink);
        userDAO.update(id, user);

        return Map.of("msg", "Beboere tilknyttet");
    }
}
