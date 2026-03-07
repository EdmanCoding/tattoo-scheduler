package com.tattoo.scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@ToString(exclude = "bookings")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    @Past
    private LocalDate birthDate;

    // TODO: Hash password with BCrypt before saving
    @NotBlank
    private String password;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Just for navigation!
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
