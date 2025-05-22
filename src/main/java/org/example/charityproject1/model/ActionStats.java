package org.example.charityproject1.model;

public class ActionStats {
    private long totalActions;
    private float totalCollected;
    private long totalParticipants;

    // Constructor
    public ActionStats(long totalActions, float totalCollected, long totalParticipants) {
        this.totalActions = totalActions;
        this.totalCollected = totalCollected;
        this.totalParticipants = totalParticipants;
    }

    // Default constructor
    public ActionStats() {
        this.totalActions = 0;
        this.totalCollected = 0;
        this.totalParticipants = 0;
    }

    // Getters and setters
    public long getTotalActions() {
        return totalActions;
    }

    public void setTotalActions(long totalActions) {
        this.totalActions = totalActions;
    }

    public float getTotalCollected() {
        return totalCollected;
    }

    public void setTotalCollected(float totalCollected) {
        this.totalCollected = totalCollected;
    }

    public long getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(long totalParticipants) {
        this.totalParticipants = totalParticipants;
    }
}