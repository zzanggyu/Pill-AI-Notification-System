package com.example.finalpillapp.NameSearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.RecognizePill.RecognizePillActivity;
import com.example.finalpillapp.Main.MainActivity;
import com.example.pillapp.R;

public class RecentSearchesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_search); // 이 레이아웃 파일로 연결됨

        // ListView 참조
        ListView recentSearchList = findViewById(R.id.recent_search_list);

        // RecognizePillActivity로 이동 버튼
        Button btnGoToRecognizePill = findViewById(R.id.home_button); // 홈 버튼으로 예시 설정
        btnGoToRecognizePill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecentSearchesActivity.this, RecognizePillActivity.class);
                startActivity(intent);
            }
        });

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료하여 이전 화면으로 돌아감
            }
        });

        // 네비게이션 바 버튼 예시
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecentSearchesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
