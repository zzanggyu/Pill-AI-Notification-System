package com.example.finalpillapp.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.legalnotice.LegalNoticeActivity;
import com.example.pillapp.R;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 3초 후에 법적 고지 화면으로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, LegalNoticeActivity.class);
                startActivity(intent);
                finish(); // 로딩 액티비티 종료
            }
        }, 3000); // 3초 딜레이
    }
}
