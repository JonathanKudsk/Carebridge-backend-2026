package com.carebridge.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    public User() {}
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    // getters & setters
}
