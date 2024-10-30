package com.example.finalpillapp.PillManagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddDrugActivity extends AppCompatActivity {

    private EditText etSearchDrug;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.pillapp.R.layout.activity_add_drug);

        etSearchDrug = findViewById(com.example.pillapp.R.id.et_search_drug);
        btnAdd = findViewById(com.example.pillapp.R.id.btn_add);

        // 추가 버튼 클릭 이벤트 처리
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 약물을 추가하고 결과를 돌려줌
                String drugName = etSearchDrug.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("added_drug", drugName);
                setResult(RESULT_OK, resultIntent);
                finish(); // AddDrugActivity 종료
            }
        });
    }
}
