package com.tattoo.scheduler.domain;

import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Long id;
    private Long userId;
    private Long artistId;
    private SessionType sessionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime endOfBufferTime;
    private BookingStatus status;
    private String notes;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
