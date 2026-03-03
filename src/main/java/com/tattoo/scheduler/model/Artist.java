package com.tattoo.scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "artists")
@Data
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    // TODO: Hash password with BCrypt before saving in production
    @NotBlank
    private String password;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
