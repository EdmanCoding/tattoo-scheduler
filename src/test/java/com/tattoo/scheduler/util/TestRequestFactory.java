package com.tattoo.scheduler.util;

import com.tattoo.scheduler.dto.CreateBookingRequest;
import com.tattoo.scheduler.model.SessionType;

import java.time.LocalDateTime;

import static com.tattoo.scheduler.util.TestData.DEFAULT_START_TIME;

public class TestRequestFactory {

    public static RequestBuilder request(){
        return new RequestBuilder();
    }

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

    /*public static CreateBookingRequest largeSessionRequest(){
        return new CreateBookingRequest(SessionType.LARGE, DEFAULT_START_TIME, null, null);
    }
    public static CreateBookingRequest largeBookingRequest(LocalDateTime startTime){
        return new CreateBookingRequest(SessionType.LARGE, startTime, null, null);
    }
    public static CreateBookingRequest largeBookingRequest(LocalDateTime startTime, String notes){
        return new CreateBookingRequest(SessionType.LARGE, startTime, notes, null);
    }
    public static CreateBookingRequest largeBookingRequest(LocalDateTime startTime, String notes, String imagePath){
        return new CreateBookingRequest(SessionType.LARGE, startTime, notes, imagePath);
    }
    public static CreateBookingRequest mediumSessionRequest(){
        return new CreateBookingRequest(SessionType.MEDIUM, DEFAULT_START_TIME, null, null);
    }
    public static CreateBookingRequest mediumSessionRequest(LocalDateTime startTime){
        return new CreateBookingRequest(SessionType.MEDIUM, startTime, null, null);
    }
    public static CreateBookingRequest mediumSessionRequest(LocalDateTime startTime, String notes){
        return new CreateBookingRequest(SessionType.MEDIUM, startTime, notes, null);
    }
    public static CreateBookingRequest mediumSessionRequest(LocalDateTime startTime, String notes, String imagePath){
        return new CreateBookingRequest(SessionType.MEDIUM, startTime, notes, imagePath);
    }
    public static CreateBookingRequest smallSessionRequest(){
        return new CreateBookingRequest(SessionType.SMALL, DEFAULT_START_TIME, null, null);
    }
    public static CreateBookingRequest smallSessionRequest(LocalDateTime startTime){
        return new CreateBookingRequest(SessionType.SMALL, startTime, null, null);
    }
    public static CreateBookingRequest smallSessionRequest(LocalDateTime startTime, String notes){
        return new CreateBookingRequest(SessionType.SMALL, startTime, notes, null);
    }
    public static CreateBookingRequest smallSessionRequest(LocalDateTime startTime, String notes, String imagePath){
        return new CreateBookingRequest(SessionType.SMALL, startTime, notes, imagePath);
    }
    public static CreateBookingRequest smallConsultationRequest(){
        return new CreateBookingRequest(SessionType.SMALL_CONSULTATION, DEFAULT_START_TIME, null, null);
    }
    public static CreateBookingRequest smallConsultationRequest(LocalDateTime startTime){
        return new CreateBookingRequest(SessionType.SMALL_CONSULTATION, startTime, null, null);
    }
    public static CreateBookingRequest smallConsultationRequest(LocalDateTime startTime, String notes){
        return new CreateBookingRequest(SessionType.SMALL_CONSULTATION, startTime, null, notes);
    }
    public static CreateBookingRequest smallConsultationRequest(LocalDateTime startTime, String notes, String imagePath){
        return new CreateBookingRequest(SessionType.SMALL_CONSULTATION, startTime, notes, imagePath);
    }
    public static CreateBookingRequest largeConsultationRequest(){
        return new CreateBookingRequest(SessionType.LARGE_CONSULTATION, DEFAULT_START_TIME, null, null);
    }
    public static CreateBookingRequest largeConsultationRequest(LocalDateTime startTime){
        return new CreateBookingRequest(SessionType.LARGE_CONSULTATION, startTime, null, null);
    }
    public static CreateBookingRequest largeConsultationRequest(LocalDateTime startTime, String notes){
        return new CreateBookingRequest(SessionType.LARGE_CONSULTATION, startTime, notes, null);
    }
    public static CreateBookingRequest largeConsultationRequest(LocalDateTime startTime, String notes, String imagePath){
        return new CreateBookingRequest(SessionType.LARGE_CONSULTATION, startTime, notes, imagePath);
    }*/
}
