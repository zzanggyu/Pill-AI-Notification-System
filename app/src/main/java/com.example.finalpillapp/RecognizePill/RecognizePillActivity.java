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

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize_pill);

        // 카메라 버튼 설정
        ImageButton btnCaptureImage = findViewById(R.id.btn_capture_image);
        btnCaptureImage.setOnClickListener(v -> {
            Intent instructionIntent = new Intent(RecognizePillActivity.this, CameraInstructionActivity.class);
            startActivity(instructionIntent);
        });

        // 갤러리에서 선택 버튼 설정
        ImageButton btnOpenGallery = findViewById(R.id.btn_open_gallery);
        btnOpenGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(RecognizePillActivity.this, GalleryActivity.class);
            startActivity(galleryIntent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            uploadImageToServer(imageBitmap);
        }
    }

    private void uploadImageToServer(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // API 호출 코드 (전달된 이미지를 서버에 전송)
        Intent intent = new Intent(this, CameraSearchResult.class);
        intent.putExtra("base64Image", base64Image);
        startActivity(intent);
    }
}
