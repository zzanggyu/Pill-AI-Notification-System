// LegalNoticeActivity.java
package com.example.finalpillapp.legalnotice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.Main.MainActivity;
import com.example.pillapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LegalNoticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_notice);

        ImageButton btnAgreeStart = findViewById(R.id.agree_button);

        btnAgreeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 서버에 사용자 동의 정보 전송
                sendAgreementToServer("user_id_value"); // 실제 사용자 ID로 변경
            }
        });
    }

    private void sendAgreementToServer(String userId) {
        ApiService apiService = RetrofitClientInstance.getApiService();

        // 동의 요청 객체 생성
        LegalNoticeRequest request = new LegalNoticeRequest(userId, true);

        Call<ApiResponse<Void>> call = apiService.saveLegalNotice(request);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 동의 정보가 성공적으로 서버에 기록된 경우
                    startActivity(new Intent(LegalNoticeActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LegalNoticeActivity.this, "서버에 동의 정보 전송 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(LegalNoticeActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                Log.e("LegalNoticeActivity", "서버 호출 실패", t);
            }
        });
    }
}
