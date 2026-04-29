package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.annotations.DynamicDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    private final ResidentDAO residentDAO;
    private final UserDAO userDAO;
    private final MappingService mappingService;

    public ResidentController(ResidentDAO residentDAO, UserDAO userDAO, MappingService mappingService) {
        this.residentDAO = residentDAO;
        this.userDAO = userDAO;
        this.mappingService = mappingService;
    }

    @GetMapping
    @DynamicDTO
    public List<Resident> getAll() {
        return residentDAO.readAll();
    }

    @GetMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<Resident> getById(@PathVariable Long id) {
        Resident r = residentDAO.read(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/cpr/{cpr}")
    @DynamicDTO
    public ResponseEntity<Resident> getByCpr(@PathVariable String cpr) {
        Resident r = residentDAO.readByCpr(cpr);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @PostMapping("/create")
    @DynamicDTO
    public ResponseEntity<Resident> create(
            @RequestBody Map<String, Object> body,
            @RequestAttribute(value = "user", required = false) Map<String, Object> jwtUser) {
        
        try {
            Resident resident = mappingService.toEntity(body, Resident.class);

            if (resident.getFirstName() == null || resident.getFirstName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            if (resident.getLastName() == null || resident.getLastName().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            Journal journal = new Journal();
            resident.setJournal(journal);
            journal.setResident(resident);

            if (jwtUser != null) {
                String email = (String) jwtUser.get("username");
                User user = userDAO.readByEmail(email);
                if (user != null) {
                    resident.addUser(user);
                }
            }

            Resident created = residentDAO.create(resident);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/residents/" + created.getId())
                    .body(created);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
