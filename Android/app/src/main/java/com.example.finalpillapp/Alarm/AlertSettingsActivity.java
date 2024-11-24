package com.example.finalpillapp.Alarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AlertSettingsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String PREF_NAME = "AlarmPrefs";
    private static final String KEY_ALARMS = "alarms";
    private AlarmAdapter alarmAdapter;
    private List<Alarm> alarms;
    private AlarmStorage alarmStorage;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_settings);

        // AlarmStorage 초기화 및 저장된 알람 불러오기
        alarmStorage = new AlarmStorage(this);
        alarms = alarmStorage.loadAlarms();

        // RecyclerView 및 FLootingActionButton 초기화
        RecyclerView recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
        FloatingActionButton fabAddAlarm = findViewById(R.id.fabAddAlarm);

        // 알람 목록 및 어댑터 초기화
        alarms = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(alarms);
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAlarms.setAdapter(alarmAdapter);

        // 알람 추가 버튼 클릭 리스너
        fabAddAlarm.setOnClickListener(v -> showTimePickerDialog());

        // 스와이프로 알람 삭제 기능 추가
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(alarmAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerViewAlarms);

        // 권한 요청 런처 초기화
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // 권한이 부여됨
            } else {
                // 권한이 거부됨
                // TODO: 사용자에게 권한의 중요성을 설명하거나 대체 동작 수행
            }
        });


        // 알림 권한 확인 및 요청
        checkNotificationPermission();
        createNotificationChannel();
        checkAndRequestAlarmPermission();
        logSharedPreferences();
    }

    // SharedPreferences에 저장된 알람 목록 로그 출력
    private void logSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String alarms = ((SharedPreferences) prefs).getString(KEY_ALARMS, "No alarms saved");
        Log.d("SharedPreferences", "Saved alarms: " + alarms);
    }

    // 정확한 알람 권한을 확인하고 필요한 경우 사용자에게 권한 요청
    private void checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    // 시간선택 다이얼로그 표시
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    long newId = System.currentTimeMillis(); // 간단한 ID 생성 방식
                    Alarm newAlarm = new Alarm(newId, hourOfDay, minute);
                    alarms.add(newAlarm);
                    alarmAdapter.notifyItemInserted(alarms.size() - 1);
                    setAlarm(newAlarm);
                    saveAlarms();
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    // 알람 목록을 저장함
    private void saveAlarms() {
        alarmStorage.saveAlarms(alarms);
    }

    // 저장된 위치의 알람 삭제
    public void deleteAlarm(int position) {
        Alarm alarmToDelete = alarms.get(position);
        cancelAlarm(alarmToDelete);
        alarms.remove(position);
        alarmAdapter.notifyItemRemoved(position);
        saveAlarms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAlarms(); // 앱이 백그라운드로 갈 때 알람 저장
    }

    // 알림 채널 생성 메서드
    private void createNotificationChannel() {
        // Android 8.0 (API 26) 이상에서만 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for Alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("alarm_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // 알림 권한 확인하고 필요한 경우 요청
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }


    // 정확한 알람 설정 권한이 있는지 확인함
    private boolean hasExactAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return false;
    }

    // 알람 설정 메서드
    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(Alarm alarm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission()) {
            // 권한이 없으면 사용자에게 알리고 설정으로 이동
            Toast.makeText(this, "정확한 알람을 설정하려면 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            checkAndRequestAlarmPermission();
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, alarm.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 현재 시간보다 이전이면 다음 날로 설정
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }





    // 알람 취소 메서드
    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, alarm.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0));

        alarmManager.cancel(pendingIntent);
    }

    // 스와이프로 삭제 기능을 위한 콜백 클래스
    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private AlarmAdapter adapter;

        SwipeToDeleteCallback(AlarmAdapter adapter) {
            super(0, ItemTouchHelper.LEFT);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Alarm alarmToDelete = alarms.get(position);
            cancelAlarm(alarmToDelete);
            adapter.removeAlarm(position);
        }
    }
}