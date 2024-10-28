package com.example.finalpillapp.SearchPill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.RecognizePill.BaseListActivity;
import com.example.pillapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class SearchResultsActivity extends BaseListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 전달된 JSON 데이터를 받아서 List 형태로 변환
        String pillListJson = getIntent().getStringExtra("pillList");
        List<PillInfo> searchResults = new Gson().fromJson(pillListJson, new TypeToken<List<PillInfo>>() {}.getType());

        // 검색 결과가 있으면 리스트를 보여주고, 없으면 종료
        if (searchResults != null && !searchResults.isEmpty()) {
            Log.d("SearchResultsActivity", "Received search results: " + searchResults.size());
            displayPillInfo(searchResults);
        } else {
            handleEmptyResults();
        }
    }

    // 검색 결과를 ListView에 표시
    @Override
    protected void displayPillInfo(List<PillInfo> pillList) {
        ListView pillListView = findViewById(R.id.pill_list);
        PillAdapter adapter = new PillAdapter(this, pillList);

        // 아이템 클릭 리스너를 통해 클릭된 아이템의 정보를 상세 화면으로 전달
        adapter.setOnItemClickListener(pill -> {
            Intent intent = new Intent(SearchResultsActivity.this, PillDetailActivity.class);
            intent.putExtra("selectedPill", new Gson().toJson(pill));  // Pill 정보를 JSON으로 전달
            startActivity(intent);
        });
        pillListView.setAdapter(adapter);
    }

    // 검색 결과가 없을 때 Toast 메시지 표시 후 종료
    @Override
    protected void handleEmptyResults() {
        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
