package com.example.finalpillapp.MyPage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.Main.MainActivity;
import com.example.pillapp.R;

public class MyPageActivity extends AppCompatActivity {
    private Button btnPersonalInfo, btnManageDrugs, btnHomeFromMyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        btnPersonalInfo = findViewById(R.id.btn_personal_info);
        btnManageDrugs = findViewById(R.id.btn_manage_drugs);
        btnHomeFromMyPage = findViewById(R.id.btn_home_from_my_page);

        // 개인 정보 관리로 이동
        btnPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, PersonalInfoActivity.class);
            startActivity(intent);
        });

        // 복용 약 관리로 이동
        btnManageDrugs.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, ManageDrugsActivity.class);
            startActivity(intent);
        });

        // 메인 화면으로 돌아가는 버튼
        btnHomeFromMyPage.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
