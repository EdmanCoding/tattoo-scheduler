package com.tattoo.scheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_artist_time", columnList = "artist_id, start_time, end_time")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private ArtistEntity artistEntity;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Session type is required")
    @Column(nullable = false)
    private SessionType sessionType;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Column(nullable = false)
    private LocalDateTime endTime;

    @NotNull(message = "Buffer time is required")
    @Column(nullable = false)
    private LocalDateTime endOfBufferTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private String notes;
    private String imagePath;

    @CreatedDate
    @Column (updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column (nullable = false)
    private LocalDateTime updatedAt;
}
