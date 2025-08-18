package com.example.vgcamera;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements FaceAnalyzer.FaceDetectionListener {

    // ====== Constants / State ======
    private static final long STRAIGHT_FACE_DURATION = 1000; // ms
    private long IDLE_DELAY_MS;

    private PreviewView previewView;
    private FaceGraphicOverlay graphicOverlay;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    private boolean isTakingPhoto = false;
    private final OkHttpClient httpClient = new OkHttpClient();
    private long faceStraightStartTime = 0;
    private String currentCameraId = "0";

    private LinearLayout loadingContainer, userInfoPanel;
    private ProgressBar loadingSpinner;
    private TextView alertTextView, labelName, nameTextView, labelCardId, cardIDTextView, labelSimilarity, similarityTextView, appTitle;
    private ImageView appLogo;

    // ====== Lifecycle ======
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // giống APK: chặn chụp màn hình
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        if (!isInternetAvailable()) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }

        // Init views
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
        appTitle = findViewById(R.id.appTitle);
        appLogo = findViewById(R.id.appLogo);
        if (appLogo != null) appLogo.setImageResource(R.drawable.logo);

        // Hiển thị version giống APK
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String vn = pi.versionName;
            if (appTitle != null) appTitle.setText("VG-Camera V" + vn);
        } catch (PackageManager.NameNotFoundException e) {
            if (appTitle != null) appTitle.setText("VG-Camera");
        }

        listAvailableCameras();
        IDLE_DELAY_MS = getSharedPreferences("settings", MODE_PRIVATE).getInt("time_waiting", 1000);
        Log.e("IDLE_DELAY_MS", String.valueOf(IDLE_DELAY_MS));

        graphicOverlay.setCameraFacing(true); // camera trước

        // Kiểm tra/cập nhật ứng dụng giống APK (delay nhẹ để UI lên ổn định)
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAndUpdateApp, 700);

        String cameraId = getIntent().getStringExtra("camera_id");
        if (!TextUtils.isEmpty(cameraId)) currentCameraId = cameraId;

        // Quyền camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    // ====== APK-like: Update flow ======
    private void checkAndUpdateApp() {
        new Thread(() -> {
            try {
                // URL update.json giống APK
                URL url = new URL("http://gmo021.cansportsvg.com/privated/androidapp/update.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject obj = new JSONObject(sb.toString());
                int remoteCode = obj.getInt("version_code");
                String versionName = obj.getString("version_name");
                String notes = obj.getString("release_notes");
                final String apkUrl = "http://gmo021.cansportsvg.com/privated/androidapp/" + obj.getString("apk_file");

                int localCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                if (remoteCode > localCode) {
                    runOnUiThread(() -> {
                        // dừng camera trước khi show dialog cập nhật để giải phóng tài nguyên
                        stopCamera();
                        showUpdateDialog(
                                R.drawable.check_circle,
                                R.color.bluesuccess,
                                "Cập nhật mới / New Update",
                                "Có phiên bản mới " + versionName + ". Hãy cập nhật để tiếp tục.\n" +
                                        "A new version " + versionName + " is available. Please update to continue.\n\n" + notes,
                                "Cập nhật / Update",
                                () -> downloadAndInstallApk(apkUrl)
                        );
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showUpdateDialog(int iconRes, int colorRes, String title, String message, String btnText, Runnable onUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.update_dialog_common, null);
        builder.setView(dialogView);

        ImageView icon = dialogView.findViewById(R.id.dialogIcon);
        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView tvMsg = dialogView.findViewById(R.id.dialogMessage);
        Button btn = dialogView.findViewById(R.id.btnUpdate);

        icon.setImageResource(iconRes);
        icon.setColorFilter(ContextCompat.getColor(this, colorRes));
        tvTitle.setText(title);
        tvTitle.setTextColor(ContextCompat.getColor(this, colorRes));
        tvMsg.setText(message);
        btn.setText(btnText);

        AlertDialog dlg = builder.create();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dlg.setCancelable(false);
        btn.setOnClickListener(v -> {
            dlg.dismiss();
            if (onUpdate != null) onUpdate.run();
        });
        dlg.show();
    }

    private void downloadAndInstallApk(String url) {
        try {
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle("Đang tải cập nhật");
            req.setDescription("Đang tải file APK...");
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            long downloadId = dm.enqueue(req);

            BroadcastReceiver br = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) return;

                    // 1) Chỉ xử lý khi SUCCESSFUL
                    DownloadManager.Query q = new DownloadManager.Query().setFilterById(downloadId);
                    try (android.database.Cursor c = dm.query(q)) {
                        if (c == null || !c.moveToFirst()) {
                            Toast.makeText(MainActivity.this, "Download query failed", Toast.LENGTH_LONG).show();
                            return;
                        }
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status != DownloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(MainActivity.this, "Download not successful", Toast.LENGTH_LONG).show();
                            return;
                        }
                        long totalSize = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (totalSize <= 0) {
                            Toast.makeText(MainActivity.this, "Downloaded file is empty", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 2) Lấy URI gốc của DownloadManager (content://downloads/...)
                        Uri apkUri = dm.getUriForDownloadedFile(downloadId);
                        if (apkUri == null) {
                            Toast.makeText(MainActivity.this, "Cannot get downloaded file URI", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 3) Cài đặt bằng URI đó
                        installApkFromDownloadUri(apkUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Install error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        try { unregisterReceiver(this); } catch (Exception ignore) {}
                    }
                }
            };

            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(br, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(br, filter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Download error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void installApkFromDownloadUri(Uri apkUri) {
        // LƯU Ý: apkUri là content://downloads/my_downloads/... (không cần FileProvider)
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    private void installApk(Uri downloadsUri) {
        // Dùng FileProvider giống APK (tên file cố định update.apk)
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk");
        Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    // ====== Camera / Analyzer ======
    private void listAvailableCameras() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : manager.getCameraIdList()) {
                Integer facing = manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);
                String facingStr = (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                        ? "FRONT" : (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK)
                        ? "BACK" : "UNKNOWN";
                Log.d("CameraID", "ID: " + id + " - Facing: " + facingStr);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

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
                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        new FaceAnalyzer(this, graphicOverlay, this)
                );

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
            try {
                cameraProvider.unbindAll();
                Log.d("Camera", "Camera stopped.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isTakingPhoto = false;
        faceStraightStartTime = 0;
    }

    @Override
    public void onAverageLuminance(double luminance) {
        // Giữ nguyên như bản hiện tại (logic chuyển camera theo ánh sáng đã comment trong code của bạn)
    }

    @Override
    public void onFaceAreaLarge(boolean isLarge) {
        // Hook sẵn nếu cần idle chuyển màn (đã comment trong bản hiện tại)
    }

    @Override
    public void onFaceLookingStraight() {
        runOnUiThread(() -> {
            if (faceStraightStartTime == 0) {
                faceStraightStartTime = System.currentTimeMillis();
            } else {
                long elapsed = System.currentTimeMillis() - faceStraightStartTime;
                if (elapsed >= STRAIGHT_FACE_DURATION && !isTakingPhoto) {
                    isTakingPhoto = true;
                    takePhoto();
                }
            }
        });
    }

    @Override
    public void onFaceNotLookingStraight() {
        runOnUiThread(() -> faceStraightStartTime = 0);
    }

    private void takePhoto() {
        if (imageCapture == null) {
            isTakingPhoto = false;
            return;
        }
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                        uploadImageToApi(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        isTakingPhoto = false;
                    }
                });
    }

    // ====== API Upload (giữ nguyên luồng giống bản hiện tại + log/timeout/fallback như APK) ======
    private void uploadImageToApi(File file) {
        runOnUiThread(() -> {
            loadingContainer.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);
        });

        new Thread(() -> {
            long startTotalTime = System.currentTimeMillis();
            Response response = null;
            boolean isFallback = false;

            try {
                Log.d("TIMECALL", "🔁 Start uploading image: " + file.getName());

                byte[] fileBytes;
                try (FileInputStream fis = new FileInputStream(file)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[16384];
                    int r;
                    while ((r = fis.read(buf)) != -1) bos.write(buf, 0, r);
                    fileBytes = bos.toByteArray();
                }

                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // Primary request
                RequestBody primaryBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image_file", file.getName(),
                                RequestBody.create(fileBytes, MediaType.parse("image/jpeg")))
                        .build();

                Request primaryReq = new Request.Builder()
                        .url("http://10.13.32.51:5001/recognize-anti-spoofing")
                        .addHeader("X-API-Key", "vg_login_app")
                        .addHeader("X-Time", currentTime)
                        .post(primaryBody)
                        .build();

                OkHttpClient clientWithTimeout = httpClient.newBuilder()
                        .callTimeout(10, TimeUnit.SECONDS)
                        .build();

                String responseBody;
                try {
                    Log.d("TIMECALL", "🌐 Calling primary API (port 5001): http://10.13.32.51:5001/recognize-anti-spoofing");
                    long t0 = System.currentTimeMillis();
                    response = clientWithTimeout.newCall(primaryReq).execute();
                    Log.d("TIMECALL", "✅ Primary API responded in " + (System.currentTimeMillis() - t0) + " ms");
                    Log.d("TIMECALL", "✅ Primary API response code: " + response.code());
                    responseBody = response.body() != null ? response.body().string() : "";
                    Log.d("TIMECALL", "✅ Primary API response body: " + responseBody);
                } catch (Exception ex) {
                    Log.e("TIMECALL", "❌ Primary API failed: " + ex.getMessage());
                    Log.d("TIMECALL", "🌐 Falling back to secondary API (port 8001): http://10.1.16.23:8001/api/x/fr/env/face_search");
                    isFallback = true;

                    RequestBody fbBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("env_token", "8d59d8d588f84fc0a24291b8c36b6206")
                            .addFormDataPart("image_file", file.getName(),
                                    RequestBody.create(file, MediaType.parse("image/jpeg")))
                            .build();

                    Request fbReq = new Request.Builder()
                            .url("http://10.1.16.23:8001/api/x/fr/env/face_search")
                            .post(fbBody)
                            .build();

                    long t0 = System.currentTimeMillis();
                    response = httpClient.newCall(fbReq).execute();
                    Log.d("TIMECALL", "✅ Fallback API responded in " + (System.currentTimeMillis() - t0) + " ms");
                    Log.d("TIMECALL", "✅ Fallback API response code: " + response.code());
                    responseBody = response.body() != null ? response.body().string() : "";
                    Log.d("TIMECALL", "✅ Fallback API response body: " + responseBody);
                }

                String finalResponseBody = responseBody;
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    loadingContainer.setVisibility(View.GONE);
                });

                if (response == null || !response.isSuccessful()) {
                    int code = response != null ? response.code() : -1;
                    Log.e("TIMECALL", (isFallback ? "❌ Fallback" : "❌ Primary") + " API failed with code: " + code);
                    if (code == 400) {
                        handleRecognitionFail();
                    } else {
                        showToastOnMainThread("API error: " + code);
                    }
                    return;
                }

                JSONObject json = new JSONObject(finalResponseBody);

                if (json.optBoolean("is_fake", false)) {
                    Log.d("TIMECALL", "🛑 Detected spoofed face.");
                    handleRecognitionFail();
                    return;
                }

                if (json.optInt("is_recognized", 0) == 1) {
                    String name = json.optString("name");
                    String cardId = json.optString("id_string");
                    double similarityVal = json.optDouble("similarity", 0) * 100.0;

                    if (similarityVal <= 55.0) {
                        Log.d("TIMECALL", "🟡 Similarity too low: " + similarityVal);
                        handleRecognitionFail();
                        return;
                    }

                    String similarity = String.format(Locale.getDefault(), "%.2f%%", similarityVal);
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
            userInfoPanel.setVisibility(View.VISIBLE);
            alertTextView.setVisibility(View.VISIBLE);
            alertTextView.setText("Facial recognition failed");

            labelName.setVisibility(View.GONE);
            nameTextView.setVisibility(View.GONE);
            labelCardId.setVisibility(View.GONE);
            cardIDTextView.setVisibility(View.GONE);
            labelSimilarity.setVisibility(View.GONE);
            similarityTextView.setVisibility(View.GONE);

            loadingContainer.setVisibility(View.GONE);

            alertTextView.postDelayed(() -> alertTextView.setText(""), 2000);
            userInfoPanel.postDelayed(() -> userInfoPanel.setVisibility(View.GONE), 2000);
        });
    }

    private void showToastOnMainThread(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
    }

    // ====== Permissions / Utilities ======
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                android.net.NetworkInfo active = connectivityManager.getActiveNetworkInfo();
                return active != null && active.isConnected();
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn thoát ứng dụng không?")
                .setPositiveButton("Thoát", (dialog, which) -> MainActivity.super.onBackPressed())
                .setNegativeButton("Hủy", null)
                .show();
    }
}
