package com.tattoo.scheduler.model;

public enum SessionType {
    SMALL_CONSULTATION(30),
    SMALL(60),
    LARGE_CONSULTATION(90),
    MEDIUM(240),
    LARGE(480);
    private final int durationMinutes;
    SessionType(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    public int getDurationMinutes() {
        return durationMinutes;
    }
}
