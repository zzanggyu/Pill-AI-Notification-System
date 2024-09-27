package com.example.pillapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    private TextView tvSearchResultsTitle;
    private RecyclerView rvSearchResults;
    private SearchResultsAdapter adapter;
    private List<String> searchResultsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // View 초기화
        tvSearchResultsTitle = findViewById(R.id.tv_search_results_title);
        rvSearchResults = findViewById(R.id.rv_search_results);

        // 인텐트로부터 검색어 가져오기
        String searchQuery = getIntent().getStringExtra("search_query");

        // 검색어가 있으면 타이틀에 표시
        if (searchQuery != null) {
            tvSearchResultsTitle.setText("'" + searchQuery + "' 검색 결과");
        }

        // RecyclerView 설정
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // 검색 결과 데이터 추가 (임시 데이터)
        searchResultsList = new ArrayList<>();
        searchResultsList.add("약 A");
        searchResultsList.add("약 B");
        searchResultsList.add("약 C");
        searchResultsList.add("약 D");

        // 어댑터 설정
        adapter = new SearchResultsAdapter(searchResultsList);
        rvSearchResults.setAdapter(adapter);
    }
}
