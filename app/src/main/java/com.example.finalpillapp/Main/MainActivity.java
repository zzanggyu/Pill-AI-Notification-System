package com.example.finalpillapp.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.Alarm.AlertSettingsActivity;
import com.example.finalpillapp.MyPage.MyPageActivity;
import com.example.finalpillapp.NameSearch.NameSearchActivity;  // 이름 검색 액티비티
import com.example.finalpillapp.NameSearch.RecentSearchesActivity;
import com.example.finalpillapp.SearchPill.SearchActivity;  // 증상 검색 액티비티
import com.example.finalpillapp.NameSearch.RecentSearchesActivity;  // 최근 검색 액티비티
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.RecognizePill.RecognizePillActivity;
import com.example.pillapp.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText searchEditText;
    private ImageButton searchButton;  // 이름으로 검색 버튼
    private ImageButton recognizePillButton;  // 카메라로 검색 버튼
    private ImageButton symptomSearchButton;  // 증상으로 검색 버튼
    private Button alarmSettingsButton;  // 알림 설정 버튼
    private Button homeButton;  // 홈 버튼
    private Button myPageButton;  // 마이페이지 버튼
    private ImageButton historyButton;  // 최근 검색한 약 버튼
    private RecyclerView recentSearchesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기존 기능 초기화
        searchButton = findViewById(R.id.search_by_name);  // 이름으로 검색 버튼
        recognizePillButton = findViewById(R.id.search_by_camera);  // 카메라로 검색 버튼
        symptomSearchButton = findViewById(R.id.search_by_symptom);  // 증상으로 검색 버튼
        alarmSettingsButton = findViewById(R.id.alarm_button);  // 알림 설정 버튼
        homeButton = findViewById(R.id.home_button);  // 홈 버튼
        myPageButton = findViewById(R.id.profile_button);  // 마이페이지 버튼
        historyButton = findViewById(R.id.history_button);  // 최근 검색한 약 버튼

        // API 서비스 초기화
        apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);

        // 검색 버튼 클릭 리스너 설정 (이름으로 검색)
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NameSearchActivity.class);  // 이름으로 검색 화면으로 이동
                startActivity(intent);
            }
        });

        // 카메라로 검색 버튼 클릭 리스너 설정
        recognizePillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecognizePillActivity.class);  // 알약 인식 화면으로 이동
                startActivity(intent);
            }
        });

        // 증상으로 검색 버튼 클릭 리스너 설정
        symptomSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);  // 증상 검색 화면으로 이동
                startActivity(intent);
            }
        });

        // 알림 설정 버튼 클릭 리스너 설정
        alarmSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AlertSettingsActivity.class);  // 알림 설정 화면으로 이동
                startActivity(intent);
            }
        });

        // 마이페이지 버튼 클릭 리스너 설정
        myPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyPageActivity.class);  // 마이페이지 화면으로 이동
                startActivity(intent);
            }
        });

        // 최근 검색한 약 버튼 클릭 리스너 설정
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecentSearchesActivity.class);  // 최근 검색한 약 화면으로 이동
                startActivity(intent);
            }
        });
    }

    // API 호출 메서드: 이름 기반과 증상 기반 검색 모두 포함
    private void searchDrug(String drugName, String symptom) {
        // 이름 기반 알약 검색
        Call<ApiResponse<List<PillInfo>>> callByName = apiService.searchPillsByName(drugName);
        callByName.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PillInfo>>> call, Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleResponse(response.body().getData());  // List<PillInfo> 전달
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PillInfo>>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "이름 검색 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 증상 기반 알약 검색
        Call<ApiResponse<List<PillInfo>>> callBySymptom = apiService.searchPillsBySymptom(symptom, new ArrayList<>());
        callBySymptom.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PillInfo>>> call, Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleResponse(response.body().getData());  // List<PillInfo> 전달
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PillInfo>>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "증상 검색 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 검색 결과를 처리하는 메서드
    private void handleResponse(List<PillInfo> pills) {
        if (pills != null && !pills.isEmpty()) {
            // RecyclerView 어댑터를 이용해 데이터를 화면에 표시하는 로직 추가
            for (PillInfo pill : pills) {
                // API 응답 데이터 로그
            }
        } else {
            Toast.makeText(MainActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
