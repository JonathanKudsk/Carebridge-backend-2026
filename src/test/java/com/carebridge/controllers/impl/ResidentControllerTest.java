package com.carebridge.controllers.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Resident;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for ResidentController.
 * Using real MappingService and manual DAO delegation to bypass persistent Mockito subclassing issues on Java 26.
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentControllerTest {

    private IDAO<Resident, Long> residentDaoMock;
    private IDAO<User, Long> userDaoMock;
    private MappingService mappingService;
    private ResidentController residentController;

    @BeforeEach
    void setUp() {
        residentDaoMock = mock(IDAO.class);
        userDaoMock = mock(IDAO.class);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mappingService = new MappingService(mapper);
        
        ResidentDAO residentDAO = new ResidentDAO() {
            @Override public Resident create(Resident r) { return residentDaoMock.create(r); }
            @Override public Resident read(Long id) { return residentDaoMock.read(id); }
            @Override public List<Resident> readAll() { return residentDaoMock.readAll(); }
            @Override public Resident update(Long id, Resident r) { return residentDaoMock.update(id, r); }
            @Override public void delete(Long id) { residentDaoMock.delete(id); }
            @Override public Resident readByCpr(String cpr) { return null; }
        };

        UserDAO userDAO = new UserDAO() {
            @Override public User create(User u) { return userDaoMock.create(u); }
            @Override public User read(Long id) { return userDaoMock.read(id); }
            @Override public List<User> readAll() { return userDaoMock.readAll(); }
            @Override public User update(Long id, User u) { return userDaoMock.update(id, u); }
            @Override public void delete(Long id) { userDaoMock.delete(id); }
            @Override public User readByEmail(String email) { return null; }
        };

        residentController = new ResidentController(residentDAO, userDAO, mappingService);
    }

    @Test
    @Order(1)
    void createTest(){
        Map<String, Object> body = Map.of(
                "firstName", "Børge",
                "lastName", "Børgesen",
                "cprNr", "121212-1234"
        );

        Map<String, Object> jwtUser = Map.of(
                "id", 1L,
                "username", "admin@carebridge.com"
        );
        
        Resident resident = new Resident();
        resident.setId(1L);
        resident.setFirstName("Børge");
        resident.setLastName("Børgesen");
        
        when(residentDaoMock.create(any(Resident.class))).thenReturn(resident);

        ResponseEntity<Resident> response = residentController.create(body, jwtUser);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(residentDaoMock, times(1)).create(any(Resident.class));
    }
}