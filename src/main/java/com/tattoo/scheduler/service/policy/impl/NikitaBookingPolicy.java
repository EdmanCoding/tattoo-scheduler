package com.tattoo.scheduler.service.policy.impl;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_END_HOUR;
import static com.tattoo.scheduler.service.constants.BookingConstants.WORK_START_HOUR;

@Component
public class NikitaBookingPolicy implements BookingPolicy {
    @Override
    public boolean isDateAllowed(LocalDate date) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return !date.isBefore(tomorrow);
    }

    @Override
    public boolean isWithinWorkingHours(LocalDateTime start, SessionType type) {
        LocalDateTime end = start.plusMinutes(type.getDurationMinutes());
        return start.getHour() >= WORK_START_HOUR &&
                (end.getHour() < WORK_END_HOUR || (end.getHour() == WORK_END_HOUR && end.getMinute() == 0))
                && end.toLocalDate().equals(start.toLocalDate());
    }

    @Override
    public boolean hasNoConflict(LocalDateTime start, SessionType type, List<Booking> existingBookings) {
        LocalDateTime protectedEnd = start.plusMinutes(type.getDurationMinutes())
                .plusMinutes(type.getBufferAfterMinutes());
        return existingBookings.stream().noneMatch(booking ->
                start.isBefore(booking.getEndOfBufferTime()) &&
                        protectedEnd.isAfter(booking.getStartTime()));
    }

    @Override
    public boolean respectsLargeExclusivity(SessionType newType, List<Booking> existingBookings) {
        if (newType == SessionType.LARGE) {
            return existingBookings.isEmpty();
        } else {
            // If we are booking something else, there must be no existing LARGE booking
            return existingBookings.stream().noneMatch(booking ->
                    booking.getSessionType() == SessionType.LARGE);
        }
    }
}
