package com.tattoo.scheduler.service.resolver.impl;

import com.tattoo.scheduler.model.Artist;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultArtistResolver implements ArtistResolver {
    private final ArtistRepository artistRepository;
    private final Long defaultArtistId;

    public DefaultArtistResolver(ArtistRepository artistRepository,
                                 @Value("${app.artist.default-id:1}") Long defaultArtistId) {
        this.artistRepository = artistRepository;
        this.defaultArtistId = defaultArtistId;
    }

    @Override
    public Artist getArtist(Long artistId) {
        Long id = artistId != null ? artistId : defaultArtistId;
        return artistRepository.getReferenceById(id);
    }
}
