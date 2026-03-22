package com.tattoo.scheduler.repository;

import com.tattoo.scheduler.model.BookingEntity;
import com.tattoo.scheduler.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByArtistId(Long artistId);

    @Query("""
            SELECT COUNT(b)>0
            FROM BookingEntity b WHERE b.artist.id = :artistId
            AND b.startTime < :endOfBufferTime
            AND b.endOfBufferTime > :startTime
            AND b.status != :excludedStatus
            """)
    boolean hasOverlap(@Param("artistId") Long artistId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endOfBufferTime") LocalDateTime endOfBufferTime,
                       @Param("excludedStatus") BookingStatus excludedStatus);

    @Query("""
            SELECT b FROM BookingEntity b
            WHERE b.artist.id = :artistId
            AND b.startTime < :dayEnd AND b.endOfBufferTime > :dayStart
            AND b.status != :excludedStatus
            """)
    List<BookingEntity> findOccupiedIntervals(@Param("artistId") Long artistId,
                                              @Param("dayStart") LocalDateTime dayStart,
                                              @Param("dayEnd") LocalDateTime dayEnd,
                                              @Param("excludedStatus") BookingStatus excludedStatus);

    // Helper for calendar view
    List<BookingEntity> findByArtistIdAndStatusNot(Long artistId, BookingStatus status);
}
