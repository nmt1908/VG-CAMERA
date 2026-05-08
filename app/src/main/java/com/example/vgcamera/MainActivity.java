package com.example.vgcamera;

import android.Manifest;
import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.view.LayoutInflater;
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

import org.json.JSONArray;
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

import androidx.compose.ui.platform.ComposeView;
import com.google.mlkit.common.sdkinternal.MlKitContext;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.example.vgcamera.ui.ComposeBridge;
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

    // ====== Permissions ======
    private static final int PERMISSION_REQUEST_ALL = 101;
    private static String[] REQUIRED_PERMISSIONS;

    static {
        java.util.List<String> perms = new java.util.ArrayList<>();
        perms.add(Manifest.permission.CAMERA);
        perms.add(Manifest.permission.RECORD_AUDIO);
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            perms.add(Manifest.permission.READ_MEDIA_IMAGES);
            perms.add(Manifest.permission.READ_MEDIA_VIDEO);
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        } else {
            // Android 10-12
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        REQUIRED_PERMISSIONS = perms.toArray(new String[0]);
    }

    private PreviewView previewView;
    private FaceGraphicOverlay graphicOverlay;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
    private ProcessCameraProvider cameraProvider;
    private com.google.mlkit.vision.facemesh.FaceMeshDetector staticDetector;

    private boolean isTakingPhoto = false;
    private final OkHttpClient httpClient = new OkHttpClient();
    private long faceStraightStartTime = 0;
    private String currentCameraId = "0";

    // UI States
    private static final int STATE_IDLE = 0;
    private static final int STATE_DETECTING = 1;
    private static final int STATE_PROCESSING = 2;
    private static final int STATE_COUNTDOWN = 3;
    private static final int STATE_SUCCESS = 4;
    private static final int STATE_ERROR = 5;
    private int currentUIState = STATE_IDLE;
    
    private long countdownStartTime = 0;

    private ComposeView composeUserInfo;
    private User activeUserForCompose = null;
    private boolean isUserInfoVisible = false;
    private boolean isExitDialogVisible = false;
    private boolean isFaceDetected = false;
    private String currentLanguage = "en";
    private String exitTitle = "Confirmation", exitMsg = "Do you want to exit?", exitConfirm = "Exit", exitCancel = "Cancel";

    // Face Metadata for Compose
    private android.graphics.Rect lastFaceRect = null;
    private float[] lastMeshPoints = null;
    private int lastImageWidth = 0;
    private int lastImageHeight = 0;
    private int lastImageRotation = 0;
    private Bitmap frozenBitmap = null;

    private ProgressBar loadingSpinner;
    private volatile boolean isLoginDialogShowing = false; // đang mở dialog login?
    private boolean navigatingNext = false;
    private boolean isRequireUpdate = false;
    private TextView appTitle;
    private ImageView appLogo;

    // ====== Lifecycle ======
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        
        // Khởi tạo ML Kit nếu ContentProvider chưa kịp chạy
        try {
            MlKitContext.initializeIfNeeded(this);
        } catch (Exception e) {
            Log.e("MLKIT", "Manual init failed", e);
        }
        
        // giống APK: chặn chụp màn hình
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        staticDetector = com.google.mlkit.vision.facemesh.FaceMeshDetection.getClient(
            new com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions.Builder()
                .build()
        );

        SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        currentLanguage = prefs.getString("app_language", "en");
        updateTextsByLanguage(currentLanguage);

        if (!isInternetAvailable()) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }

        // Init views
        composeUserInfo = findViewById(R.id.composeUserInfo);
        updateComposeUI();

        previewView = findViewById(R.id.previewView);
        graphicOverlay = findViewById(R.id.graphicOverlay);
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

        if (appLogo != null) {
            appLogo.setOnClickListener(v -> showLoginDialog());
        }

        // Hỏi tất cả runtime permissions ngay khi khởi động
        autoGrantPermissionsIfDeviceOwner();
        checkAndRequestAllPermissions();
    }

    private void autoGrantPermissionsIfDeviceOwner() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminName = new ComponentName(this, DeviceAdminReceiver.class);

        if (dpm.isDeviceOwnerApp(getPackageName())) {
            Log.d("DeviceOwner", "App is Device Owner. Setting global policy and granting permissions...");
            
            try {
                // Ép chính sách toàn cục: Tự động cấp tất cả các quyền runtime
                dpm.setPermissionPolicy(adminName, DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT);
            } catch (Exception e) {
                Log.e("DeviceOwner", "❌ Failed to set Permission Policy: " + e.getMessage());
            }

            // Danh sách các quyền cần cấp tự động
            java.util.List<String> permissions = new java.util.ArrayList<>();
            Collections.addAll(permissions, REQUIRED_PERMISSIONS);
            
            // Bổ sung các quyền media cho Android 13+ nếu chưa có trong REQUIRED_PERMISSIONS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            }

            for (String permission : permissions) {
                try {
                    dpm.setPermissionGrantState(adminName, getPackageName(), permission, 
                        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                    Log.d("DeviceOwner", "✅ Auto-granted: " + permission);
                } catch (Exception e) {
                    Log.e("DeviceOwner", "❌ Failed to grant " + permission + ": " + e.getMessage());
                }
            }

            // ÉP BẬT GPS VÀ KHÓA NÚT GẠT
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Android 9.0+ : Ép bật GPS
                    dpm.setLocationEnabled(adminName, true);
                    Log.d("DeviceOwner", "✅ Location Services forced ON");
                }
                
                // Khóa không cho user vào tắt GPS trong Cài đặt
                dpm.addUserRestriction(adminName, android.os.UserManager.DISALLOW_CONFIG_LOCATION);
                Log.d("DeviceOwner", "✅ Location Configuration DISABLED for user");
                
            } catch (Exception e) {
                Log.e("DeviceOwner", "❌ Failed to force location: " + e.getMessage());
            }
        } else {
            Log.d("DeviceOwner", "⚠️ App is NOT Device Owner.");
        }
    }

    private void updateComposeUI() {
        if (composeUserInfo == null) return;
        ComposeBridge.setMainOverlayContent(
                composeUserInfo,
                activeUserForCompose,
                isUserInfoVisible,
                currentUIState,
                frozenBitmap,
                lastFaceRect,
                lastMeshPoints,
                lastImageWidth,
                lastImageHeight,
                lastImageRotation,
                isExitDialogVisible,
                exitTitle,
                exitMsg,
                exitConfirm,
                exitCancel,
                () -> {
                    // Confirm Exit
                    isExitDialogVisible = false;
                    MainActivity.super.onBackPressed();
                },
                () -> {
                    // Dismiss Exit
                    isExitDialogVisible = false;
                    updateComposeUI();
                }
        );
    }

    // Tiện ích cập nhật state từ Java
    private void showModernUserInfo(User user) {
        runOnUiThread(() -> {
            activeUserForCompose = user;
            isUserInfoVisible = true;
            // Giữ state PROCESSING cho đến khi hiệu ứng kết thúc hoặc chuyển màn
            updateComposeUI();
        });
    }

    private void hideModernUserInfo() {
        runOnUiThread(() -> {
            isUserInfoVisible = false;
            currentUIState = STATE_IDLE;
            frozenBitmap = null;
            updateComposeUI();
        });
    }

    // ====== Permission Helpers ======
    private void checkAndRequestAllPermissions() {
        java.util.List<String> missingPerms = new java.util.ArrayList<>();
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                missingPerms.add(perm);
            }
        }

        if (missingPerms.isEmpty()) {
            // Tất cả quyền thường đã có → kiểm tra MANAGE_EXTERNAL_STORAGE
            checkStorageAndStartCamera();
        } else {
            // Hỏi các quyền còn thiếu
            ActivityCompat.requestPermissions(this,
                missingPerms.toArray(new String[0]),
                PERMISSION_REQUEST_ALL);
        }
    }

    private void checkStorageAndStartCamera() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean isDO = dpm.isDeviceOwnerApp(getPackageName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            if (isDO) {
                // Nếu là Device Owner, ta có thể thử bỏ qua hoặc thông báo nhưng thường 
                // hệ thống vẫn bắt user bật tay 1 lần cho MANAGE_EXTERNAL_STORAGE.
                // Tuy nhiên ta sẽ log lại để biết.
                Log.d("DeviceOwner", "MANAGE_EXTERNAL_STORAGE is missing but we are Device Owner.");
            }
            
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            startCamera();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 9999);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d("PERMISSIONS", "✅ All Files Access granted!");
                } else {
                    Toast.makeText(this, "Vui lòng cấp quyền quản lý file để ứng dụng dọn rác bộ nhớ tự động!", Toast.LENGTH_LONG).show();
                }
                startCamera();
            }
        } else if (requestCode == 9999) {
            // Quay lại từ trang Settings → kiểm tra quyền lại
            checkAndRequestAllPermissions();
        }
    }
    private void showLoginDialog() {
        if (isLoginDialogShowing) return;   // tránh mở trùng
        isLoginDialogShowing = true;

        stopCamera(); // <<< DỪNG CAMERA TRƯỚC KHI MỞ DIALOG

        View view = getLayoutInflater().inflate(R.layout.dialog_login, null, false);

        final android.widget.EditText etUsername = view.findViewById(R.id.etUsername);
        final android.widget.EditText etPassword = view.findViewById(R.id.etPassword);
        final Button btnLogin = view.findViewById(R.id.btnLogin);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Nếu user đóng/cancel dialog mà chưa chuyển màn → khởi động lại camera
        dlg.setOnCancelListener(d -> {
            isLoginDialogShowing = false;
            if (!navigatingNext) startCamera();
        });
        dlg.setOnDismissListener(d -> {
            isLoginDialogShowing = false;
            if (!navigatingNext) startCamera();
        });

        Runnable doSubmit = () -> {
            String user = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            new Thread(() -> {
                try {
                    okhttp3.RequestBody form = new okhttp3.MultipartBody.Builder()
                            .setType(okhttp3.MultipartBody.FORM)
                            .addFormDataPart("username", user)
                            .addFormDataPart("password", pass)
                            .build();

                    okhttp3.Request req = new okhttp3.Request.Builder()
                            .url("http://10.1.16.89/api/camera-api/doLogin")
                            .post(form)
                            .build();

                    OkHttpClient client = httpClient.newBuilder()
                            .callTimeout(10, TimeUnit.SECONDS)
                            .build();

                    try (okhttp3.Response resp = client.newCall(req).execute()) {
                        if (!resp.isSuccessful()) {
                            runOnUiThread(() -> {
                                btnLogin.setEnabled(true);
                                Toast.makeText(MainActivity.this, "Login lỗi: " + resp.code(), Toast.LENGTH_LONG).show();
                            });
                            return;
                        }

                        String body = resp.body() != null ? resp.body().string() : "{}";
                        JSONObject j = new JSONObject(body);

                        String name  = j.optString("name", user);
                        String empno = j.optString("empno", j.optString("username", user)); // cardID = empno
                        User activeUser = new User(name, empno, "100%");

                        runOnUiThread(() -> {
                            try { dlg.dismiss(); } catch (Exception ignore) {}
                            goToNextScreen(activeUser); // sẽ không restart camera vì navigatingNext=true
                        });
                    }
                } catch (Exception ex) {
                    Log.e("LoginDialog", "Login error: " + ex.getMessage(), ex);
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(MainActivity.this, "Không gọi được login: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        };

        btnLogin.setOnClickListener(v -> doSubmit.run());
        etPassword.setOnEditorActionListener((tv, actionId, event) -> { doSubmit.run(); return true; });

        dlg.show();
    }



    // ====== APK-like: Update flow ======
    private void checkAndUpdateApp() {
        new Thread(() -> {
            try {
                // URL update.json giống APK
                URL url = new URL("http://10.1.16.89/privated/androidapp/update.json");
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
                final String apkUrl = "http://10.1.16.89/privated/androidapp/" + obj.getString("apk_file");

                int localCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                if (remoteCode > localCode) {
                    isRequireUpdate = true;
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

    private void downloadAndInstallApk(final String urlString) {
        showToastOnMainThread("Đang tải bản cập nhật...");
        new Thread(() -> {
            try {
                okhttp3.Request request = new okhttp3.Request.Builder().url(urlString).build();
                OkHttpClient client = httpClient.newBuilder()
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        showToastOnMainThread("Lỗi tải xuống (HTTP " + response.code() + ")");
                        return;
                    }

                    File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk");
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }

                    try (java.io.InputStream in = response.body().byteStream();
                         java.io.FileOutputStream out = new java.io.FileOutputStream(apkFile)) {
                         
                        byte[] buffer = new byte[16384];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }

                    showToastOnMainThread("Tải hoàn tất, đang cài đặt...");

                    Uri apkUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        apkUri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", apkFile);
                    } else {
                        apkUri = Uri.fromFile(apkFile);
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToastOnMainThread("Tải thất bại: " + e.getMessage());
            }
        }).start();
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

                imageAnalysis = new ImageAnalysis.Builder()
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
    public void onFaceMeshDetected(com.google.mlkit.vision.facemesh.FaceMesh mesh, int width, int height, int rotation, boolean isLookingStraight) {
        if (currentUIState == STATE_PROCESSING || currentUIState == STATE_SUCCESS) return;

        runOnUiThread(() -> {
            lastFaceRect = mesh.getBoundingBox();
            java.util.List<com.google.mlkit.vision.facemesh.FaceMeshPoint> points = mesh.getAllPoints();
            lastMeshPoints = new float[points.size() * 2];
            for (int i = 0; i < points.size(); i++) {
                lastMeshPoints[i * 2] = points.get(i).getPosition().getX();
                lastMeshPoints[i * 2 + 1] = points.get(i).getPosition().getY();
            }

            lastImageWidth = width;
            lastImageHeight = height;
            lastImageRotation = rotation;
            
            // Xử lý logic đếm ngược nếu mặt đang ở target và nhìn thẳng
            if (currentUIState == STATE_COUNTDOWN) {
                if (!isLookingStraight) {
                    currentUIState = STATE_DETECTING;
                    countdownStartTime = 0;
                } else {
                    long elapsed = System.currentTimeMillis() - countdownStartTime;
                    if (elapsed >= 2000) {
                        lockAndProcess();
                    }
                }
            } else if (currentUIState != STATE_PROCESSING && currentUIState != STATE_SUCCESS) {
                currentUIState = STATE_DETECTING;
            }
            // Chỉ update Compose nếu không đang xử lý ảnh tĩnh
            if (currentUIState != STATE_PROCESSING && currentUIState != STATE_SUCCESS) {
                updateComposeUI();
            }
        });
    }

    @Override
    public void onNoFaceDetected() {
        if (currentUIState == STATE_PROCESSING || currentUIState == STATE_SUCCESS) return;
        runOnUiThread(() -> {
            if (currentUIState != STATE_IDLE) {
                currentUIState = STATE_IDLE;
                countdownStartTime = 0;
                lastFaceRect = null;
                lastMeshPoints = null;
                updateComposeUI();
            }
        });
    }

    @Override
    public void onFaceInTarget(com.google.mlkit.vision.facemesh.FaceMesh mesh, boolean isLookingStraight) {
        if (isTakingPhoto || currentUIState == STATE_PROCESSING || currentUIState == STATE_SUCCESS) return;
        runOnUiThread(() -> {
            if (isLookingStraight && currentUIState != STATE_COUNTDOWN) {
                currentUIState = STATE_COUNTDOWN;
                countdownStartTime = System.currentTimeMillis();
                updateComposeUI();
            }
        });
    }

    @Override
    public void onFaceNotInTarget() {
        // Có thể reset UI hoặc giữ state DETECTING nhưng không cho chụp
    }

    @Override
    public void onAverageLuminance(double luminance) {
        // Hook dự phòng cho ánh sáng (nếu cần xử lý độ sáng môi trường)
    }

    private void lockAndProcess() {
        if (currentUIState == STATE_PROCESSING) return;
        
        runOnUiThread(() -> {
            // 1. Đóng băng hình ảnh
            frozenBitmap = previewView.getBitmap();
            if (frozenBitmap == null) return;

            currentUIState = STATE_PROCESSING;
            countdownStartTime = 0;
            updateComposeUI();

            // Rung nhẹ khi khóa mặt (Haptic Feedback)
            try {
                android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(60, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(60);
                    }
                }
            } catch (Exception ignored) {}

            // 2. Chạy AI trên ảnh tĩnh để Mesh khớp 100%
            com.google.mlkit.vision.common.InputImage inputImage = 
                com.google.mlkit.vision.common.InputImage.fromBitmap(frozenBitmap, 0);

            staticDetector.process(inputImage)
                .addOnSuccessListener(meshes -> {
                    if (!meshes.isEmpty()) {
                        com.google.mlkit.vision.facemesh.FaceMesh mesh = meshes.get(0);
                        java.util.List<com.google.mlkit.vision.facemesh.FaceMeshPoint> points = mesh.getAllPoints();
                        lastMeshPoints = new float[points.size() * 2];
                        for (int i = 0; i < points.size(); i++) {
                            lastMeshPoints[i * 2] = points.get(i).getPosition().getX();
                            lastMeshPoints[i * 2 + 1] = points.get(i).getPosition().getY();
                        }
                        
                        // Ảnh tĩnh từ getBitmap đã được rotate đúng chiều
                        lastFaceRect = mesh.getBoundingBox();
                        lastImageWidth = frozenBitmap.getWidth();
                        lastImageHeight = frozenBitmap.getHeight();
                        lastImageRotation = 0; 
                        
                        updateComposeUI();
                    }
                    // 3. Gửi thẳng tấm ảnh vừa đóng băng lên API
                    uploadBitmapToApi(frozenBitmap);
                })
                .addOnFailureListener(e -> {
                    uploadBitmapToApi(frozenBitmap);
                });
        });
    }

    private void uploadBitmapToApi(Bitmap bitmap) {
        if (bitmap == null) return;
        new Thread(() -> {
            try {
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File file = new File(dir, "frozen_capture_" + System.currentTimeMillis() + ".jpg");
                java.io.FileOutputStream out = new java.io.FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                uploadImageFile(file);
            } catch (Exception e) {
                Log.e("Upload", "Failed to save frozen bitmap", e);
            }
        }).start();
    }

    private void uploadImageFile(File file) {
        final long processingStartTime = System.currentTimeMillis();
        final long minProcessingTime = 1600; 

        new Thread(() -> {
            Response response = null;
            String responseBody = "";
            boolean isFallback = false;

            try {
                byte[] fileBytes;
                try (FileInputStream fis = new FileInputStream(file)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[16384];
                    int r;
                    while ((r = fis.read(buf)) != -1) bos.write(buf, 0, r);
                    fileBytes = bos.toByteArray();
                }

                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // 1. API chính (Port 5001)
                RequestBody bodyPrimary = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image_file", file.getName(),
                                RequestBody.create(fileBytes, MediaType.parse("image/jpeg")))
                        .build();

                Request reqPrimary = new Request.Builder()
                        .url("http://10.13.34.166:5001/recognize-anti-spoofing")
                        .addHeader("X-API-Key", "vg_login_app")
                        .addHeader("X-Time", currentTime)
                        .post(bodyPrimary)
                        .build();

                OkHttpClient clientWithTimeout = httpClient.newBuilder()
                        .callTimeout(10, TimeUnit.SECONDS)
                        .build();

                try {
                    Log.d("TIMECALL", "🌐 Calling primary API (port 5001): http://10.13.34.166:5001/recognize-anti-spoofing");
                    response = clientWithTimeout.newCall(reqPrimary).execute();
                    responseBody = response.body() != null ? response.body().string() : "";
                    Log.d("TIMECALL", "✅ Primary API response: " + responseBody);
                } catch (Exception ex) {
                    Log.e("TIMECALL", "❌ Primary API failed: " + ex.getMessage());
                    Log.d("TIMECALL", "🌐 Falling back to secondary API (port 8001): http://10.1.16.23:8001/api/x/fr/env/face_search");
                    isFallback = true;

                    RequestBody bodyFallback = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("env_token", "8d59d8d588f84fc0a24291b8c36b6206")
                            .addFormDataPart("image_file", file.getName(),
                                    RequestBody.create(fileBytes, MediaType.parse("image/jpeg")))
                            .build();

                    Request reqFallback = new Request.Builder()
                            .url("http://10.1.16.23:8001/api/x/fr/env/face_search")
                            .post(bodyFallback)
                            .build();

                    response = clientWithTimeout.newCall(reqFallback).execute();
                    responseBody = response.body() != null ? response.body().string() : "";
                    Log.d("TIMECALL", "✅ Fallback API response: " + responseBody);
                }
                
                if (response != null && response.isSuccessful()) {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.optBoolean("is_fake", false)) {
                        handleRecognitionFail();
                        return;
                    }
                    if (json.optInt("is_recognized", 0) == 1) {
                        String name = json.optString("name");
                        String cardId = json.optString("id_string");
                        double similarityVal = json.optDouble("similarity", 0) * 100.0;
                        if (similarityVal > 55.0) {
                            User activeUser = new User(name, cardId, String.format(Locale.getDefault(), "%.2f%%", similarityVal));
                            long elapsed = System.currentTimeMillis() - processingStartTime;
                            long remain = Math.max(0, minProcessingTime - elapsed);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                currentUIState = STATE_SUCCESS; // Dừng animation
                                activeUserForCompose = activeUser;
                                isUserInfoVisible = true;
                                updateComposeUI();
                                new Handler(Looper.getMainLooper()).postDelayed(() -> goToNextScreen(activeUser), 1500);
                            }, remain);
                            return;
                        }
                    }
                }
                handleRecognitionFail();
            } catch (Exception e) {
                Log.e("API", "Upload error: " + e.getMessage());
                handleRecognitionFail();
            } finally {
                runOnUiThread(() -> {
                    if (!navigatingNext) startCamera();
                    isTakingPhoto = false;
                });
            }
        }).start();
    }
    private void goToNextScreen(User activeUser) {
        if (isRequireUpdate) return;
        navigatingNext = true;

        runOnUiThread(() -> {
            if (cameraProvider != null) cameraProvider.unbindAll();
            showModernUserInfo(activeUser);
        });

        verifyAndProceed(activeUser);
    }

    private void verifyAndProceed(User activeUser) {
        String cardId = activeUser.getCardId();
        String mergedUrl = "http://gmo021.cansportsvg.com/api/camera-api/getInfoAndOrdersByEmpNo";
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        OkHttpClient client = httpClient.newBuilder()
                .callTimeout(10, TimeUnit.SECONDS)
                .build();

        new Thread(() -> {
            try {
                okhttp3.RequestBody body = new okhttp3.FormBody.Builder()
                        .add("empno", cardId)
                        .add("date", today)
                        .build();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(mergedUrl)
                        .post(body)
                        .build();

                String apiLanguage = "en";
                int countOrders = 0;
                String reasonsJson = "[]";

                try (okhttp3.Response resp = client.newCall(req).execute()) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        JSONObject root = new JSONObject(resp.body().string());

                        JSONObject userObj = root.optJSONObject("user");
                        if (userObj != null) {
                            apiLanguage = userObj.optString("language", "en");
                            SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                            prefs.edit().putString("app_language", apiLanguage).apply();
                            currentLanguage = apiLanguage;
                        }

                        countOrders = root.optInt("count_orders", 0);
                        JSONArray arr = root.optJSONArray("orders");
                        if (arr == null) arr = new JSONArray();
                        reasonsJson = arr.toString();
                    }
                }

                final String finalLanguage = apiLanguage;
                final int finalCountOrders = countOrders;
                final String finalReasonsJson = reasonsJson;

                runOnUiThread(() -> {
                    if (finalCountOrders == 0) {
                        // Không có đơn → hiện dialog theo ngôn ngữ, bấm OK → văng ra camera
                        navigatingNext = false;
                        showNoOrderDialog(finalLanguage);
                    } else {
                        // Có đơn → vào MenuActivity kèm language
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                            intent.putExtra("activeUser", activeUser);
                            intent.putExtra("show_report", true);
                            intent.putExtra("camera_id", currentCameraId);
                            intent.putExtra("is_login_flow", true);
                            intent.putExtra("language", finalLanguage);
                            intent.putExtra("count_orders", finalCountOrders);
                            intent.putExtra("orders_json", finalReasonsJson); // Truyền danh sách đơn hàng

                            androidx.core.app.ActivityOptionsCompat options =
                                    androidx.core.app.ActivityOptionsCompat.makeCustomAnimation(
                                            MainActivity.this, android.R.anim.fade_in, android.R.anim.fade_out);
                            startActivity(intent, options.toBundle());
                            finish();
                        }, 800);
                    }
                });

            } catch (Exception e) {
                Log.e("VERIFY", "verifyAndProceed error: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    navigatingNext = false;
                    Toast.makeText(MainActivity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        startCamera();
                        isTakingPhoto = false;
                    }, 500);
                });
            }
        }).start();
    }

    private void showNoOrderDialog(String lang) {
        String title, message, buttonText;

        switch (lang) {
            case "vi":
                title = "Thông báo";
                message = "Bạn chưa có đơn đăng ký chụp ảnh hoặc đơn chưa được duyệt. Vui lòng liên hệ TIT#172";
                buttonText = "OK";
                break;
            case "cn":
                title = "通知";
                message = "您沒有已批准的照片註冊訂單。請聯繫 TIT#172";
                buttonText = "確定";
                break;
            default: // en
                title = "Notification";
                message = "You don't have any approved photo registration order. Please contact TIT#172";
                buttonText = "OK";
                break;
        }

        showCustomDialog(
                R.drawable.ic_x_circle,
                R.color.red,
                title,
                message,
                buttonText,
                () -> {
                    // Reset UI hoàn toàn để có thể đăng nhập lại
                    runOnUiThread(() -> {
                        hideModernUserInfo();
                        currentUIState = STATE_IDLE;
                        navigatingNext = false;
                        isTakingPhoto = false;
                        startCamera();
                        updateComposeUI();
                    });
                }
        );
    }

    public void showCustomDialog(int iconResId, int iconTintColorResId,
                                 String title, String message,
                                 String buttonText, Runnable onClose) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_common, null);
        builder.setView(dialogView);

        ImageView icon = dialogView.findViewById(R.id.dialogIcon);
        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageView = dialogView.findViewById(R.id.dialogMessage);
        Button btn = dialogView.findViewById(R.id.dialogButton);

        icon.setImageResource(iconResId);

        int colorToApply = ContextCompat.getColor(this, iconTintColorResId);
        if (iconTintColorResId == R.color.bluesuccess) {
            colorToApply = android.graphics.Color.parseColor("#4CAF50"); // Xanh lá
        }

        icon.setColorFilter(colorToApply);
        titleView.setText(title);
        titleView.setTextColor(colorToApply);
        messageView.setText(message);
        btn.setText(buttonText);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorToApply));

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false);

        btn.setOnClickListener(v -> {
            dialog.dismiss();
            if (onClose != null) onClose.run();
        });

        dialog.show();
    }


    private void handleRecognitionFail() {
        runOnUiThread(() -> {
            currentUIState = STATE_ERROR; // Chuyển sang trạng thái lỗi
            User failUser = new User("Recognition Failed", "------", "0%");
            activeUserForCompose = failUser;
            isUserInfoVisible = true;
            frozenBitmap = previewView.getBitmap(); // Đóng băng ảnh lỗi để user thấy
            updateComposeUI();

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                hideModernUserInfo();
                currentUIState = STATE_IDLE;
                isTakingPhoto = false;
                updateComposeUI();
            }, 2500);
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

        if (requestCode == PERMISSION_REQUEST_ALL) {
            java.util.List<String> denied = new java.util.ArrayList<>();
            java.util.List<String> permanentlyDenied = new java.util.ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        permanentlyDenied.add(permissions[i]);
                    } else {
                        denied.add(permissions[i]);
                    }
                }
            }

            if (!permanentlyDenied.isEmpty()) {
                // Người dùng đã chọn "Không hỏi lại" → phải vào Settings
                showPermissionSettingsDialog();
            } else if (!denied.isEmpty()) {
                // Người dùng từ chối nhưng chưa "Không hỏi lại" → giải thích và hỏi lại
                showPermissionRationaleDialog();
            } else {
                // Tất cả quyền đã được cấp
                checkStorageAndStartCamera();
            }
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Cần quyền truy cập / Permission Required")
            .setMessage(
                "Ứng dụng cần các quyền sau để hoạt động:\n"
                + "• Camera: nhận diện khuôn mặt\n"
                + "• Microphone: ghi âm video\n"
                + "• Bộ nhớ: lưu ảnh/video\n"
                + "• Vị trí: xác định mạng WiFi\n\n"
                + "App needs these permissions to work:\n"
                + "• Camera: face recognition\n"
                + "• Microphone: video recording\n"
                + "• Storage: save photos/videos\n"
                + "• Location: identify WiFi network"
            )
            .setCancelable(false)
            .setPositiveButton("Cấp quyền / Grant", (d, which) -> checkAndRequestAllPermissions())
            .setNegativeButton("Thoát / Exit", (d, which) -> finish())
            .show();
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Quyền bị từ chối / Permission Denied")
            .setMessage(
                "Một số quyền bị từ chối vĩnh viễn. Vui lòng vào Cài đặt để cấp thủ công.\n\n"
                + "Some permissions were permanently denied. Please go to Settings to grant them manually."
            )
            .setCancelable(false)
            .setPositiveButton("Mở Cài đặt / Open Settings", (d, which) -> openAppSettings())
            .setNegativeButton("Thoát / Exit", (d, which) -> finish())
            .show();
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
    protected void onDestroy() {
        super.onDestroy();
        if (staticDetector != null) {
            staticDetector.close();
        }
    }

    @Override
    public void onBackPressed() {
        if (isExitDialogVisible) {
            isExitDialogVisible = false;
            updateComposeUI();
        } else {
            isExitDialogVisible = true;
            updateComposeUI();
        }
    }
    private void updateTextsByLanguage(String lang) {
        switch (lang) {
            case "vi":
                exitTitle = "Xác nhận Thoát";
                exitMsg = "Bạn có chắc chắn muốn thoát khỏi ứng dụng không?";
                exitConfirm = "Thoát";
                exitCancel = "Hủy bỏ";
                break;
            case "cn":
                exitTitle = "确认退出";
                exitMsg = "您确定要退出应用程序吗？";
                exitConfirm = "退出";
                exitCancel = "取消";
                break;
            case "en":
            default:
                exitTitle = "Confirm Exit";
                exitMsg = "Are you sure you want to exit the application?";
                exitConfirm = "Exit";
                exitCancel = "Cancel";
                break;
        }
    }
}
