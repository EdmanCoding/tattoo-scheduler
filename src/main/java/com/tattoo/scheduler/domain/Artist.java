package com.tattoo.scheduler.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Artist {
    private Long id;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdAt;
}
