package com.tattoo.scheduler.util;

import com.tattoo.scheduler.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.tattoo.scheduler.util.TestRequestFactory.DEFAULT_END_TIME;
import static com.tattoo.scheduler.util.TestRequestFactory.DEFAULT_START_TIME;

public class TestData {
    public static Artist createTestArtist() {
        return Artist.builder()
                .id(1L)
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret").build();
    }
    public static Artist createArtistWithId(Long id) {
        return Artist.builder()
                .id(id).build();
    }
    public static User createTestUser1() {
        return User.builder()
                .name("TestUser")
                .phoneNumber("123-4567")
                .email("testMailUser@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2001,4,15)).build();
    }
    public static User createTestUser2() {
        return User.builder()
                .name("TestUser2")
                .phoneNumber("765-4321")
                .email("testMailUser2@email.com")
                .password("secret")
                .birthDate(LocalDate.of(2000,2,19)).build();
    }
    public static User createUserWithId(Long id) {
        return User.builder()
                .id(id).build();
    }
    public static Booking createTestBooking(){
        return createTestBooking(99L, 42L, 1L);
    }
    public static Booking createTestBooking(Long bookingId, Long userId, Long artistId) {
        return Booking.builder()
                .id(bookingId)
                .user(createUserWithId(userId))
                .artist(createArtistWithId(artistId))
                .sessionType(SessionType.MEDIUM)
                .startTime(DEFAULT_START_TIME)
                .endTime(DEFAULT_END_TIME)
                .status(BookingStatus.PENDING)
                .notes("Test notes")
                .imagePath("/images/test.png")
                .createdAt(LocalDateTime.of(2026,3,10,17,0))
                .build();
    }
}
