package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnManageDrugs, btnAlarmSettings, btnRecognizePill, btnMyPage;
    private ImageView ivSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML에서 정의된 버튼과 이미지 뷰를 자바 코드에 연결
        btnManageDrugs = findViewById(R.id.btn_manage_drugs);
        btnAlarmSettings = findViewById(R.id.btn_alarm_settings);
        btnRecognizePill = findViewById(R.id.btn_recognize_pill);
        btnMyPage = findViewById(R.id.btn_my_page);
        ivSettings = findViewById(R.id.iv_settings);

        // 버튼 클릭 이벤트 설정
        btnManageDrugs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManageDrugsActivity.class);
                startActivity(intent);
            }
        });

        btnAlarmSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlertSettingsActivity.class);
                startActivity(intent);
            }
        });

        btnRecognizePill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhotoSearchActivity.class); // 약물 인식 액티비티로 이동
                startActivity(intent);
            }
        });

        btnMyPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                startActivity(intent);
            }
        });

        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
