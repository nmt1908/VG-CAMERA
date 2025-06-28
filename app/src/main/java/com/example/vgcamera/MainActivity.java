package com.example.vgcamera;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Matrix;
import android.media.ExifInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.*;

import com.example.vgcamera.User;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements FaceAnalyzer.FaceDetectionListener {
    private PreviewView previewView;
    private FaceGraphicOverlay graphicOverlay;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    private boolean isTakingPhoto = false;
    private final OkHttpClient httpClient = new OkHttpClient();
    private long faceStraightStartTime = 0;
    private static final long STRAIGHT_FACE_DURATION = 1000;
    TextView alertTextView, labelName, nameTextView, labelCardId, cardIDTextView, labelSimilarity, similarityTextView,appTitle;
    ProgressBar loadingSpinner;
    private String currentCameraId = "0";
    private LinearLayout loadingContainer,userInfoPanel;


    private ImageView appLogo;
    private long IDLE_DELAY_MS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        if (!isInternetAvailable()) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();

        }
        listAvailableCameras();
        IDLE_DELAY_MS = getSharedPreferences("settings", MODE_PRIVATE).getInt("time_waiting", 1000);
        Log.e("IDLE_DELAY_MS", String.valueOf(IDLE_DELAY_MS));
        loadingContainer = findViewById(R.id.loadingContainer);
        userInfoPanel = findViewById(R.id.userInfoPanel);
        previewView = findViewById(R.id.previewView);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        alertTextView = findViewById(R.id.labelAlert);
        labelName = findViewById(R.id.labelUserName);
        nameTextView = findViewById(R.id.userName);
        labelCardId = findViewById(R.id.labelUserCardId);
        cardIDTextView = findViewById(R.id.userCardId);
        labelSimilarity = findViewById(R.id.labelUserSimilarity);
        similarityTextView = findViewById(R.id.userSimilarity);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        appTitle=findViewById(R.id.appTitle);
        appLogo =findViewById(R.id.appLogo);
        appLogo.setImageResource(R.drawable.logo);

        graphicOverlay.setCameraFacing(true); // camera trước

        String cameraId = getIntent().getStringExtra("camera_id");
//        Log.d("CurrentCamera","Camera current"+cameraId);
////        appTitle.setText(cameraId);
        if (cameraId == null) cameraId = "0"; // mặc định camera trước

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
//            startCamera(cameraId);
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

    }
//    private Runnable goIdleRunnable = () -> {
//        Intent intent = new Intent(MainActivity.this, IdleActivity.class);
//        intent.putExtra("camera_id", currentCameraId);
//        startActivity(intent);
//        finish();
//    };
    private void listAvailableCameras() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                String facingStr = (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT)
                        ? "FRONT"
                        : (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK)
                        ? "BACK"
                        : "UNKNOWN";

                Log.d("CameraID", "ID: " + cameraId + " - Facing: " + facingStr);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //    @Override
