package com.tattoo.scheduler.service.slot;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.policy.impl.NikitaBookingPolicy;
import com.tattoo.scheduler.service.slot.impl.DefaultSlotGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.tattoo.scheduler.util.TestData.DEFAULT_DAY_END_TIME;
import static com.tattoo.scheduler.util.TestData.DEFAULT_START_TIME;
import static org.assertj.core.api.Assertions.assertThat;


public class DefaultSlotGeneratorTest {
    private final SlotGenerator generator = new DefaultSlotGenerator(new NikitaBookingPolicy());

    @ParameterizedTest(name = "[{index}] Book {0} -> {3} -> slots: {4}")
    @MethodSource("slotGenerationTestData")
    void generate_shouldReturnExpectedSlots(
            SessionType type, List<Booking> existingBookings,
            List<LocalDateTime> expectedSlots,
            String description, Integer number) {
        List<LocalDateTime> result = generator.generate(DEFAULT_START_TIME, DEFAULT_DAY_END_TIME,
                type, existingBookings);
        assertThat(result).containsExactlyElementsOf(expectedSlots);
        assertThat(result.size()).isEqualTo(number);
    }

    private static Stream<Arguments> slotGenerationTestData() {
        // 10:00-14:00, buffer until 16:00
        Booking mediumBookingDayStart = Booking.builder()
                .startTime(DEFAULT_START_TIME)
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(6))
                .build();
        // 16:00-20:00, buffer until 22:00
        Booking mediumBookingDayEnd = Booking.builder()
                .startTime(DEFAULT_START_TIME.plusHours(6))
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(12))
                .build();
        // 12:00-16:00, buffer until 18:00
        Booking mediumBookingMidDay = Booking.builder()
                .startTime(DEFAULT_START_TIME.plusHours(2))
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(8))
                .build();
        // 10:00-11:00, buffer until 12:00
        Booking smallBookingDayStart = Booking.builder()
                .startTime(DEFAULT_START_TIME)
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(2))
                .build();
        // 19:00-20:00, buffer until 21:00
        Booking smallBookingDayEnd = Booking.builder()
                .startTime(DEFAULT_START_TIME.plusHours(9))
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(11))
                .build();
        // 14:00-15:00, buffer until 16:00
        Booking smallBookingMidDay = Booking.builder()
                .startTime(DEFAULT_START_TIME.plusHours(4))
                .endOfBufferTime(DEFAULT_START_TIME.plusHours(6))
                .build();

        // Expected slots when no bookings
        List<LocalDateTime> noBookingsSmallConsultationSlots = List.of(DEFAULT_START_TIME,
                DEFAULT_START_TIME.plusHours(1), DEFAULT_START_TIME.plusHours(2),
                DEFAULT_START_TIME.plusHours(3), DEFAULT_START_TIME.plusHours(4),
                DEFAULT_START_TIME.plusHours(5), DEFAULT_START_TIME.plusHours(6),
                DEFAULT_START_TIME.plusHours(7), DEFAULT_START_TIME.plusHours(8),
                DEFAULT_START_TIME.plusHours(9));
        List<LocalDateTime> noBookingsLargeConsultationSlots = List.of(DEFAULT_START_TIME,
                DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(4),
                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8));
        List<LocalDateTime> noBookingsSmallSlots = List.of(DEFAULT_START_TIME,
                DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(4),
                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8));
        List<LocalDateTime> noBookingsMediumSlots = List.of(DEFAULT_START_TIME,
                DEFAULT_START_TIME.plusHours(6));

        return Stream.of(
                // Free days
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(),
                        noBookingsSmallConsultationSlots, "Free day", 10),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(),
                        noBookingsLargeConsultationSlots, "Free day", 5),
                Arguments.of(SessionType.SMALL, List.of(), noBookingsSmallSlots, "Free day", 5),
                Arguments.of(SessionType.MEDIUM, List.of(), noBookingsMediumSlots, "Free day", 2),

                Arguments.of(SessionType.MEDIUM, List.of(mediumBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(6)),
                        "Medium 10:00-14:00 exist", 1),
                Arguments.of(SessionType.MEDIUM, List.of(mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME), "Medium 16:00-20:00 exist", 1),
                Arguments.of(SessionType.MEDIUM, List.of(mediumBookingMidDay),
                        List.of(), "Medium 12:00-16:00 exist", 0),

                Arguments.of(SessionType.SMALL, List.of(mediumBookingDayStart),
                List.of(DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8)),
                "Medium 10:00-14:00 exist", 2),
                Arguments.of(SessionType.SMALL, List.of(mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(2),
                                DEFAULT_START_TIME.plusHours(4)), "Medium 16:00-20:00 exist", 3),
                Arguments.of(SessionType.SMALL, List.of(mediumBookingMidDay),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(8)),
                        "Medium 12:00-16:00 exist", 2),

                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(mediumBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(7),
                                DEFAULT_START_TIME.plusHours(8), DEFAULT_START_TIME.plusHours(9)),
                        "Medium 10:00-14:00 exist", 4),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(1),
                                DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(3),
                                DEFAULT_START_TIME.plusHours(4), DEFAULT_START_TIME.plusHours(5)),
                        "Medium 16:00-20:00 exist", 6),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(mediumBookingMidDay),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(1),
                                DEFAULT_START_TIME.plusHours(8), DEFAULT_START_TIME.plusHours(9)),
                        "Medium 12:00-16:00 exist", 4),

                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(mediumBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8)),
                        "Medium 10:00-14:00 exist", 2),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(2),
                                DEFAULT_START_TIME.plusHours(4)), "Medium 16:00-20:00 exist", 3),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(mediumBookingMidDay),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(8)),
                        "Medium 12:00-16:00 exist", 2),

                Arguments.of(SessionType.MEDIUM, List.of(smallBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(2)), "Small 10:00-11:00 exist", 1),
                Arguments.of(SessionType.MEDIUM, List.of(smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME), "Small 19:00-20:00 exist", 1),
                Arguments.of(SessionType.MEDIUM, List.of(smallBookingMidDay),
                        List.of(DEFAULT_START_TIME.plusHours(6)), "Small 14:00-15:00 exist", 1),

                Arguments.of(SessionType.SMALL, List.of(smallBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(4),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8)),
                        "Small 10:00-11:00 exist", 4),
                Arguments.of(SessionType.SMALL, List.of(smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(2),
                                DEFAULT_START_TIME.plusHours(4), DEFAULT_START_TIME.plusHours(6))
                        , "Small 19:00-20:00 exist", 4),
                Arguments.of(SessionType.SMALL, List.of(smallBookingMidDay),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(2),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8)),
                        "Small 14:00-15:00 exist", 4),

                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(smallBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(3),
                                DEFAULT_START_TIME.plusHours(4), DEFAULT_START_TIME.plusHours(5),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(7),
                                DEFAULT_START_TIME.plusHours(8), DEFAULT_START_TIME.plusHours(9)),
                        "Small 10:00-11:00 exist", 8),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(1),
                                DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(3),
                                DEFAULT_START_TIME.plusHours(4), DEFAULT_START_TIME.plusHours(5),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(7),
                                DEFAULT_START_TIME.plusHours(8))
                        , "Small 19:00-20:00 exist", 9),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(smallBookingMidDay),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(1),
                                DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(3),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(7),
                                DEFAULT_START_TIME.plusHours(8), DEFAULT_START_TIME.plusHours(9)),
                        "Small 14:00-15:00 exist", 8),

                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(smallBookingDayStart),
                        List.of(DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(4),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8)),
                        "Small 10:00-11:00 exist", 4),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(2),
                                DEFAULT_START_TIME.plusHours(4), DEFAULT_START_TIME.plusHours(6))
                        , "Small 19:00-20:00 exist", 4),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(smallBookingMidDay),
                        List.of(DEFAULT_START_TIME, DEFAULT_START_TIME.plusHours(2),
                                DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(8)),
                        "Small 14:00-15:00 exist", 4),

                Arguments.of(SessionType.MEDIUM, List.of(mediumBookingDayStart, mediumBookingDayEnd),
                        List.of(), "Two medium exist", 0),
                Arguments.of(SessionType.SMALL, List.of(mediumBookingDayStart, mediumBookingDayEnd),
                        List.of(), "Two medium exist", 0),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(mediumBookingDayStart, mediumBookingDayEnd),
                        List.of(), "Two medium exist", 0),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(mediumBookingDayStart, mediumBookingDayEnd),
                        List.of(), "Two medium exist", 0),

                Arguments.of(SessionType.MEDIUM, List.of(mediumBookingDayStart, smallBookingDayEnd),
                        List.of(), "Medium(10-14) and small(19-20) exist", 0),
                Arguments.of(SessionType.MEDIUM, List.of(smallBookingDayStart, mediumBookingDayEnd),
                        List.of(), "Small(10-11) and Medium(16-20) exist", 0),

                Arguments.of(SessionType.SMALL, List.of(mediumBookingDayStart, smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME.plusHours(6)), "Medium(10-14) and small(19-20) exist", 1),
                Arguments.of(SessionType.SMALL, List.of(smallBookingDayStart, mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(4)),
                        "Small(10-11) and Medium(16-20) exist", 2),

                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(mediumBookingDayStart, smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME.plusHours(6), DEFAULT_START_TIME.plusHours(7),
                                DEFAULT_START_TIME.plusHours(8)), "Medium(10-14) and small(19-20) exist", 3),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(smallBookingDayStart, mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(3),
                                DEFAULT_START_TIME.plusHours(4), DEFAULT_START_TIME.plusHours(5)),
                        "Small(10-11) and Medium(16-20) exist", 4),

                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(mediumBookingDayStart, smallBookingDayEnd),
                        List.of(DEFAULT_START_TIME.plusHours(6)), "Medium(10-14) and small(19-20) exist", 1),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(smallBookingDayStart, mediumBookingDayEnd),
                        List.of(DEFAULT_START_TIME.plusHours(2), DEFAULT_START_TIME.plusHours(4)),
                        "Small(10-11) and Medium(16-20) exist", 2),

                Arguments.of(SessionType.MEDIUM, List.of(smallBookingDayStart, smallBookingMidDay,
                        smallBookingDayEnd), List.of(), "Small(10-11, 14-15, 19-20) exist", 0),
                Arguments.of(SessionType.SMALL, List.of(smallBookingDayStart, smallBookingMidDay,
                        smallBookingDayEnd), List.of(DEFAULT_START_TIME.plusHours(2),
                        DEFAULT_START_TIME.plusHours(6)), "Small(10-11, 14-15, 19-20) exist", 2),
                Arguments.of(SessionType.SMALL_CONSULTATION, List.of(smallBookingDayStart, smallBookingMidDay,
                        smallBookingDayEnd), List.of(DEFAULT_START_TIME.plusHours(2),
                        DEFAULT_START_TIME.plusHours(3), DEFAULT_START_TIME.plusHours(6),
                        DEFAULT_START_TIME.plusHours(7), DEFAULT_START_TIME.plusHours(8)),
                        "Small(10-11, 14-15, 19-20) exist", 5),
                Arguments.of(SessionType.LARGE_CONSULTATION, List.of(smallBookingDayStart, smallBookingMidDay,
                        smallBookingDayEnd), List.of(DEFAULT_START_TIME.plusHours(2),
                        DEFAULT_START_TIME.plusHours(6)), "Small(10-11, 14-15, 19-20) exist", 2));
    }
}
