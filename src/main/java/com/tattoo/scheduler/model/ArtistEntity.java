package com.tattoo.scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "artists")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistEntity {
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

    @Column (updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Just for navigation!
    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<BookingEntity> bookingEntities;
}
