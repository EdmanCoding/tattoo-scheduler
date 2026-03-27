package com.tattoo.scheduler.service.policy;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.policy.impl.NikitaBookingPolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NikitaBookingPolicyTest {
    private final NikitaBookingPolicy policy = new NikitaBookingPolicy();
    // 10:00 on June 26, 2026
    private static final LocalDateTime BASE_DATE_TIME = LocalDateTime.of(2026, 6, 26, 10, 0);

    @ParameterizedTest(name = "daysOffset={0} → should be {1}")
    @CsvSource({
            "1, true",      // tomorrow
            "0, false",     // today
            "-1, false",    // yesterday
            "30, true",     // month ahead
            "-30, false"    // month ago
    })
    void isDateAllowed_ShouldReturnCorrectResult(int daysOffset, boolean expected) {
        LocalDate date = LocalDate.now().plusDays(daysOffset);  // Arrange
        boolean result = policy.isDateAllowed(date);            // Act
        assertThat(result).isEqualTo(expected);                 // Assert
    }
    // working hours are 10:00 - 20:00
    @ParameterizedTest
    @MethodSource("workingHoursTestData")
    void isWithinWorkingHours_ShouldReturnCorrectResult(
            LocalDateTime start, SessionType type, boolean expected) {
        assertThat(policy.isWithinWorkingHours(start, type)).isEqualTo(expected);
    }
    private static Stream<Arguments> workingHoursTestData() {
        return Stream.concat(
                workingHoursValidScenarios(),
                workingHoursInvalidScenarios()
        );
    }
    private static Stream<Arguments> workingHoursValidScenarios() {
        return Stream.of(
                // Exactly at 10:00 – all session types should work
                Arguments.of(BASE_DATE_TIME, SessionType.SMALL, true),
                Arguments.of(BASE_DATE_TIME, SessionType.MEDIUM, true),
                Arguments.of(BASE_DATE_TIME, SessionType.LARGE, true),
                Arguments.of(BASE_DATE_TIME, SessionType.SMALL_CONSULTATION, true),
                Arguments.of(BASE_DATE_TIME, SessionType.LARGE_CONSULTATION, true),
                // Sessions that end exactly at 20:00
                Arguments.of(BASE_DATE_TIME.plusHours(9), SessionType.SMALL, true),     //19:00
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.MEDIUM, true),    //16:00
                Arguments.of(BASE_DATE_TIME.plusHours(2), SessionType.LARGE, true),      //12:00
                Arguments.of(BASE_DATE_TIME.plusHours(9).plusMinutes(30), SessionType.SMALL_CONSULTATION, true), //19:30
                Arguments.of(BASE_DATE_TIME.plusHours(8).plusMinutes(30), SessionType.LARGE_CONSULTATION, true)  //18:30
        );
    }
    private static Stream<Arguments> workingHoursInvalidScenarios() {
        return Stream.of(
                // Just before 10:00 – all should fail
                Arguments.of(BASE_DATE_TIME.minusMinutes(1),SessionType.SMALL, false),
                Arguments.of(BASE_DATE_TIME.minusMinutes(1), SessionType.MEDIUM, false),
                Arguments.of(BASE_DATE_TIME.minusMinutes(1), SessionType.LARGE, false),
                Arguments.of(BASE_DATE_TIME.minusMinutes(1), SessionType.SMALL_CONSULTATION, false),
                Arguments.of(BASE_DATE_TIME.minusMinutes(1), SessionType.LARGE_CONSULTATION, false),
                // Sessions that end just after 20:00 – all should fail
                Arguments.of(BASE_DATE_TIME.plusHours(9).plusMinutes(1), SessionType.SMALL, false),     //19:01
                Arguments.of(BASE_DATE_TIME.plusHours(6).plusMinutes(1), SessionType.MEDIUM, false),    //16:01
                Arguments.of(BASE_DATE_TIME.plusHours(2).plusMinutes(1), SessionType.LARGE, false),      //12:01
                Arguments.of(BASE_DATE_TIME.plusHours(9).plusMinutes(31), SessionType.SMALL_CONSULTATION, false), //19:31
                Arguments.of(BASE_DATE_TIME.plusHours(8).plusMinutes(31), SessionType.LARGE_CONSULTATION, false)  //18:31
        );
    }
    @ParameterizedTest(name = "[{index}] {0} with {1} → {3}")
    @MethodSource("conflictCheckTestData")
    void hasNoConflict_ShouldReturnCorrectResult(LocalDateTime start, SessionType type,
                                                 List<Booking> existingBookings, boolean expected) {
        assertThat(policy.hasNoConflict(start, type, existingBookings)).isEqualTo(expected);
    }
    private static Stream<Arguments> conflictCheckTestData() {
        Booking medium = Booking.builder()
                .sessionType(SessionType.MEDIUM)
                .startTime(BASE_DATE_TIME)                      // 10:00
                .endOfBufferTime(BASE_DATE_TIME.plusHours(6))   // 16:00
                .build();
        Booking small = Booking.builder()
                .sessionType(SessionType.SMALL)
                .startTime(BASE_DATE_TIME.plusHours(8))         // 18:00
                .endOfBufferTime(BASE_DATE_TIME.plusHours(10))  // 20:00
                .build();
        Booking smallConsult = Booking.builder()
                .sessionType(SessionType.SMALL_CONSULTATION)
                .startTime(BASE_DATE_TIME.plusHours(6))         // 16:00
                .endOfBufferTime(BASE_DATE_TIME.plusHours(7))   // 17:00
                .build();
        Booking largeConsult = Booking.builder()
                .sessionType(SessionType.LARGE_CONSULTATION)
                .startTime(BASE_DATE_TIME.plusHours(6).plusMinutes(30))         // 16:30
                .endOfBufferTime(BASE_DATE_TIME.plusHours(8).plusMinutes(30))   // 18:30
                .build();
        return Stream.of(
                //      <== NO CONFLICT ==>
                // starts right after the buffer end of existing
                // and ends exactly at the end of working hours
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.MEDIUM,
                        List.of(medium), true),
                // starts right after the buffer end of existing
                // and buffer ends right before the start of existing
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.SMALL,
                        List.of(medium, small), true),
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.LARGE_CONSULTATION,
                        List.of(medium, small), true),
                Arguments.of(BASE_DATE_TIME.plusHours(7), SessionType.SMALL_CONSULTATION,
                        List.of(medium, small, smallConsult), true),
                //      <== CONFLICT ==>
                // start overlaps with buffer of existing
                Arguments.of(BASE_DATE_TIME.plusHours(5), SessionType.MEDIUM,
                        List.of(medium), false),
                Arguments.of(BASE_DATE_TIME.plusHours(5), SessionType.SMALL,
                        List.of(medium), false),
                Arguments.of(BASE_DATE_TIME.plusHours(5), SessionType.LARGE_CONSULTATION,
                        List.of(medium), false),
                Arguments.of(BASE_DATE_TIME.plusHours(5), SessionType.SMALL_CONSULTATION,
                        List.of(medium), false),
                // start overlaps with session time of existing
                Arguments.of(BASE_DATE_TIME.plusHours(3), SessionType.MEDIUM,
                        List.of(medium), false),
                Arguments.of(BASE_DATE_TIME.plusHours(3), SessionType.SMALL,
                        List.of(medium), false),
                Arguments.of(BASE_DATE_TIME.plusHours(3), SessionType.LARGE_CONSULTATION,
                        List.of(medium), false),
                Arguments.of(BASE_DATE_TIME.plusHours(3), SessionType.SMALL_CONSULTATION,
                        List.of(medium), false),
                // buffer's end overlaps with start of existing
                Arguments.of(BASE_DATE_TIME.plusHours(2).plusMinutes(30), SessionType.MEDIUM,
                        List.of(small), false),
                Arguments.of(BASE_DATE_TIME.plusHours(7), SessionType.SMALL,
                        List.of(small), false),
                Arguments.of(BASE_DATE_TIME.plusHours(6).plusMinutes(30), SessionType.LARGE_CONSULTATION,
                        List.of(small), false),
                Arguments.of(BASE_DATE_TIME.plusHours(7).plusMinutes(30), SessionType.SMALL_CONSULTATION,
                        List.of(small), false),
                // session's end overlaps with start of existing
                Arguments.of(BASE_DATE_TIME.plusHours(4).plusMinutes(30), SessionType.MEDIUM,
                        List.of(small), false),
                Arguments.of(BASE_DATE_TIME.plusHours(7).plusMinutes(30), SessionType.SMALL,
                        List.of(small), false),
                Arguments.of(BASE_DATE_TIME.plusHours(7), SessionType.LARGE_CONSULTATION,
                        List.of(small), false),
                Arguments.of(BASE_DATE_TIME.plusHours(7).plusMinutes(40), SessionType.SMALL_CONSULTATION,
                        List.of(small), false),
                // overlaps with two existing
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.MEDIUM,
                        List.of(medium, largeConsult), false),
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.SMALL,
                        List.of(medium, largeConsult), false),
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.LARGE_CONSULTATION,
                        List.of(medium, largeConsult), false),
                Arguments.of(BASE_DATE_TIME.plusHours(6), SessionType.SMALL_CONSULTATION,
                        List.of(medium, largeConsult), false)
        );
    }
    @ParameterizedTest(name = "[{index}] Booking {0} -> {3} -> {2}")
    @MethodSource("largeExclusivityTestData")
    void respectsLargeExclusivity_shouldReturnCorrectResult(SessionType type, List<Booking> existing,
                                                            boolean expected, String description) {
        assertThat(policy.respectsLargeExclusivity(type, existing)).isEqualTo(expected);
    }
    private static Stream<Arguments> largeExclusivityTestData() {
        Booking large = Booking.builder()
                .sessionType(SessionType.LARGE)
                .build();
        Booking medium = Booking.builder()
                .sessionType(SessionType.MEDIUM)
                .build();
        Booking small = Booking.builder()
                .sessionType(SessionType.SMALL)
                .build();
        Booking smallConsult = Booking.builder()
                .sessionType(SessionType.SMALL_CONSULTATION)
                .build();
        Booking largeConsult = Booking.builder()
                .sessionType(SessionType.LARGE_CONSULTATION)
                .build();
        return Stream.of(
                //   <<== want to book LARGE ==>>
                //         <= NO CONFLICT =>
                Arguments.of(SessionType.LARGE, List.of(), true, "No other active booking this day"),
                //          <= CONFLICT =>
                Arguments.of(SessionType.LARGE, List.of(large), false,
                        "LARGE active booking exists"),
                Arguments.of(SessionType.LARGE, List.of(medium), false,
                        "MEDIUM active booking exists"),
                Arguments.of(SessionType.LARGE, List.of(small), false,
                        "SMALL active booking exists"),
                Arguments.of(SessionType.LARGE, List.of(smallConsult), false,
                        "SMALL_CONSULTATION active booking exists"),
                Arguments.of(SessionType.LARGE, List.of(largeConsult), false,
                        "LARGE_CONSULTATION active booking exists"),
                Arguments.of(SessionType.LARGE, List.of(largeConsult, smallConsult, medium), false,
                        "Many different types active bookings exists"),
                Arguments.of(SessionType.LARGE, List.of(small, small, small), false,
                        "Many active bookings of the same type exists"),
                //   <<== want to book non LARGE ==>>
                //         <= NO CONFLICT =>
                Arguments.of(SessionType.MEDIUM, List.of(), true, "No other active booking this day"),
                Arguments.of(SessionType.MEDIUM, List.of(small), true, "SMALL active booking exists"),
                Arguments.of(SessionType.SMALL, List.of(), true, "No other active booking this day"),
                Arguments.of(SessionType.SMALL, List.of(medium), true, "MEDIUM active booking exists"),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(), true, "No other active booking this day"),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(small), true, "SMALL active booking exists"),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(), true, "No other active booking this day"),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(medium), true, "MEDIUM active booking exists"),
                //          <= CONFLICT =>
                Arguments.of(SessionType.MEDIUM, List.of(large), false, "LARGE active booking exists"),
                Arguments.of(SessionType.SMALL, List.of(large), false, "LARGE active booking exists"),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(large), false, "LARGE active booking exists"),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(large), false, "LARGE active booking exists")
        );
    }
}
