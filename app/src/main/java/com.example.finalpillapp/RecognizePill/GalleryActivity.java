package com.example.finalpillapp.RecognizePill;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.pillapp.R;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY_PERMISSION = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private GridView photoGrid;
    private ImageButton selectButtonInactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galary);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        photoGrid = findViewById(R.id.photo_grid);
        selectButtonInactive = findViewById(R.id.select_button_inactive);

        // 사진 선택 버튼
        selectButtonInactive.setOnClickListener(v -> {
            if (checkGalleryPermission()) {
                openGallery();
            } else {
                requestGalleryPermission();
            }
        });

        // 권한 확인 후 갤러리 이미지 로드
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

    // 갤러리 열기 메서드 추가
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 선택된 이미지 URI를 비트맵으로 변환하여 서버로 전송
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                uploadImageToServer(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 서버에 이미지 업로드 메서드
    private void uploadImageToServer(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ApiResponse<List<PillInfo>>> call = apiService.analyzePill(new PillImageRequest(base64Image));

        call.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PillInfo>>> call, @NonNull Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PillInfo> pillInfoList = response.body().getData();
                    Intent intent = new Intent(GalleryActivity.this, CameraSearchResult.class);
                    intent.putExtra("pillInfoList", new Gson().toJson(pillInfoList));
                    startActivity(intent);
                } else {
                    Toast.makeText(GalleryActivity.this, "알약 분석 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PillInfo>>> call, @NonNull Throwable t) {
                Toast.makeText(GalleryActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    // 갤러리 이미지 로드
    private void loadGalleryImages() {
        ArrayList<String> imagePaths = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    imagePaths.add(path);
                }
            }
        }

        ImageAdapter imageAdapter = new ImageAdapter(this, imagePaths);
        photoGrid.setAdapter(imageAdapter);
    }
}
