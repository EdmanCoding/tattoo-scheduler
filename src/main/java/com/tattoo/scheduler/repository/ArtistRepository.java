package com.tattoo.scheduler.repository;

import com.tattoo.scheduler.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
}
