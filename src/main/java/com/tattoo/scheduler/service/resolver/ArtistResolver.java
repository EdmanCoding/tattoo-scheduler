package com.tattoo.scheduler.service.resolver;

import com.tattoo.scheduler.domain.Artist;

public interface ArtistResolver {
    Artist getArtist(Long artistId);
    Long getDefaultArtistId();
}
