package com.example.finalpillapp.SearchPill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillImformation.ManufacturerActivity;
import com.example.finalpillapp.PillImformation.PillInfoActivity;
import com.example.finalpillapp.PillImformation.PreparationActivity;
import com.example.finalpillapp.PillImformation.SideEffectsActivity;
import com.example.finalpillapp.PillImformation.UsageActivity;
import com.example.finalpillapp.PillImformation.WarningActivity;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.pillapp.R;
import com.squareup.picasso.Picasso;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PillDetailActivity extends AppCompatActivity {

    private PillInfo pill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_details);

        Log.d("PillDetailActivity", "Activity 시작됨");

        // Intent로 전달된 PillInfo 객체 복원
        pill = getIntent().getParcelableExtra("selectedPill");
        if (pill == null) {
            Log.e("PillDetailActivity", "Pill 객체가 null입니다.");
            Toast.makeText(this, "약 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.d("PillDetailActivity", "Pill 객체 로드됨: " + pill.getItemName());
        }

        // UI 요소 초기화 및 데이터 설정
        setupUI();
    }

    // UI 요소 초기화 및 PillInfo 데이터 설정
    private void setupUI() {
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageView pillImageView = findViewById(R.id.pill_image);
        TextView pillNameTextView = findViewById(R.id.pill_name);
        TextView pillEffectTextView = findViewById(R.id.pill_effect);

        // 데이터 설정
        pillNameTextView.setText(pill.getItemName());
        pillEffectTextView.setText("효능: " + pill.getEfcyQesitm());
        Picasso.get().load(pill.getImageUrl()).into(pillImageView);

        // 각 상세 정보 버튼 설정
        setupDetailButtons();
    }

    // 각 상세 정보 버튼에 클릭 리스너 설정
    private void setupDetailButtons() {
        ImageButton detailsButton = findViewById(R.id.details_button);
        ImageButton warningButton = findViewById(R.id.warning_button);
        ImageButton usageButton = findViewById(R.id.usage_button);
        ImageButton sideEffectsButton = findViewById(R.id.side_effects_button);
        ImageButton preparationButton = findViewById(R.id.preparation_button);
        ImageButton manufacturerButton = findViewById(R.id.manufacturer_button);

        // 각 버튼 클릭 시 해당 화면으로 이동
        detailsButton.setOnClickListener(v -> startActivity(new Intent(this, PillInfoActivity.class)));
        warningButton.setOnClickListener(v -> startActivity(new Intent(this, WarningActivity.class)));
        usageButton.setOnClickListener(v -> startActivity(new Intent(this, UsageActivity.class)));
        sideEffectsButton.setOnClickListener(v -> startActivity(new Intent(this, SideEffectsActivity.class)));
        preparationButton.setOnClickListener(v -> startActivity(new Intent(this, PreparationActivity.class)));
        manufacturerButton.setOnClickListener(v -> startActivity(new Intent(this, ManufacturerActivity.class)));
    }

    // 사용자의 복용 약 목록에 약물 추가
    private void addMedicationToDatabase() {
        String userId = DeviceUtil.getDeviceId(this);
        Log.d("PillDetailActivity", "기기 ID: " + userId);

        // 서버로 전달할 약물 정보 JSON 생성
        JsonObject pillJson = new JsonObject();
        pillJson.addProperty("user_id", userId);
        pillJson.addProperty("itemSeq", pill.getItemSeq());
        pillJson.addProperty("itemName", pill.getItemName());
        pillJson.addProperty("efcyQesitm", pill.getEfcyQesitm());
        pillJson.addProperty("atpnQesitm", pill.getAtpnQesitm());
        pillJson.addProperty("seQesitm", pill.getEfficacy());
        pillJson.addProperty("etcotc", pill.getEtcotc());
        pillJson.addProperty("itemImage", pill.getImageUrl());

        ApiService apiService = RetrofitClientInstance.getApiService();
        Call<ResponseBody> call = apiService.addPill(pillJson);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "약물이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    Log.d("PillDetailActivity", "약물 추가 성공");
                } else {
                    Toast.makeText(getApplicationContext(), "약물 추가 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("PillDetailActivity", "약물 추가 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PillDetailActivity", "서버 오류: " + t.getMessage(), t);
            }
        });
    }
}
