package com.example.pillapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AddDrugActivity extends AppCompatActivity {

    private EditText etSearchDrug;
    private ImageView btnSearch;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drug);

        etSearchDrug = findViewById(R.id.et_search_drug);
        btnSearch = findViewById(R.id.btn_search);
        btnAdd = findViewById(R.id.btn_add);

        // 검색 버튼 클릭 이벤트 처리
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 약물 검색 로직 추가
            }
        });

        // 추가 버튼 클릭 이벤트 처리
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택한 약물 목록에 추가하는 로직 추가
            }
        });
    }
}
