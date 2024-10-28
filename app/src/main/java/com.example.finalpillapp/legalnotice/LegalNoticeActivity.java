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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LegalNoticeActivity extends AppCompatActivity {
    private static final String TAG = "LegalNoticeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_notice);

        ImageButton btnAgreeStart = findViewById(R.id.agree_button);

        btnAgreeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "동의 버튼 클릭됨");
                sendAgreementToServer("user_id_value"); // 실제 사용자 ID로 변경해야 함
            }
        });
    }

    private void sendAgreementToServer(String userId) {
        Log.d(TAG, "서버에 동의 정보 전송 시작");
        ApiService apiService = RetrofitClientInstance.getApiService();

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Log.d(TAG, "생성된 현재 날짜: " + currentDate);

        LegalNoticeRequest request = new LegalNoticeRequest(userId, currentDate, true);

        // 요청 정보 로깅
        Log.d(TAG, "요청 URL: " + RetrofitClientInstance.BASE_URL + "legal-notice");
        Log.d(TAG, "요청 데이터: userId=" + request.getUserId() +
                ", date=" + request.getDate() +
                ", accepted=" + request.isAccepted());

        Call<ApiResponse<Void>> call = apiService.sendLegalNotice(request);

        // 요청 자체의 디버그 정보
        Log.d(TAG, "Request URL: " + call.request().url());
        Log.d(TAG, "Request Method: " + call.request().method());
        Log.d(TAG, "Request Headers: " + call.request().headers());

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                Log.d(TAG, "서버 응답 받음: " + response.code());

                if (response.isSuccessful()) {
                    ApiResponse<Void> apiResponse = response.body();
                    Log.d(TAG, "응답 바디: " + apiResponse);

                    if (apiResponse != null && apiResponse.isSuccess()) {
                        Log.d(TAG, "서버 응답 성공");
                        Intent intent = new Intent(LegalNoticeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = apiResponse != null ? apiResponse.getMessage() : "Unknown error";
                        Log.e(TAG, "서버 응답 실패: " + errorMessage);
                        showError("서버 응답 실패: " + errorMessage);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "No error body";
                        Log.e(TAG, "HTTP 에러: " + response.code());
                        Log.e(TAG, "에러 응답: " + errorBody);
                        Log.e(TAG, "Response Headers: " + response.headers());
                        showError("서버 오류 (" + response.code() + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "에러 바디 읽기 실패", e);
                        showError("서버 오류");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e(TAG, "네트워크 에러 발생", t);
                Log.e(TAG, "Failed URL: " + call.request().url());
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Error cause: " + t.getCause());
                showError("네트워크 오류: " + t.getMessage());

                // 네트워크 에러 상세 정보 출력
                if (t instanceof java.net.SocketTimeoutException) {
                    Log.e(TAG, "연결 시간 초과");
                } else if (t instanceof java.net.UnknownHostException) {
                    Log.e(TAG, "호스트를 찾을 수 없음");
                } else if (t instanceof java.net.ConnectException) {
                    Log.e(TAG, "서버에 연결할 수 없음");
                }

                // MainActivity로 이동
                Intent intent = new Intent(LegalNoticeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(LegalNoticeActivity.this, message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: " + message);
        });
    }
}