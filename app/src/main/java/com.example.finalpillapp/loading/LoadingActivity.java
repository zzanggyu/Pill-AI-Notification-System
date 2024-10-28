// LoadingActivity.java
package com.example.finalpillapp.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.Main.MainActivity;
import com.example.finalpillapp.legalnotice.LegalNoticeActivity;
import com.example.pillapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {

    private static final String USER_ID = "user_id_value"; // 실제 사용자 ID로 변경

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        checkUserAgreement(USER_ID);
    }

    private void checkUserAgreement(String userId) {
        ApiService apiService = RetrofitClientInstance.getApiService();
        Call<ApiResponse<Void>> call = apiService.checkLegalNotice(userId);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 동의한 사용자일 경우 MainActivity로 이동
                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                } else {
                    // 동의하지 않은 경우 LegalNoticeActivity로 이동
                    startActivity(new Intent(LoadingActivity.this, LegalNoticeActivity.class));
                }
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(LoadingActivity.this, "서버 연결 실패. 동의 여부 확인 불가", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoadingActivity.this, LegalNoticeActivity.class));
                finish();
            }
        });
    }
}
