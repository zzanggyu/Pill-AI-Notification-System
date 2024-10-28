// LegalNoticeRequest.java
package com.example.finalpillapp.legalnotice;

public class LegalNoticeRequest {
    private String userId;
    private boolean accepted;
    private String date;

    public LegalNoticeRequest(String userId, boolean accepted, String date) {
        this.userId = userId;
        this.accepted = accepted;
        this.date = date;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
