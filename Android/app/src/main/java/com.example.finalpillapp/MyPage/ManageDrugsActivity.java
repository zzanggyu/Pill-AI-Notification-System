package com.example.finalpillapp.MyPage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalpillapp.Main.MainActivity;
import com.example.finalpillapp.PillManagement.AddDrugActivity;
import com.example.finalpillapp.PillManagement.DrugAdapter;

import java.util.ArrayList;
import java.util.List;

public class ManageDrugsActivity extends AppCompatActivity {
    private Button btnAddDrug;
    private RecyclerView rvDrugs;
    private DrugAdapter drugAdapter;
    private List<String> drugList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.pillapp.R.layout.activity_manage_drugs);

        // RecyclerView 설정
        rvDrugs = findViewById(com.example.pillapp.R.id.rv_drugs);
        rvDrugs.setLayoutManager(new LinearLayoutManager(this));
        drugAdapter = new DrugAdapter(drugList);
        rvDrugs.setAdapter(drugAdapter);

        // 약물 추가 버튼 클릭 리스너
        btnAddDrug = findViewById(com.example.pillapp.R.id.btn_add_drug);
        btnAddDrug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AddDrugActivity로 이동하여 약물 검색 및 추가
                Intent intent = new Intent(ManageDrugsActivity.this, AddDrugActivity.class);
                startActivityForResult(intent, 1); // 1은 요청 코드
            }
        });

        // 홈으로 돌아가기 버튼
        Button btnHome = findViewById(com.example.pillapp.R.id.btn_home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageDrugsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // 약물 추가 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // AddDrugActivity에서 추가된 약물을 받아옴
            String addedDrug = data.getStringExtra("added_drug");
            if (addedDrug != null) {
                drugList.add(addedDrug);
                drugAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
            }
        }
    }
}
