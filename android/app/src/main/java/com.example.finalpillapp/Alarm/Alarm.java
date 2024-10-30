package com.example.finalpillapp.Alarm;

import org.json.JSONException;
import org.json.JSONObject;

// 하나의 알람에 대한 정보를 나타내는 클래스 ID, 시간, 분, 활성화 상태를 관리
public class Alarm {
    private long id; // id
    private final int hour; // 알람 시간(0~23)
    private final int minute; // 알람 분(0~59)
    private boolean isOn; // 알람 활성화 상태

    // Alarm 객체 생성
    public Alarm(long id, int hour, int minute) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.isOn = true; // 기본적으로 알람은 활성화 상태로 생성(true),  비활(false)
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public String getTimeString() {
        // 알람 시간을 "HH:MM" 형식으로 반환 포맷팅
        return String.format("%02d:%02d", hour, minute);
    }

    // Alarm 객체를 JSON 객체로 변환
    // 알람 데이터를 저장하기 위해서
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("hour", hour);
        json.put("minute", minute);
        json.put("isOn", isOn);
        return json;
    }

    // JSON 객체로부터 Alarm 객체를 생성
    // 저장된 알람 데이터를 네트워크로 받아 Alarm 객체로 변환
    public static Alarm fromJson(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        int hour = json.getInt("hour");
        int minute = json.getInt("minute");
        Alarm alarm = new Alarm(id, hour, minute);
        alarm.setOn(json.getBoolean("isOn"));
        return alarm;
    }
}



