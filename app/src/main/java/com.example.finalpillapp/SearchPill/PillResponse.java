package com.example.finalpillapp.SearchPill;

import com.example.finalpillapp.PillInfo.PillInfo;

import java.util.List;

public class PillResponse {
    private boolean success;
    private String message;
    private List<PillInfo> data;
    private String error;

    // 기본 생성자
    public PillResponse() {}

    // 모든 필드를 포함하는 생성자
    public PillResponse(boolean success, String message, List<PillInfo> data, String error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    // Getter 및 Setter 메서드
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PillInfo> getData() {
        return data;
    }

    public void setData(List<PillInfo> data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
