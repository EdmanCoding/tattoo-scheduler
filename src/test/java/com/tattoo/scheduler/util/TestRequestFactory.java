package com.tattoo.scheduler.util;

import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.SessionType;

import java.time.LocalDateTime;

import static com.tattoo.scheduler.util.TestData.DEFAULT_START_TIME;

public class TestRequestFactory {

    public static RequestBuilder request() { return new RequestBuilder(); }

    public static class RequestBuilder{
        private SessionType type = SessionType.MEDIUM;
        private LocalDateTime startTime = DEFAULT_START_TIME;
        private String notes;
        private String imagePath;

        public RequestBuilder ofType(SessionType type) {
            this.type = type;
            return this;
        }
        public RequestBuilder at(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }
        public RequestBuilder withNotes(String notes) {
            this.notes = notes;
            return this;
        }
        public RequestBuilder withImage(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }
        public CreateBookingRequest build(){
            return new CreateBookingRequest(type, startTime, notes, imagePath);
        }
    }
}
