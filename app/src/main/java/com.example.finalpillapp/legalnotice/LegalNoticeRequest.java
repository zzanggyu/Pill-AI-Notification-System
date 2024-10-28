package com.example.finalpillapp.legalnotice;

public class LegalNoticeRequest {
    private String userId;
    private boolean accepted;

    public LegalNoticeRequest(String userId, boolean accepted) {
        this.userId = userId;
        this.accepted = accepted;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
