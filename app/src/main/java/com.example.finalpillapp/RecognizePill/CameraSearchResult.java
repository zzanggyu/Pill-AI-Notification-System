package com.example.finalpillapp.RecognizePill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.SearchPill.PillAdapter;
import com.example.finalpillapp.SearchPill.PillDetailActivity;
import com.example.pillapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class CameraSearchResult extends AppCompatActivity {

    private ListView pillListView;
    private List<PillInfo> pillInfoList;
    private PillInfo selectedPill; // 선택된 알약을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        pillListView = findViewById(R.id.pill_list);
        ImageButton selectButton = findViewById(R.id.select_button); // 알약 선택 버튼

        // JSON 데이터를 가져와서 List<PillInfo>로 변환
        String pillInfoJson = getIntent().getStringExtra("pillInfoList");
        if (pillInfoJson != null) {
            pillInfoList = new Gson().fromJson(pillInfoJson, new TypeToken<List<PillInfo>>() {}.getType());

            // 어댑터 설정
            PillAdapter adapter = new PillAdapter(this, pillInfoList);
            pillListView.setAdapter(adapter);

            // ListView 항목 클릭 시 선택된 항목을 기록
            pillListView.setOnItemClickListener((parent, view, position, id) -> {
                selectedPill = pillInfoList.get(position); // 선택된 알약을 저장
                Toast.makeText(CameraSearchResult.this, "알약이 선택되었습니다.", Toast.LENGTH_SHORT).show();
            });

            // "알약 선택" 버튼을 클릭하여 선택된 알약의 상세 정보로 이동
            selectButton.setOnClickListener(v -> {
                if (selectedPill != null) {
                    Intent intent = new Intent(CameraSearchResult.this, PillDetailActivity.class);
                    intent.putExtra("selectedPill", new Gson().toJson(selectedPill));
                    startActivity(intent);
                } else {
                    Toast.makeText(CameraSearchResult.this, "먼저 알약을 선택하세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "알약 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
