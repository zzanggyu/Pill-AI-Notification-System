package com.example.pillapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class PhotoSearchActivity extends AppCompatActivity {
    private Button btnOpenCamera, btnOpenGallery, btnHomeFromPhotoSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_search);

        btnOpenCamera = findViewById(R.id.btn_open_camera);
        btnOpenGallery = findViewById(R.id.btn_open_gallery);
        btnHomeFromPhotoSearch = findViewById(R.id.btn_home_from_photo_search);

        // 카메라 열기 (추후 구현 필요)
        btnOpenCamera.setOnClickListener(v -> {
            // 카메라 열기 로직 구현
        });

        // 갤러리 열기 (추후 구현 필요)
        btnOpenGallery.setOnClickListener(v -> {
            // 갤러리에서 사진 선택 로직 구현
        });

        // 메인 화면으로 돌아가는 버튼
        btnHomeFromPhotoSearch.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoSearchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
