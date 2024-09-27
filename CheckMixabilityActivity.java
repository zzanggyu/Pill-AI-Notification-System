package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CheckMixabilityActivity extends AppCompatActivity {
    private EditText etDrug1, etDrug2;
    private Button btnCheckMixability, btnHome;
    private TextView tvMixabilityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_mixability);

        // 툴바 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 액션바에서 뒤로가기 버튼 활성화
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // UI 요소 초기화
        etDrug1 = findViewById(R.id.et_drug1);
        etDrug2 = findViewById(R.id.et_drug2);
        btnCheckMixability = findViewById(R.id.btn_check_mixability);
        tvMixabilityResult = findViewById(R.id.tv_mixability_result);
        btnHome = findViewById(R.id.btn_home);

        // 혼합 가능성 확인 버튼 클릭 리스너
        btnCheckMixability.setOnClickListener(v -> {
            String drug1 = etDrug1.getText().toString();
            String drug2 = etDrug2.getText().toString();
            // 혼합 가능성 확인 로직을 여기에 추가
            tvMixabilityResult.setText("결과: 혼합 가능성 없음");
        });

        // 홈 버튼 클릭 리스너
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(CheckMixabilityActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // 뒤로가기 버튼 기능 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
