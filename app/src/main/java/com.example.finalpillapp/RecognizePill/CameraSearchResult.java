package com.example.finalpillapp.RecognizePill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.finalpillapp.RecognizePill.BaseListActivity;
import com.example.finalpillapp.SearchPill.PillDetailActivity;
import com.example.pillapp.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraSearchResult extends BaseListActivity {

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);

        String base64Image = getIntent().getStringExtra("base64Image");
        if (base64Image != null) {
            uploadImageToServer(base64Image);
        }
    }

    private void uploadImageToServer(String base64Image) {
        PillImageRequest request = new PillImageRequest(base64Image);
        Call<ApiResponse<List<PillInfo>>> call = apiService.analyzePill(request);

        call.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PillInfo>>> call, @NonNull Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PillInfo> pillInfoList = response.body().getData();
                    displayPillInfo(pillInfoList);
                } else {
                    Toast.makeText(CameraSearchResult.this, "알약 분석 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PillInfo>>> call, @NonNull Throwable t) {
                Toast.makeText(CameraSearchResult.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
