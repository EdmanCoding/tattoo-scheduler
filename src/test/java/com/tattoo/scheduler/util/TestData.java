package com.tattoo.scheduler.util;

import com.tattoo.scheduler.domain.Artist;
import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestData {
    // === Constants ===
    public static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.of(2026, 4, 15, 10, 0);
    public static final LocalDateTime DEFAULT_END_TIME = LocalDateTime.of(2026, 4, 15, 14, 0);
    public static final LocalDateTime DEFAULT_END_OF_BUFFER_TIME = LocalDateTime.of(2026, 4, 15, 16, 0);
    public static final LocalDateTime DEFAULT_CREATED_TIME = LocalDateTime.of(2026, 3, 15, 12, 44);
    public static final LocalDateTime DEFAULT_DAY_END_TIME = LocalDateTime.of(2026, 4, 15, 20, 0);
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2026, 4, 15);
    public static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2000, 2, 22);

    public static final Long TEST_USER_ID = 1L;
    public static final Long TEST_ARTIST_ID = 1L;
    public static final Long TEST_BOOKING_ID = 1L;
    public static final Long TEST_NONEXISTING_ARTIST_ID = 658L;

    public static ArtistEntity createTestArtistEntity() {
        return ArtistEntity.builder()
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret").build();
    }

    public static ArtistEntity createArtistEntityWithId(Long id) {
        ArtistEntity artist = createTestArtistEntity();
        artist.setId(id);
        return artist;
    }

    public static UserEntity createTestUserEntity1() {
        return UserEntity.builder()
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(DEFAULT_BIRTH_DATE).build();
    }

    public static UserEntity createUserEntityWithId(Long id) {
        return UserEntity.builder()
                .id(id)
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(DEFAULT_BIRTH_DATE).build();
    }

    public static BookingEntity createTestBookingEntity() {
        return createTestBookingEntity(TEST_USER_ID, TEST_ARTIST_ID);
    }

    public static BookingEntity createTestBookingEntity(Long userId, Long artistId) {
        return BookingEntity.builder()
                .userEntity(createUserEntityWithId(userId))
                .artistEntity(createArtistEntityWithId(artistId))
                .sessionType(SessionType.MEDIUM)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)
                .status(BookingStatus.PENDING)
                .notes("Test notes")
                .imagePath("/images/test.png")
                .createdAt(DEFAULT_CREATED_TIME).build();
    }

    public static BookingEntity createTestBookingEntity(UserEntity user, ArtistEntity artist,
                                                        SessionType type, LocalDateTime start) {
        return BookingEntity.builder()
                .userEntity(user)
                .artistEntity(artist)
                .sessionType(type)
                .startTime(start)
                .endTime(start.plusMinutes(type.getDurationMinutes()))
                .endOfBufferTime(start.plusMinutes(type.getDurationMinutes())
                        .plusMinutes(type.getBufferAfterMinutes()))
                .status(BookingStatus.PENDING)
                .createdAt(DEFAULT_CREATED_TIME)
                .updatedAt(DEFAULT_CREATED_TIME).build();
    }

    public static Booking createTestBookingDomain() {
        return Booking.builder()
                .id(99L)
                .userId(42L)
                .artistId(1L)
                .sessionType(SessionType.MEDIUM)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .endOfBufferTime(DEFAULT_END_OF_BUFFER_TIME)
                .status(BookingStatus.PENDING)
                .notes("Test notes")
                .imagePath("/images/test.png")
                .createdAt(DEFAULT_CREATED_TIME)
                .updatedAt(DEFAULT_CREATED_TIME).build();
    }

    public static Artist createTestArtistDomain() {
        return Artist.builder()
                .id(TEST_ARTIST_ID)
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret")
                .createdAt(DEFAULT_CREATED_TIME).build();
    }

    public static User createTestUserDomain() {
        return User.builder()
                .id(TEST_USER_ID)
                .name("TestUser")
                .email("testMailUser@email.com")
                .phoneNumber("123-4567")
                .birthDate(DEFAULT_BIRTH_DATE)
                .password("secret")
                .createdAt(DEFAULT_CREATED_TIME).build();
    }
}
