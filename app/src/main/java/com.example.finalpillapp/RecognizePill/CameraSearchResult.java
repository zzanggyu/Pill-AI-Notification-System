package com.example.finalpillapp.RecognizePill;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pillapp.R;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class CameraSearchResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // Retrieve and parse JSON data from the intent
        String pillInfoJson = getIntent().getStringExtra("pillInfoList");
        if (pillInfoJson != null) {
            List<PillInfo> pillInfoList = new Gson().fromJson(pillInfoJson, new TypeToken<List<PillInfo>>() {}.getType());

            // Display or handle the pillInfoList as needed
            for (PillInfo pillInfo : pillInfoList) {
                Toast.makeText(this, "알약 이름: " + pillInfo.getItemName(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "알약 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