//    public void onAverageLuminance(double luminance) {
//        Log.d("MainActivity", "Luminance: " + luminance);
//
//        if (luminance < LOW_LIGHT_THRESHOLD) {
//            if (canSwitch) {
//                lowLightCount++;
//                if (lowLightCount >= LOW_LIGHT_FRAME_LIMIT) {
//                    lowLightCount = 0;
//                    canSwitch = false;
//                    Log.d("Switch Camera Called", "Switch Camera Called");
//                    startCamera("1");
//                }
//            }
//        } else if (luminance > HIGH_LIGHT_THRESHOLD) {
//            lowLightCount = 0;
//            if (!canSwitch) {
//                Log.d("Switch Camera Reset", "Lighting good, can switch again");
//                canSwitch = true;
//            }
//        } else {
//
//            Log.d("Lighting", "In mid-range, no state change");
//        }
//    }
    @Override
    public void onAverageLuminance(double luminance) {
//        Log.d("MainActivity", "Luminance: " + luminance);
//        appTitle.setText("Cam " + currentCameraId + " | Lum: " + luminance);
//
//        if (currentCameraId.equals("0")) {
//            // Chuyển từ front (0) → back (1) nếu tối
//            if (luminance < LOW_LIGHT_THRESHOLD) {
//                lowLightCount++;
//                if (lowLightCount >= LOW_LIGHT_FRAME_LIMIT && canSwitchFrontToBack) {
//                    lowLightCount = 0;
//                    canSwitchFrontToBack = false;
//                    canSwitchBackToFront = true; // cho phép chuyển ngược lại lần sau
//                    Log.d("Switch Camera", "0→1 (dark)");
//                    startCamera("1");
//                }
//            } else {
//                // Ánh sáng tốt trở lại → reset flag chuyển từ 0→1
//                lowLightCount = 0;
//                if (!canSwitchFrontToBack) {
//                    Log.d("Reset Flag", "Reset canSwitchFrontToBack");
//                    canSwitchFrontToBack = true;
//                }
//            }
//        } else {
//            // Chuyển từ back (1) → front (0) nếu sáng
//            if (luminance > HIGH_LIGHT_THRESHOLD) {
//                lowLightCount++;
//                if (lowLightCount >= LOW_LIGHT_FRAME_LIMIT && canSwitchBackToFront) {
//                    lowLightCount = 0;
//                    canSwitchBackToFront = false;
//                    canSwitchFrontToBack = true; // cho phép chuyển ngược lại lần sau
//                    Log.d("Switch Camera", "1→0 (bright)");
//                    startCamera("0");
//                }
//            } else {
//                // Ánh sáng kém trở lại → reset flag chuyển từ 1→0
//                lowLightCount = 0;
//                if (!canSwitchBackToFront) {
//                    Log.d("Reset Flag", "Reset canSwitchBackToFront");
//                    canSwitchBackToFront = true;
//                }
//            }
//        }
    }




    public void onFaceAreaLarge(boolean isLarge) {
//        runOnUiThread(() -> {
//            if (isLarge) {
//                if (isCountingDownToIdle) {
//                    idleHandler.removeCallbacks(goIdleRunnable);
//                    isCountingDownToIdle = false;
//                }
//            } else {
//                if (!isCountingDownToIdle) {
//                    idleHandler.postDelayed(goIdleRunnable, IDLE_DELAY_MS);
//                    isCountingDownToIdle = true;
//                }
//            }
//        });
    }

    private void uploadImageToApi(File file) {
        runOnUiThread(() -> {
            loadingContainer.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);
        });

        new Thread(() -> {
            long startTotalTime = System.currentTimeMillis(); // ⭐ Bắt đầu đo toàn bộ thời gian API

            try {
                Log.d("TIMECALL", "🔁 Start uploading image: " + file.getName());

                byte[] fileBytes;
                try (InputStream is = new FileInputStream(file)) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    fileBytes = buffer.toByteArray();
                }

                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date());

                // API chính
                RequestBody requestBodyPrimary = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image_file", file.getName(),
                                RequestBody.create(fileBytes, MediaType.parse("image/jpeg")))
                        .build();

                Request requestPrimary = new Request.Builder()
                        .url("http://10.13.32.51:5001/recognize-anti-spoofing")
                        .addHeader("X-API-Key", "vg_login_app")
                        .addHeader("X-Time", currentTime)
                        .post(requestBodyPrimary)
                        .build();

                OkHttpClient clientWithTimeout = httpClient.newBuilder()
                        .callTimeout(10, TimeUnit.SECONDS)
                        .build();

                Response response;
                String responseBody;
                boolean isFallback = false;

                try {
                    Log.d("TIMECALL", "🌐 Calling primary API (port 5001): http://10.13.32.51:5001/recognize-anti-spoofing");
                    long startApiTime = System.currentTimeMillis();

                    response = clientWithTimeout.newCall(requestPrimary).execute();

                    long durationApiTime = System.currentTimeMillis() - startApiTime;
                    Log.d("TIMECALL", "✅ Primary API responded in " + durationApiTime + " ms");
                    Log.d("TIMECALL", "✅ Primary API response code: " + response.code());

                    responseBody = response.body().string();
                    Log.d("TIMECALL", "✅ Primary API response body: " + responseBody);
                } catch (Exception ex) {
                    Log.e("TIMECALL", "❌ Primary API failed: " + ex.getMessage());
                    Log.d("TIMECALL", "🌐 Falling back to secondary API (port 8001): http://10.1.16.23:8001/api/x/fr/env/face_search");
                    isFallback = true;

                    RequestBody requestBodyFallback = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("env_token", "8d59d8d588f84fc0a24291b8c36b6206")
                            .addFormDataPart("image_file", file.getName(),
                                    RequestBody.create(file, MediaType.parse("image/jpeg")))
                            .build();

                    Request requestFallback = new Request.Builder()
                            .url("http://10.1.16.23:8001/api/x/fr/env/face_search")
                            .post(requestBodyFallback)
                            .build();

                    long startApiTime = System.currentTimeMillis();

                    response = httpClient.newCall(requestFallback).execute();

                    long durationApiTime = System.currentTimeMillis() - startApiTime;
                    Log.d("TIMECALL", "✅ Fallback API responded in " + durationApiTime + " ms");
                    Log.d("TIMECALL", "✅ Fallback API response code: " + response.code());

                    responseBody = response.body().string();
                    Log.d("TIMECALL", "✅ Fallback API response body: " + responseBody);
                }

                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    loadingContainer.setVisibility(View.GONE);
                });

                if (!response.isSuccessful()) {
                    Log.e("TIMECALL", (isFallback ? "❌ Fallback" : "❌ Primary") + " API failed with code: " + response.code());
                    if (response.code() == 400) {
                        handleRecognitionFail();
                    } else {
                        showToastOnMainThread("API error: " + response.code());
                    }
                    return;
                }

                JSONObject jsonObject = new JSONObject(responseBody);

                if (jsonObject.optBoolean("is_fake", false)) {
                    Log.d("TIMECALL", "🛑 Detected spoofed face.");
                    handleRecognitionFail();
                    return;
                }

                if (jsonObject.optInt("is_recognized", 0) == 1) {
                    String name = jsonObject.optString("name");
                    String cardId = jsonObject.optString("id_string");
                    double similarityVal = jsonObject.optDouble("similarity", 0) * 100;

                    if (similarityVal <= 55) {
                        Log.d("TIMECALL", "🟡 Similarity too low: " + similarityVal);
                        handleRecognitionFail();
                        return;
                    }

                    String similarity = String.format("%.2f%%", similarityVal);
                    User activeUser = new User(name, cardId, similarity);

                    runOnUiThread(() -> {
                        labelName.setText("Name:");
                        nameTextView.setText(activeUser.getName());

                        labelCardId.setText("Card ID:");
                        cardIDTextView.setText(activeUser.getCardId());

                        labelSimilarity.setText("Similarity:");
                        similarityTextView.setText(activeUser.getSimilarity());

                        labelName.setVisibility(View.VISIBLE);
                        nameTextView.setVisibility(View.VISIBLE);
                        labelCardId.setVisibility(View.VISIBLE);
                        cardIDTextView.setVisibility(View.VISIBLE);
                        labelSimilarity.setVisibility(View.VISIBLE);
                        similarityTextView.setVisibility(View.VISIBLE);

                        alertTextView.setText("Facial recognition successful");
                        userInfoPanel.setVisibility(View.VISIBLE);

                        userInfoPanel.postDelayed(() -> userInfoPanel.setVisibility(View.GONE), 3000);

                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        intent.putExtra("activeUser", activeUser);
                        intent.putExtra("show_report", true);
                        intent.putExtra("camera_id", currentCameraId);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.d("TIMECALL", "🟤 Face not recognized.");
                    runOnUiThread(() -> alertTextView.setText("Face not recognized"));
                    handleRecognitionFail();
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToastOnMainThread("Error: " + e.getMessage());
                Log.e("FaceAPI_Error", "❗ Exception: " + e.getMessage(), e);
            } finally {
                long totalDuration = System.currentTimeMillis() - startTotalTime;
                Log.d("TIMECALL", "🕒 Total API round-trip time: " + totalDuration + " ms");

                runOnUiThread(() -> {
                    startCamera();
                    isTakingPhoto = false;
                });
            }
        }).start();
    }




    private void handleRecognitionFail() {
        runOnUiThread(() -> {
            // Hiện panel chứa thông báo
            userInfoPanel.setVisibility(View.VISIBLE);

            // Hiện alertTextView và set text báo lỗi
            alertTextView.setVisibility(View.VISIBLE);
            alertTextView.setText("Facial recognition failed");

            // Ẩn các thông tin user
            labelName.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
            labelCardId.setVisibility(View.GONE);
            cardIDTextView.setVisibility(View.GONE);
            labelSimilarity.setVisibility(View.GONE);
            similarityTextView.setVisibility(View.GONE);

            // Ẩn loading spinner nếu còn hiện
            loadingContainer.setVisibility(View.GONE);

            // Tự ẩn sau 2 giây
            alertTextView.postDelayed(() -> alertTextView.setText(""), 2000);
            userInfoPanel.postDelayed(()->{
                userInfoPanel.setVisibility(View.GONE);
            }, 2000);
        });
    }



    private void showToastOnMainThread(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
    }
