package com.tattoo.scheduler.service;

import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.mapper.BookingMapper;
import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.repository.BookingRepository;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.service.creator.BookingCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingCreator bookingCreator;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    public BookingService(BookingRepository bookingRepository,
                          BookingCreator bookingCreator,
                          BookingMapper bookingMapper,
                          UserRepository userRepository,
                          ArtistRepository artistRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingCreator = bookingCreator;
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
    }

    @Transactional
    public Booking createBooking(Booking request) {
        // 1. Enrich and validate (throws if invalid)
        Booking enriched = bookingCreator.enrichAndValidate(request);

        // 2. Convert to entity and set user/artist references
        BookingEntity entity = bookingMapper.toEntity(enriched);
        entity.setUserEntity(userRepository.getReferenceById(enriched.getUserId()));
        entity.setArtistEntity(artistRepository.getReferenceById(enriched.getArtistId()));

        // 3. Save
        BookingEntity saved = bookingRepository.save(entity);

        // 4. Convert back to domain
        return bookingMapper.toDomain(saved);
    }
}
