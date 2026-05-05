package com.carebridge;

import com.carebridge.config.ApplicationConfig;
<<<<<<< HEAD
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
=======
import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
>>>>>>> TEAM-3
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {

        Javalin app = ApplicationConfig.startServer(7070);

        Populator.populate(HibernateConfig.getEntityManagerFactory());

        app.get("/", ctx -> ctx.result("Carebridge API is running"));

        // Users
        User user1 = new User("admin", "admin@gmail.com", "1234", Role.ADMIN);
        User user2 = new User("test", "test@gmail.com", "1234", Role.USER);

        UserDAO userDAO = UserDAO.getInstance();
        userDAO.create(user1);
        userDAO.create(user2);

        ResidentDAO residentDAO = ResidentDAO.getInstance();

        // ---------- Resident 1 ----------
        Journal journal1 = new Journal();

        Resident res1 = new Resident();
        res1.setFirstName("Mariam");
        res1.setLastName("Elmir");
        res1.setCprNr("040404-0000");
        res1.setAge(22);
        res1.setGender("Female");

        // relation
        res1.setJournal(journal1);
        journal1.setResident(res1);

        residentDAO.create(res1);

        // ---------- Resident 2 ----------
        Journal journal2 = new Journal();

        Resident res2 = new Resident();
        res2.setFirstName("Ali");
        res2.setLastName("Hassan");
        res2.setCprNr("010101-1234");
        res2.setAge(30);
        res2.setGender("Male");

        res2.setJournal(journal2);
        journal2.setResident(res2);

        residentDAO.create(res2);

        // ---------- Resident 3 ----------
        Journal journal3 = new Journal();

        Resident res3 = new Resident();
        res3.setFirstName("Sara");
        res3.setLastName("Jensen");
        res3.setCprNr("020202-5678");
        res3.setAge(27);
        res3.setGender("Female");

        res3.setJournal(journal3);
        journal3.setResident(res3);

        residentDAO.create(res3);
    }
}