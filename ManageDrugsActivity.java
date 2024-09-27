package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ManageDrugsActivity extends AppCompatActivity {
    private Button btnAddDrug;
    private RecyclerView rvDrugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_drugs);

        btnAddDrug = findViewById(R.id.btn_add_drug);
        rvDrugs = findViewById(R.id.rv_drugs);

        // 약물 목록 RecyclerView 설정
        rvDrugs.setLayoutManager(new LinearLayoutManager(this));

        // 약물 추가 버튼 클릭 리스너 설정
        btnAddDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageDrugsActivity.this, AddDrugActivity.class);
                startActivity(intent);
            }
        });

        // 홈 버튼 추가
        Button btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageDrugsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
