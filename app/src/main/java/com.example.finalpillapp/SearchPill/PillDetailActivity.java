// PillDetailActivity.java
package com.example.finalpillapp.SearchPill;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.pillapp.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PillDetailActivity extends AppCompatActivity {

    private static final String RECENT_SEARCH_PREFS = "RecentSearchPrefs";
    private static final String RECENT_SEARCH_KEY = "RecentSearch";
    private static final int MAX_RECENT_ITEMS = 10;

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

        ImageButton addMedicationButton = findViewById(R.id.add_to_favorites_button);

        // UI에 데이터 설정
        pillNameTextView.setText(pill.getItemName());
        pillEffectTextView.setText("효능: " + pill.getEfcyQesitm());
        Picasso.get().load(pill.getImageUrl()).into(pillImageView);

        // "복용 중인 약 추가" 버튼 클릭 리스너 설정
        addMedicationButton.setOnClickListener(v -> {
            addMedicationToDatabase();
            saveToRecentSearch(pill);  // 최근 본 알약에 저장
        });
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
        pillJson.addProperty("SideEffects", pill.getSideEffects());
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

    // 최근 본 알약 SharedPreferences에 저장
    private void saveToRecentSearch(PillInfo pillInfo) {
        SharedPreferences sharedPreferences = getSharedPreferences(RECENT_SEARCH_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // 기존 목록 불러오기
        String recentPillsJson = sharedPreferences.getString(RECENT_SEARCH_KEY, null);
        List<PillInfo> recentPills;

        if (recentPillsJson != null) {
            Type type = new TypeToken<List<PillInfo>>() {}.getType();
            recentPills = gson.fromJson(recentPillsJson, type);
        } else {
            recentPills = new ArrayList<>();
        }

        // 중복 제거: 같은 알약이 있으면 기존 항목 삭제
        for (int i = 0; i < recentPills.size(); i++) {
            if (recentPills.get(i).getItemSeq().equals(pillInfo.getItemSeq())) {
                recentPills.remove(i);
                break;
            }
        }

        // 새로운 알약 추가 (최대 10개 유지)
        recentPills.add(0, pillInfo);  // 최신 알약을 맨 앞으로 추가
        if (recentPills.size() > MAX_RECENT_ITEMS) {
            recentPills.remove(recentPills.size() - 1);  // 가장 오래된 항목 삭제
        }

        // 변경된 목록 저장
        String updatedPillsJson = gson.toJson(recentPills);
        editor.putString(RECENT_SEARCH_KEY, updatedPillsJson);
        editor.apply();
    }
}
