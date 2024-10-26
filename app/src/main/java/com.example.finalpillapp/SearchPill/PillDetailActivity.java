package com.example.finalpillapp.SearchPill;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.example.pillapp.R;

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

        pill = getIntent().getParcelableExtra("selectedPill");
        if (pill == null) {
            Log.e("PillDetailActivity", "Pill 객체가 null입니다. 인텐트 데이터가 전달되지 않았습니다.");
            Toast.makeText(this, "약 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            Log.d("PillDetailActivity", "Pill 객체가 정상적으로 로드됨: " + pill.getItemName());
        }

        // UI 요소 찾기
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageView pillImageView = findViewById(R.id.pill_image);
        TextView pillNameTextView = findViewById(R.id.pill_name);
        TextView pillEffectTextView = findViewById(R.id.pill_effect);

        Button addMedicationButton = findViewById(R.id.add_to_favorites_button);

        // UI에 데이터 설정
        pillNameTextView.setText(pill.getItemName());
        pillEffectTextView.setText("효능: " + pill.getEfcyQesitm());
        Picasso.get().load(pill.getItemImage()).into(pillImageView);

        // "복용 중인 약 추가" 버튼 클릭 리스너 설정
        addMedicationButton.setOnClickListener(v -> addMedicationToDatabase());
    }

    private void addMedicationToDatabase() {
        String userId = DeviceUtil.getDeviceId(this);
        Log.d("PillDetailActivity", "기기 ID: " + userId);

        JsonObject pillJson = new JsonObject();
        pillJson.addProperty("user_id", userId);
        pillJson.addProperty("itemSeq", pill.getItemSeq());
        pillJson.addProperty("itemName", pill.getItemName());
        pillJson.addProperty("efcyQesitm", pill.getEfcyQesitm());
        pillJson.addProperty("atpnQesitm", pill.getAtpnQesitm());
        pillJson.addProperty("seQesitm", pill.getSeQesitm());
        pillJson.addProperty("etcotc", pill.getEtcotc());
        pillJson.addProperty("itemImage", pill.getItemImage());

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
