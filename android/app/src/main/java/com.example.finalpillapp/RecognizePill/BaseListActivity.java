package com.example.finalpillapp.RecognizePill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.SearchPill.PillAdapter;
import com.example.finalpillapp.SearchPill.PillDetailActivity;
import com.example.pillapp.R;

import java.util.List;

public abstract class BaseListActivity extends AppCompatActivity {

    protected ListView listView;
    protected PillAdapter pillAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        listView = findViewById(R.id.pill_list);
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    protected void displayPillInfo(List<PillInfo> pillInfoList) {
        pillAdapter = new PillAdapter(this, pillInfoList);
        listView.setAdapter(pillAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            PillInfo selectedPill = pillInfoList.get(position);
            Intent intent = new Intent(this, PillDetailActivity.class);
            intent.putExtra("selectedPill", selectedPill);
            startActivity(intent);
        });
    }

    protected void handleEmptyResults() {
        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
    }
}
