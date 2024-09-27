package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LegalNoticeActivity extends AppCompatActivity {

    private Button btnAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_notice);

        btnAgree = findViewById(R.id.btn_agree);

        // 법적 고지 동의 후 메인 화면으로 이동
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LegalNoticeActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 법적 고지 액티비티 종료
            }
        });
    }
}
