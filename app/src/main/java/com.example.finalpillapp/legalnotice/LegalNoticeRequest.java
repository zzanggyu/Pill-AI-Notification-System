package com.example.finalpillapp.legalnotice;

public class LegalNoticeRequest {
    private String userId;
    private String date;
    private boolean accepted;

    public LegalNoticeRequest(String userId, String date, boolean accepted) {
        this.userId = userId;
        this.date = date;
        this.accepted = accepted;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public boolean isAccepted() { return accepted; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setDate(String date) { this.date = date; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    @Override
    public String toString() {
        return "LegalNoticeRequest{" +
                "userId='" + userId + '\'' +
                ", date='" + date + '\'' +
                ", accepted=" + accepted +
                '}';
    }
}