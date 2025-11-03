package com.carebridge.models;

import jakarta.persistence.*;
import java.util.List;

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
