package com.carebridge.entities;

import com.carebridge.crud.annotations.CrudResource;
import com.carebridge.crud.data.core.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@CrudResource(path = "residents")
public class Resident extends BaseEntity
{
    private String firstName;
    private String lastName;
    private String cprNr;

    @OneToOne(mappedBy = "resident", cascade = CascadeType.ALL)
    private Journal journal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "resident_user",
            joinColumns = @JoinColumn(name = "resident_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    public Resident()
    {
    }
    public Resident(String firstName, String lastName, String cprNr, Journal journal, User guardian)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNr = cprNr;
        this.journal = journal;
        this.users = new HashSet<>();
    }

    public String getCprNr() {
        return cprNr;
    }

    public void setCprNr(String cprNr) {
        this.cprNr = cprNr;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }
}
