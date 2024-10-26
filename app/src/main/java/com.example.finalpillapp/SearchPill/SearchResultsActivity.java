package com.example.finalpillapp.SearchPill;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.RecognizePill.BaseListActivity;

import java.util.List;

public class SearchResultsActivity extends BaseListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<PillInfo> searchResults = getIntent().getParcelableArrayListExtra("pillList");
        if (searchResults != null && !searchResults.isEmpty()) {
            Log.d("SearchResultsActivity", "Received search results: " + searchResults.size());
            displayPillInfo(searchResults);
        } else {
            handleEmptyResults();
        }
    }
}
