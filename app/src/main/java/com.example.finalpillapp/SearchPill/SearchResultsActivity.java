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

import java.util.List;

public class SearchResultsActivity extends BaseListActivity {

    private static final String TAG = "SearchResultsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        List<PillInfo> searchResults = getIntent().getParcelableArrayListExtra("pillList");

        if (searchResults != null && !searchResults.isEmpty()) {
            Log.d(TAG, "Received search results: " + searchResults.size());
            displayPillInfo(searchResults);
        } else {
            handleEmptyResults();
        }
    }

    @Override
    protected void displayPillInfo(List<PillInfo> pillList) {
        ListView pillListView = findViewById(R.id.pill_list);
        PillAdapter adapter = new PillAdapter(this, pillList);

        adapter.setOnItemClickListener(pill -> {
            Intent intent = new Intent(SearchResultsActivity.this, PillDetailActivity.class);
            intent.putExtra("selectedPill", pill);  // `PillInfo` 객체 그대로 전달
            startActivity(intent);
        });
        pillListView.setAdapter(adapter);
    }

    @Override
    protected void handleEmptyResults() {
        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
