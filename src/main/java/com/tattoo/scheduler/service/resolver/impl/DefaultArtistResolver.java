package com.tattoo.scheduler.service.resolver.impl;

import com.tattoo.scheduler.domain.Artist;
import com.tattoo.scheduler.mapper.ArtistMapper;
import com.tattoo.scheduler.model.ArtistEntity;
import com.tattoo.scheduler.repository.ArtistRepository;
import com.tattoo.scheduler.service.resolver.ArtistResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultArtistResolver implements ArtistResolver {
    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;
    private final Long defaultArtistId;

    public DefaultArtistResolver(ArtistRepository artistRepository,
                                 @Value("${app.artist.default-id:1}") Long defaultArtistId,
                                 ArtistMapper artistMapper) {
        this.artistRepository = artistRepository;
        this.defaultArtistId = defaultArtistId;
        this.artistMapper = artistMapper;
    }

    @Override
    public Artist getArtist(Long artistId) {
        Long id = artistId != null ? artistId : defaultArtistId;
        ArtistEntity entity = artistRepository.getReferenceById(id);
        return artistMapper.toDomain(entity);
    }
}
