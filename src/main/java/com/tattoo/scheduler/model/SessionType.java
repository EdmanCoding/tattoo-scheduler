package com.tattoo.scheduler.model;

public enum SessionType {
    SMALL_CONSULTATION(30, 30),
    SMALL(60, 60),
    LARGE_CONSULTATION(90, 30),
    MEDIUM(240, 120),
    LARGE(480, 0);
    private final int durationMinutes;
    private final int bufferAfterMinutes;

    SessionType(int durationMinutes, int bufferAfterMinutes) {
        this.durationMinutes = durationMinutes;
        this.bufferAfterMinutes = bufferAfterMinutes;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getBufferAfterMinutes() {
        return bufferAfterMinutes;
    }
}
