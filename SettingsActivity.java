package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {
    private Button btnHomeFromSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnHomeFromSettings = findViewById(R.id.btn_home_from_settings);

        // 메인 화면으로 돌아가는 버튼
        btnHomeFromSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 뒤로 가기 시 설정 화면으로 돌아가지 않도록 종료
        });
    }
}
