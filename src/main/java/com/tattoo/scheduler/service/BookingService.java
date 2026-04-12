package com.tattoo.scheduler.service;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.mapper.BookingMapper;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.creator.BookingCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingCreator bookingCreator;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @Transactional
    public Booking createBooking(Booking request) {
        Booking enriched = bookingCreator.enrichAndValidate(request);

        BookingEntity entity = bookingMapper.toEntity(enriched);
        entity.setUserEntity(userRepository.getReferenceById(enriched.getUserId()));
        entity.setArtistEntity(artistRepository.getReferenceById(enriched.getArtistId()));

        BookingEntity saved = bookingRepository.save(entity);

        return bookingMapper.toDomain(saved);
    }
}
