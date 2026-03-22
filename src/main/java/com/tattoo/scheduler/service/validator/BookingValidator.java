package com.tattoo.scheduler.service.validator;

import com.tattoo.scheduler.domain.Booking;

public interface BookingValidator {
    void validate(Booking booking);
}
