package com.example.finalpillapp.SearchPill;

import android.content.Context;
import android.provider.Settings;

public class DeviceUtil {

    // 기기의 고유 ID를 반환하는 메서드
    public static String getDeviceId(Context context) {
        // 기기의 Secure 설정에서 ANDROID_ID를 가져와 반환
        // ANDROID_ID는 각 기기마다 고유한 값으로, 앱 설치 후에도 동일하게 유지됨
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}