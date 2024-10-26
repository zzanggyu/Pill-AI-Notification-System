package com.example.finalpillapp.legalnotice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.Main.MainActivity;
import com.example.pillapp.R;

public class LegalNoticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_notice);

        // "동의 및 시작" 버튼을 찾습니다.
        ImageButton btnAgreeStart = findViewById(R.id.agree_button);

        // 버튼 클릭 리스너 설정
        btnAgreeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LegalNoticeActivity", "동의 및 시작 버튼 클릭됨");

                // MainActivity로 이동
                Intent intent = new Intent(LegalNoticeActivity.this, MainActivity.class);
                startActivity(intent);

                // 현재 액티비티 종료
                finish();
            }
        });
    }
}
