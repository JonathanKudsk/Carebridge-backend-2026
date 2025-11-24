package com.carebridge.entities;

import com.carebridge.entities.enums.Role;
import com.carebridge.entities.security.ISecurityUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
public class User implements ISecurityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private Instant created_at;

    @Column(nullable = false)
    private Instant updated_at;

    public User() {
    }

    public User(String name, String email, String rawPassword, Role role) {
        this.name = name;
        this.email = email;
        setPassword(rawPassword);
        this.role = role != null ? role : Role.USER;
    }

    public void setPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("Password must not be blank");
        this.passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    @Override
    public boolean verifyPassword(String pw) {
        return pw != null && BCrypt.checkpw(pw, this.passwordHash);
    }

    @Override
    public void addRole(Role role) {
        if (role != null) this.role = role;
    }

    @Override
    public void removeRole(String roleName) {
        if (roleName != null && this.role.name().equalsIgnoreCase(roleName)) {
            this.role = Role.USER;
        }
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.created_at = now;
        this.updated_at = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updated_at = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }
}
