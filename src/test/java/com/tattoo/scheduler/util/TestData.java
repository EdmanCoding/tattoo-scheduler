package com.tattoo.scheduler.util;

import com.tattoo.scheduler.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestData {
    // === Constants ===
    public static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2026, 4, 15, 10, 0);
    public static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2026, 4, 15, 14, 0);
    public static final LocalDateTime DEFAULT_END_OF_BUFFER_TIME = LocalDateTime.of(2026, 4, 15, 16, 0);

    public static final Long TEST_USER_ID = 1L;
    public static final Long TEST_ARTIST_ID = 1L;
    public static final Long TEST_NONEXISTING_USER_ID = 658L;

    public static ArtistEntity createTestArtist() {
        return ArtistEntity.builder()
                .id(1L)
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret").build();
    }
    public static ArtistEntity createArtistWithId(Long id) {
        return ArtistEntity.builder()
                .id(id).build();
    }
    public static UserEntity createTestUser1() {
        return UserEntity.builder()
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2001,4,15)).build();
    }
    public static UserEntity createTestUser2() {
        return UserEntity.builder()
                .name("TestUser2")
                .phoneNumber("765-4321")
                .email("testMailUser2@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2000,2,19)).build();
    }
    public static UserEntity createUserWithId(Long id) {
        return UserEntity.builder()
                .id(id)
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2001,4,15)).build();
    }
    public static BookingEntity createTestBooking(){
        return createTestBooking(99L, 42L, 1L);
    }
    public static BookingEntity createTestBooking(Long bookingId, Long userId, Long artistId) {
        return BookingEntity.builder()
                .id(bookingId)
                .userEntity(createUserWithId(userId))
                .artistEntity(createArtistWithId(artistId))
                .sessionType(SessionType.MEDIUM)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)
                .status(BookingStatus.PENDING)
                .notes("Test notes")
                .imagePath("/images/test.png")
                .createdAt(LocalDateTime.of(2026,3,10,17,0))
                .build();
    }
}
