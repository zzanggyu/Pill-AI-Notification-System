package com.example.finalpillapp.RecognizePill;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pillapp.R;

import java.util.ArrayList;
import com.example.finalpillapp.RecognizePill.ImageAdapter;

public class GalleryActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY_PERMISSION = 1;
    private GridView photoGrid;
    private ImageButton selectButtonInactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galary);

        // UI 요소 초기화
        photoGrid = findViewById(R.id.photo_grid);
        selectButtonInactive = findViewById(R.id.select_button_inactive);
        ImageButton backButton = findViewById(R.id.back_button);
        Button alarmButton = findViewById(R.id.alarm_button);
        Button homeButton = findViewById(R.id.home_button);
        Button profileButton = findViewById(R.id.profile_button);

        // 뒤로가기 버튼 설정
        backButton.setOnClickListener(v -> finish());

        // 사진 선택 버튼 (비활성화 상태)
        selectButtonInactive.setOnClickListener(v -> {
            if (checkGalleryPermission()) {
                loadGalleryImages();
            } else {
                requestGalleryPermission();
            }
        });

        // 하단 네비게이션 버튼 기능 설정
        alarmButton.setOnClickListener(v -> Toast.makeText(this, "알림 기능 연결", Toast.LENGTH_SHORT).show());
        homeButton.setOnClickListener(v -> Toast.makeText(this, "홈 화면으로 이동", Toast.LENGTH_SHORT).show());
        profileButton.setOnClickListener(v -> Toast.makeText(this, "프로필 화면으로 이동", Toast.LENGTH_SHORT).show());

        // 권한 확인 후 이미지 로드
        if (checkGalleryPermission()) {
            loadGalleryImages();
        } else {
            requestGalleryPermission();
        }
    }

    private boolean checkGalleryPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
        }
    }

    // 갤러리의 모든 이미지 불러오기
    private void loadGalleryImages() {
        ArrayList<String> imagePaths = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = contentResolver.query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    imagePaths.add(path);
                }
            }
        }

        // 이미지 어댑터 설정
        ImageAdapter imageAdapter = new ImageAdapter(this, imagePaths);
        photoGrid.setAdapter(imageAdapter);
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadGalleryImages();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
