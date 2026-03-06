package com.tattoo.scheduler.repository;

import com.tattoo.scheduler.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByArtistId(Long artistId);

}
