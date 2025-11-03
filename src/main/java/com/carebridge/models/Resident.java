package com.carebridge.models;

import jakarta.persistence.*;

@Entity
@Table (name = "residents")
public class Resident
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId()
    {
        return id;
    }
}
