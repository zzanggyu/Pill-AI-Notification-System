package com.example.finalpillapp.API;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    // 기본 생성자
    public ApiResponse() {}

    // 모든 필드를 포함하는 생성자
    public ApiResponse(boolean success, String message, T data, String error) {
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

