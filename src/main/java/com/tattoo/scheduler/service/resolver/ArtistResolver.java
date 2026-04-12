package com.tattoo.scheduler.service.resolver;

import com.tattoo.scheduler.domain.Artist;

/**
 * Resolves artist entities by ID and provides a default artist.
 * <p>
 * Used when no specific artist is selected by the client.
 */
public interface ArtistResolver {
    /**
     * Retrieves an artist by ID.
     *
     * @param artistId the artist ID (if null, returns the default artist)
     * @return the artist domain object
     * @throws com.tattoo.scheduler.service.exception.ArtistNotFoundException if not found
     */
    Artist getArtist(Long artistId);
    /**
     * Returns the default artist ID configured in application properties.
     * Used when no artist is specified in the request.
     *
     * @return default artist ID
     */
    Long getDefaultArtistId();
}
