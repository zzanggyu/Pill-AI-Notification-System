package com.example.finalpillapp.SearchPill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.RecognizePill.CameraSearchResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText searchInput;
    private ImageButton searchButton;  // ImageButton으로 수정

    // 체크박스 변수 추가
    private CheckBox coldCheckBox, indigestionCheckBox, feverCheckBox, headacheCheckBox, dizzinessCheckBox, insomniaCheckBox, fatigueCheckBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.pillapp.R.layout.activity_search);

        // UI 요소 초기화
        searchInput = findViewById(com.example.pillapp.R.id.search_edit_text);  // 수정된 ID 사용
        searchButton = findViewById(com.example.pillapp.R.id.search_button);  // ImageButton으로 수정
        coldCheckBox = findViewById(com.example.pillapp.R.id.coldCheckBox);
        indigestionCheckBox = findViewById(com.example.pillapp.R.id.indigestionCheckBox);
        feverCheckBox = findViewById(com.example.pillapp.R.id.feverCheckBox);
        headacheCheckBox = findViewById(com.example.pillapp.R.id.headacheCheckBox);
        dizzinessCheckBox = findViewById(com.example.pillapp.R.id.dizzinessCheckBox);
        insomniaCheckBox = findViewById(com.example.pillapp.R.id.insomniaCheckBox);
        fatigueCheckBox = findViewById(com.example.pillapp.R.id.fatigueCheckBox);

        // Retrofit 인스턴스 생성
        apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);

        // 검색 버튼 클릭 리스너 설정
        searchButton.setOnClickListener(new View.OnClickListener() {  // ImageButton 클릭 리스너 설정
            @Override
            public void onClick(View view) {
                String searchText = searchInput.getText().toString().trim();
                List<String> selectedSymptoms = getSelectedSymptoms();

                if (!searchText.isEmpty() || !selectedSymptoms.isEmpty()) {
                    searchPills(searchText, selectedSymptoms);
                } else {
                    Toast.makeText(SearchActivity.this, "검색어 또는 증상을 선택하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 선택된 증상을 가져오는 메서드
    private List<String> getSelectedSymptoms() {
        List<String> selectedSymptoms = new ArrayList<>();
        if (coldCheckBox.isChecked()) selectedSymptoms.add("감기");
        if (indigestionCheckBox.isChecked()) selectedSymptoms.add("소화불량");
        if (feverCheckBox.isChecked()) selectedSymptoms.add("발열");
        if (headacheCheckBox.isChecked()) selectedSymptoms.add("두통");
        if (dizzinessCheckBox.isChecked()) selectedSymptoms.add("어지러움");
        if (insomniaCheckBox.isChecked()) selectedSymptoms.add("불면증");
        if (fatigueCheckBox.isChecked()) selectedSymptoms.add("피로");
        return selectedSymptoms;
    }

    // API 호출 메서드: 증상 및 검색어 기반 알약 검색
    private void searchPills(String searchText, List<String> symptoms) {
        Call<ApiResponse<List<PillInfo>>> call = apiService.searchPills(searchText, symptoms);
        call.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PillInfo>>> call, Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PillInfo> searchResults = response.body().getData();
                    if (searchResults != null && !searchResults.isEmpty()) {
                        // 검색 결과 처리: CameraSearchResult로 전환
                        Log.d("SearchActivity", "검색 결과 개수: " + searchResults.size());
                        Intent intent = new Intent(SearchActivity.this, CameraSearchResult.class);
                        intent.putExtra("pillInfoList", new Gson().toJson(searchResults));  // JSON으로 변환하여 전달
                        startActivity(intent);
                    } else {
                        Toast.makeText(SearchActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "서버 오류: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("SearchActivity", "서버 오류: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PillInfo>>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SearchActivity", "네트워크 오류: " + t.getMessage());
            }
        });
    }

}
