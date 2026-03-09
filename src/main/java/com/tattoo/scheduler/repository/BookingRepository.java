package com.tattoo.scheduler.repository;

import com.tattoo.scheduler.model.Booking;
import com.tattoo.scheduler.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByArtistId(Long artistId);

    @Query("""
            SELECT COUNT(b)>0 
            FROM Booking b WHERE b.artist.id = :artistId
            AND b.startTime < :endTime
            AND b.endTime > :startTime
            AND b.status != :excludedStatus
            """)
    boolean hasOverlap(@Param("artistId") Long artistId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime,
                       @Param("excludedStatus") BookingStatus excludedStatus);

    // Helper for calendar view
    List<Booking> findByArtistIdAndStatusNot(Long artistId, BookingStatus status);


}
