package com.tattoo.scheduler.service.creator;

import com.tattoo.scheduler.domain.Booking;

public interface BookingCreator {
    Booking enrichAndValidate(Booking booking);
}