//    private void startCamera(String cameraId) {
//        currentCameraId = cameraId;
//        isTakingPhoto = false;
//        faceStraightStartTime = 0;
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//
//        cameraProviderFuture.addListener(() -> {
//            try {
//                cameraProvider = cameraProviderFuture.get();
//
//                Preview preview = new Preview.Builder().build();
//                preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .build();
//                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
//                        new FaceAnalyzer(this, graphicOverlay, this));
//
//                int rotation = getWindowManager().getDefaultDisplay().getRotation();
//
//                imageCapture = new ImageCapture.Builder()
//                        .setTargetRotation(rotation)
//                        .build();
//
//                // Create CameraSelector using cameraId
//                CameraSelector cameraSelector = new CameraSelector.Builder()
//                        .addCameraFilter(cameraInfos -> {
//                            for (CameraInfo info : cameraInfos) {
//                                Camera2CameraInfo camera2Info = Camera2CameraInfo.from(info);
//                                if (camera2Info.getCameraId().equals(cameraId)) {
//                                    return Collections.singletonList(info);
//                                }
//                            }
//                            return Collections.emptyList();
//                        })
//                        .build();
//
//                cameraProvider.unbindAll();
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
//
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
    private void startCamera() {
        isTakingPhoto = false;
        faceStraightStartTime = 0;
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new FaceAnalyzer(this,graphicOverlay, this));

                // Set rotation đúng chiều của màn hình hiển thị
                int rotation = getWindowManager().getDefaultDisplay().getRotation();


                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(rotation)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        isTakingPhoto = false;
        faceStraightStartTime = 0;
    }

    @Override
    public void onFaceLookingStraight() {
        runOnUiThread(() -> {
            if (faceStraightStartTime == 0) {
                faceStraightStartTime = System.currentTimeMillis();
            } else {
                long elapsed = System.currentTimeMillis() - faceStraightStartTime;
                if (elapsed >= 0 && !isTakingPhoto) {
                    isTakingPhoto = true;
                    takePhoto();
                }
            }
        });
    }

    @Override
    public void onFaceNotLookingStraight() {
        runOnUiThread(() -> {
            faceStraightStartTime = 0; // reset nếu mặt không nhìn thẳng hoặc không đủ gần
        });
    }

    private void takePhoto() {
        if (imageCapture == null) {
            isTakingPhoto = false;
            return;
        }

        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            isTakingPhoto = false;
            return;
        }
        File file = new File(dir, filename);

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

//                         Gửi ảnh lên API, không hiển thị dialog
                        uploadImageToApi(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        isTakingPhoto = false;
                    }
                });
    }


    private void showImageDialog(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Toast.makeText(this, "Không thể tải ảnh để hiển thị", Toast.LENGTH_SHORT).show();
            isTakingPhoto = false;
            return;
        }

        // Xoay bitmap nếu cần thiết
        bitmap = rotateBitmapIfRequired(imagePath, bitmap);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ảnh đã chụp")
                .setView(imageView)
                .setPositiveButton("Đóng", (d, which) -> {
                    d.dismiss();
                })
                .setCancelable(false)
                .create();

        dialog.show();
    }
    private Bitmap rotateBitmapIfRequired(String imagePath, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int rotationDegrees = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationDegrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationDegrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationDegrees = 270;
                    break;
                default:
                    return bitmap; // không cần xoay
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationDegrees);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle(); // giải phóng bitmap cũ
            return rotatedBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            startCamera("0");
            startCamera();
        }
    }
    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn thoát ứng dụng không?")
                .setPositiveButton("Thoát", (dialog, which) -> super.onBackPressed()) // gọi super tại đây
                .setNegativeButton("Hủy", null)
                .show();
    }


}