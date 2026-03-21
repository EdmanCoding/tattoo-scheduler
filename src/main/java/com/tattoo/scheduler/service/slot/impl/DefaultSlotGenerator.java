package com.tattoo.scheduler.service.slot.impl;

import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.policy.BookingPolicy;
import com.tattoo.scheduler.service.slot.SlotGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultSlotGenerator implements SlotGenerator {
    private final BookingPolicy bookingPolicy;
    private static final int SLOT_GRANULARITY_MINUTES = 15;

    public DefaultSlotGenerator(BookingPolicy bookingPolicy) {
        this.bookingPolicy = bookingPolicy;
    }

    @Override
    public List<LocalDateTime> generate (LocalDateTime dayStart, LocalDateTime dayEnd,
                                         SessionType sessionType, List<Booking> existingBookings){
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime candidate = dayStart;
        long duration = sessionType.getDurationMinutes();
        long totalBlock = duration + sessionType.getBufferAfterMinutes();

        while(candidate.plusMinutes(duration).isBefore(dayEnd) ||
        candidate.plusMinutes(duration).isEqual(dayEnd)) {
            if(bookingPolicy.hasNoConflict(candidate,sessionType,existingBookings)) {
                slots.add(candidate);
                candidate = candidate.plusMinutes(totalBlock);
            } else {
                candidate = candidate.plusMinutes(SLOT_GRANULARITY_MINUTES);
            }
        }
        return slots;
    }
}
