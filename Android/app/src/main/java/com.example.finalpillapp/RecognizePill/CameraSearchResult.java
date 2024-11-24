package com.example.finalpillapp.RecognizePill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.SearchPill.PillDetailActivity;
import com.example.finalpillapp.SearchPill.PillAdapter;
import com.example.pillapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class CameraSearchResult extends AppCompatActivity {

    private ListView pillListView;
    private List<PillInfo> pillInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        pillListView = findViewById(R.id.pill_list);

        // Intent로 전달된 JSON 데이터를 받아와 List로 변환
        String pillInfoJson = getIntent().getStringExtra("pillInfoList");

        if (pillInfoJson != null) {
            // JSON 데이터를 List<PillInfo>로 변환
            pillInfoList = new Gson().fromJson(pillInfoJson, new TypeToken<List<PillInfo>>() {}.getType());

            // 어댑터를 설정하고 클릭 리스너를 추가하여 클릭 시 상세 화면으로 이동
            PillAdapter adapter = new PillAdapter(this, pillInfoList);
            adapter.setOnItemClickListener(pill -> {
                Intent intent = new Intent(CameraSearchResult.this, PillDetailActivity.class);
                intent.putExtra("selectedPill", new Gson().toJson(pill));
                startActivity(intent);
            });
            pillListView.setAdapter(adapter);
        } else {
            // 데이터가 없을 경우 오류 메시지 표시
            Toast.makeText(this, "알약 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
