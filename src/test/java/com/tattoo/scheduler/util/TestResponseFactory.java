package com.tattoo.scheduler.util;

import com.tattoo.scheduler.dto.BookingResponse;
import com.tattoo.scheduler.model.BookingStatus;
import com.tattoo.scheduler.model.SessionType;

import java.time.LocalDateTime;

import static com.tattoo.scheduler.util.TestData.*;

public class TestResponseFactory {
    public static ResponseBuilder response() { return new ResponseBuilder(); }
    public static class ResponseBuilder{
        private Long id = TEST_BOOKING_ID;
        private Long userId = TEST_USER_ID;
        private Long artistId = TEST_ARTIST_ID;
        private SessionType sessionType = SessionType.MEDIUM;
        private LocalDateTime startTime = DEFAULT_START_TIME;
        private LocalDateTime endTime = DEFAULT_END_TIME;
        private LocalDateTime endOfBufferTime = DEFAULT_END_OF_BUFFER_TIME;
        private BookingStatus status = BookingStatus.PENDING;
        private String notes;
        private String imagePath;
        private LocalDateTime createdAt = DEFAULT_CREATED_TIME;

        public ResponseBuilder withId(Long id){
            this.id = id;
            return this;
        }
        public ResponseBuilder withUserId(Long userId){
            this.userId = userId;
            return this;
        }
        public ResponseBuilder withArtistId(Long artistId){
            this.artistId = artistId;
            return this;
        }
        public ResponseBuilder ofType(SessionType sessionType){
            this.sessionType = sessionType;
            return this;
        }
        public ResponseBuilder withStartTimeAt(LocalDateTime startTime){
            this.startTime = startTime;
            return this;
        }
        public ResponseBuilder withEndTimeAt(LocalDateTime endTime){
            this.endTime = endTime;
            return this;
        }
        public ResponseBuilder withEndOfBufferTimeAt(LocalDateTime endOfBufferTime){
            this.endOfBufferTime = endOfBufferTime;
            return this;
        }
        public ResponseBuilder withStatus(BookingStatus status){
            this.status = status;
            return this;
        }
        public ResponseBuilder withNotes(String notes){
            this.notes = notes;
            return this;
        }
        public ResponseBuilder withImagePath(String imagePath){
            this.imagePath = imagePath;
            return this;
        }
        public ResponseBuilder withCreatedAt(LocalDateTime createdAt){
            this.createdAt = createdAt;
            return this;
        }
        public BookingResponse build(){
            return new BookingResponse(id, userId, artistId, sessionType, startTime,
                    endTime, endOfBufferTime, status, notes, imagePath, createdAt);
        }
    }
}
