package com.example.finalpillapp.RecognizePill;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pillapp.R;
import java.io.ByteArrayOutputStream;

public class RecognizePillActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1; // 카메라 요청 코드 상수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_pill);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 카메라 버튼 설정: CameraInstructionActivity로 이동
        ImageButton btnCaptureImage = findViewById(R.id.btn_capture_image);
        btnCaptureImage.setOnClickListener(v -> {
            Intent instructionIntent = new Intent(RecognizePillActivity.this, CameraInstructionActivity.class);
            startActivity(instructionIntent);
        });

        // 갤러리 버튼 설정: GalleryActivity로 이동
        ImageButton btnOpenGallery = findViewById(R.id.btn_open_gallery);
        btnOpenGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(RecognizePillActivity.this, GalleryActivity.class);
            startActivity(galleryIntent);
        });
    }

    // onActivityResult: 카메라에서 촬영한 이미지 결과를 처리하는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            // 촬영된 이미지를 비트맵으로 가져옴
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            uploadImageToServer(imageBitmap); // 서버에 이미지 업로드
        }
    }

    // 이미지 서버 업로드 메서드
    private void uploadImageToServer(Bitmap bitmap) {
        // 비트맵 이미지를 JPEG로 압축하고 바이트 배열로 변환
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT); // Base64 문자열로 인코딩

        // 인코딩된 이미지를 CameraSearchResult 액티비티로 전달
        Intent intent = new Intent(this, CameraSearchResult.class);
        intent.putExtra("base64Image", base64Image); // 이미지 데이터를 인텐트에 추가
        startActivity(intent); // CameraSearchResult 액티비티 시작
    }
}
