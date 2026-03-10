package com.tattoo.scheduler.util;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.model.User;

import java.time.LocalDate;

public class TestData {
    public static Artist createTestArtist() {
        return Artist.builder()
                .name("TestArtist")
                .email("testMail@email.com")
                .password("secret").build();
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
}
