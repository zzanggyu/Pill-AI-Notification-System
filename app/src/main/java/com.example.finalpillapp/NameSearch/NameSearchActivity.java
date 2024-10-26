package com.example.finalpillapp.NameSearch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.Main.MainActivity;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.SearchPill.SearchResultsActivity;
import com.example.pillapp.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NameSearchActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText searchInput;
    private ImageButton searchButton;
    private ImageButton backButton;  // 뒤로 가기 버튼 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_search);

        // UI 요소 초기화
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        backButton = findViewById(R.id.back_button);  // backButton 초기화 추가

        // 검색 버튼 클릭 리스너 설정
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = searchInput.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    searchPillsByName(searchText);
                } else {
                    Toast.makeText(NameSearchActivity.this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 뒤로 가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity로 이동
                Intent intent = new Intent(NameSearchActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });
    }

    // 이름 기반 알약 검색 메서드
    private void searchPillsByName(String name) {
        Call<ApiResponse<List<PillInfo>>> call = apiService.searchPillsByName(name);
        call.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PillInfo>>> call, Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PillInfo> searchResults = response.body().getData();
                    if (searchResults != null && !searchResults.isEmpty()) {
                        // SearchResultsActivity로 검색 결과를 전달하고 전환
                        Intent intent = new Intent(NameSearchActivity.this, SearchResultsActivity.class);
                        intent.putParcelableArrayListExtra("pillList", new ArrayList<>(searchResults));
                        startActivity(intent);
                    } else {
                        Toast.makeText(NameSearchActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NameSearchActivity.this, "서버 오류: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("NameSearchActivity", "서버 오류: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PillInfo>>> call, Throwable t) {
                Toast.makeText(NameSearchActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("NameSearchActivity", "네트워크 오류: " + t.getMessage());
            }
        });
    }
}
