package com.example.finalpillapp.RecognizePill;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.util.Base64;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pillapp.R;
import com.example.finalpillapp.API.ApiResponse;
import com.example.finalpillapp.API.ApiService;
import com.example.finalpillapp.API.RetrofitClientInstance;
import com.example.finalpillapp.PillInfo.PillInfo;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraInstructionActivity extends AppCompatActivity {

    private TextureView cameraPreview; // 카메라 프리뷰를 위한 TextureView
    private CameraDevice cameraDevice; // 카메라 장치 객체
    private CameraCaptureSession captureSession; // 카메라 캡처 세션
    private CaptureRequest.Builder captureRequestBuilder; // 캡처 요청 빌더
    private Size previewSize; // 프리뷰 화면 크기
    private ImageButton captureButton; // 사진 촬영 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 카메라 프리뷰 설정
        cameraPreview = findViewById(R.id.camera_preview);
        cameraPreview.setSurfaceTextureListener(textureListener);

        // 사진 촬영 버튼 설정
        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(v -> takePicture());
    }

    // TextureView의 SurfaceTextureListener 설정
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(); // 카메라 열기
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };

    // 카메라 열기 메서드
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0]; // 기본 카메라 ID 가져오기
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            previewSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(SurfaceTexture.class)[0]; // 프리뷰 크기 설정

            // 카메라 권한 체크
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
                return;
            }

            // 카메라 열기
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "카메라에 접근할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 카메라 장치의 상태 콜백 설정
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview(); // 카메라 프리뷰 생성
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close(); // 연결 해제 시 카메라 닫기
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close(); // 에러 발생 시 카메라 닫기
            cameraDevice = null;
        }
    };

    // 카메라 프리뷰 생성 메서드
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface); // 프리뷰 화면 설정

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    captureSession = session;
                    updatePreview(); // 프리뷰 업데이트
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(CameraInstructionActivity.this, "카메라 설정 실패", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 카메라 프리뷰 업데이트 메서드
    private void updatePreview() {
        if (cameraDevice == null) return;

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 사진 촬영 메서드
    private void takePicture() {
        if (cameraDevice == null) return;

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            Surface surface = new Surface(texture);

            captureBuilder.addTarget(surface);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            captureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    // 촬영된 이미지를 비트맵으로 변환하고 서버에 업로드
                    Bitmap bitmap = textureToBitmap(texture);
                    uploadImageToServer(bitmap);
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // TextureView에서 비트맵으로 변환
    private Bitmap textureToBitmap(SurfaceTexture texture) {
        return cameraPreview.getBitmap();
    }

    // 서버에 이미지 업로드
    private void uploadImageToServer(Bitmap bitmap) {
        // 비트맵 이미지를 Base64 문자열로 변환
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // API 요청 생성 및 호출
        PillImageRequest request = new PillImageRequest(base64Image);
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<ApiResponse<List<PillInfo>>> call = apiService.analyzePill(request);

        call.enqueue(new Callback<ApiResponse<List<PillInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PillInfo>>> call, @NonNull Response<ApiResponse<List<PillInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PillInfo>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        List<PillInfo> pillInfoList = apiResponse.getData();

                        // 분석된 정보 전달을 위한 CameraSearchResult 액티비티로 전환
                        Intent intent = new Intent(CameraInstructionActivity.this, CameraSearchResult.class);
                        intent.putExtra("pillInfoList", new Gson().toJson(pillInfoList));
                        startActivity(intent);
                    } else {
                        Toast.makeText(CameraInstructionActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(CameraInstructionActivity.this, "알약 분석 실패: 서버 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<PillInfo>>> call, @NonNull Throwable t) {
                Toast.makeText(CameraInstructionActivity.this, "API 호출 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
