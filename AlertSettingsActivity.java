package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class AlertSettingsActivity extends AppCompatActivity {
    private Button btnHomeFromAlertSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_settings);

        btnHomeFromAlertSettings = findViewById(R.id.btn_home_from_alert_settings);

        // 메인 화면으로 돌아가는 버튼
        btnHomeFromAlertSettings.setOnClickListener(v -> {
            Intent intent = new Intent(AlertSettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 뒤로 가기 시 알림 설정 화면으로 돌아가지 않도록 종료
        });
    }
}
