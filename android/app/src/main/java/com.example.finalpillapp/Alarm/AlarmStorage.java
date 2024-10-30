package com.example.finalpillapp.Alarm;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// 알람 데이터를 영구적으로 저장하고 불러오는 기능을 하는 클래스ㅁ
// SharedPreferences를 사용하여 알람 데이터를 JSON 형식으로 저장하고 불러옴
public class AlarmStorage {
    // SharedPreferences 파일 이름
    private static final String PREF_NAME = "AlarmPrefs";
    // 알람 데이터를 저장할 키 값
    private static final String KEY_ALARMS = "alarms";

    private SharedPreferences preferences;

    // context 앱의 컨텍스트. SharedPreferences 접근에 사용
    public AlarmStorage(Context context) {
        // MODE_PRIVATE: 이 앱에서만 접근 가능한 SharedPreferences 파일을 생성 또는 열기
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 알람 리스트 저장
    // 각 Alarm 객체를 JSON으로 변환하여 JSONArray로 만든 후, 문자열로 저장
    public void saveAlarms(List<Alarm> alarms) {
        JSONArray jsonArray = new JSONArray();
        for (Alarm alarm : alarms) {
            try {
                // 각 Alarm 객체를 JSON 객체로 변환
                jsonArray.put(alarm.toJson());
            } catch (JSONException e) {
                // JSON 변환 중 오류 발생 시 스택 트레이스 출력(예외 처리)
                // 예외가 발생한 위치와 그 호출 스택을 콘솔에 출력
                // 추적 정보를 보여줌
                // 개발과정에서 사용 출시과정에서는 적절한 예외처리로 바꾸어야 함
                e.printStackTrace();
            }
        }
        // JSONArray를 문자열로 변환하여 SharedPreferences에 저장
        preferences.edit().putString(KEY_ALARMS, jsonArray.toString()).apply();
    }

    // 저장된 알람 리스트를 불러옴
    // SharedPreferences에서 문자열을 불러와 JSONArray로 파싱한 후,
    // 각 JSON 객체를 Alarm 객체로 변환합니다.
    public List<Alarm> loadAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        // SharedPreferences에서 키 값으로 알람 데이터를 가져옴 기본값은 빈 JASONArray 문자열 "[]"
        String jsonString = preferences.getString(KEY_ALARMS, "[]");
        try {
            // 문자열을 JSONArray로 파싱
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                // 각 JSON 객체를 Alarm 객체로 변환
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                alarms.add(Alarm.fromJson(jsonObject));
            }
        } catch (JSONException e) {
            // JSON 파싱 중 오류 발생 시 스택 트레이스 출력
            e.printStackTrace();
        }
        return alarms;
    }
}