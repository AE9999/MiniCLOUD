package com.ae.sat.message;

/**
 * Created by ae on 24-5-16.
 */
public class Stats {
    private float progress;
    private long conflicts;
    private String message;

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public long getConflicts() {
        return conflicts;
    }

    public void setConflicts(long conflicts) {
        this.conflicts = conflicts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "progress=" + progress +
                ", conflicts=" + conflicts +
                ", message='" + message + '\'' +
                '}';
    }
}
