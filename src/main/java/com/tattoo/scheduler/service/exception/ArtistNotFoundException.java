package com.tattoo.scheduler.service.exception;

public class ArtistNotFoundException extends RuntimeException {
    public ArtistNotFoundException(Long artistId) { super("Artist with id " + artistId + " not found"); }
}
