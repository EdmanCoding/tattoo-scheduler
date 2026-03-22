package com.tattoo.scheduler.repository;

import com.tattoo.scheduler.model.ArtistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {
}
