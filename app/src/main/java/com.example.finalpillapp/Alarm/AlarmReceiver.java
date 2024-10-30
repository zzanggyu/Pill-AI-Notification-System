package com.example.finalpillapp.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.pillapp.R;

// 이 클래스는 설정된 알람 시간이 되었을 때 시스템에 의해 호출되어 알림을 생성
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    // context 앱 현재 컨텍스트
    // intent 수신된 인텐트 알람에 대한 추가 정보
    public void onReceive(Context context, Intent intent) {
        // 알람 이벤트가 발생했을 때 시스템에 의해 호출되는 메서드
        // 알림을 생성하고 표시함
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); // 기본 알람 소리 URI 가져오기
        // 알람 구성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_alarmicon) // 알림 아이콘 설정
                .setContentTitle("알람") // 알림 제목
                .setContentText("약 복용시간이 되었습니다!") // 알림 내용
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 알람 우선순위 설정
                .setSound(alarmSound) // 알람 소리 설정
                .setAutoCancel(true); // 사용자가 알림을 탭하면 자동으로 알람 제거

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Android 13 (api  레벨 33) 이상에서 알림 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 권한이 부여됐느지 확인
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                //  권한이 있으면 알람 표시
                notificationManager.notify(1, builder.build());
            }
            // 권한 없으면 표시x
        } else {
            // Android 13 미만 버전에서는 권한 확인 없이 알림 표시
            notificationManager.notify(1, builder.build());
        }
    }
}
