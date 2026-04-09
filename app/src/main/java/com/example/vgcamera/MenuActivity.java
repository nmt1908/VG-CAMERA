package com.example.vgcamera;

import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;
import com.example.vgcamera.ui.ComposeBridge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MenuActivity extends AppCompatActivity {
    User newUser;
    private final Handler handler = new Handler();
    String currentLanguage = "en";
    private List<LanguageItem> languageList;
    private LanguageAdapter adapter;
    JSONObject userJson;
    SharedPreferences prefs;
    private static final int REQUEST_DELETE_PERMISSION = 1002;
    private Queue<Uri> deleteQueue = new LinkedList<>();
    private Uri pendingDeleteUri;
    private String textExitTitle;
    private String textExitMessage;
    private String textExitPositive;
    private String textExitNegative;
    private String reportTitle;
    private String reportMessageTemplate;
    private String reportKeep;
    private String reportDeleteAll;
    private String
            deleteSuccessTitle,
            deleteSuccessMessage,
            deleteButtonText;
    private boolean languageInitialized = false;
    private boolean showReport;
    private boolean isLanguageDone = false;
    private boolean isReasonsDone = false;
    private boolean isErrorDialogShown = false;
    private JSONArray pendingOrdersToSelect = null;
    private ComposeView composeView;
    private boolean isLogoutDialogVisible = false;
    private boolean isLoginFlow = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        newUser = (User) getIntent().getSerializableExtra("activeUser");
        if (newUser == null) {
            Toast.makeText(this, "Please identify your face", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        showReport = getIntent().getBooleanExtra("show_report", false);
        isLoginFlow = getIntent().getBooleanExtra("is_login_flow", false);

        // ✅ Nạp ngôn ngữ NGAY LẬP TỨC: ưu tiên language từ Intent (trực tiếp từ MainActivity)
        prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        String intentLanguage = getIntent().getStringExtra("language");
        if (intentLanguage != null && !intentLanguage.isEmpty()) {
            currentLanguage = intentLanguage;
        } else {
            currentLanguage = prefs.getString("app_language", "en");
        }

        // Tiêu thụ cờ hiệu ngay sau khi đọc để tránh persist khi Activity restart
        if (isLoginFlow) {
            getIntent().removeExtra("is_login_flow");
        }

        // Cập nhật văn bản NGAY TRƯỚC khi vẽ UI
        updateTextsByLanguage(currentLanguage);

        ComposeView cv = new ComposeView(this);
        this.composeView = cv;
        setContentView(cv);

        updateComposeUI();

        String existingReasons = prefs.getString("approved_reasons_json", null);
        isReasonsDone = (existingReasons != null);

        // Nếu là đăng nhập bằng khuôn mặt: language + approved_reasons đã được lưu bởi MainActivity
        //   → không cần gọi lại API, đánh dấu xong để checkAllDone() chạy ngay
        if (isLoginFlow) {
            isLanguageDone = true;
            prefs.edit().putString("app_language", currentLanguage).apply();

            // Nhận dữ liệu đơn hàng từ MainActivity
            int countOrders = getIntent().getIntExtra("count_orders", 0);
            String ordersJson = getIntent().getStringExtra("orders_json");

            if (countOrders == 1 && ordersJson != null) {
                try {
                    JSONArray ordersArr = new JSONArray(ordersJson);
                    if (ordersArr.length() > 0) {
                        saveReasonsToPreferences(ordersArr.getJSONObject(0).getJSONArray("reasons"));
                        isReasonsDone = true;
                    }
                } catch (Exception e) {
                    Log.e("MENU", "Error auto-saving single order: " + e.getMessage());
                }
            } else if (countOrders >= 2 && ordersJson != null) {
                try {
                    pendingOrdersToSelect = new JSONArray(ordersJson);
                    isReasonsDone = true;
                } catch (Exception e) {
                    Log.e("MENU", "Error parsing multiple orders from intent: " + e.getMessage());
                }
            } else if (countOrders == 0) {
                // Trường hợp hy hữu không có đơn nhưng vẫn vào được (đã được MainActivity chặn nhưng vẫn set flag an toàn)
                isReasonsDone = true;
            }
        } else {
            // Không phải login flow (ví dụ: từ CameraActivity quay lại)
            isLanguageDone = true; // language đã được nạp từ prefs/intent ở trên
            if (existingReasons == null) {
                // Chưa có đơn → gọi API để lấy
                getInfoByEmpNo(newUser.getCardId(), showReport);
            }
        }

        if (!isReasonsDone) {
            // Lấy Lý do chụp ảnh từ API bất đồng bộ (chạy song song ngay lập tức)
            String empno = newUser.getCardId();
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Log.d("APPROVED_REASONS", "🔄 Fetching approved reasons in PARALLEL");
            fetchApprovedReasons(empno, currentDate, showReport);
        }

        // Kích hoạt hàm rào chắn gộp luồng
        checkAllDone();

    }


    private void performLogout() {
        prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Giữ lại app_language, chỉ xóa session data
        String savedLanguage = prefs.getString("app_language", "en");
        editor.clear();
        editor.putString("app_language", savedLanguage); // Khôi phục ngôn ngữ
        editor.apply();

        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isLogoutDialogVisible) {
            isLogoutDialogVisible = false;
            updateComposeUI();
        } else {
            showLogoutConfirmation();
        }
    }

    private void showLogoutConfirmation() {
        isLogoutDialogVisible = true;
        updateComposeUI();
    }

    private void changeLanguage(String newLang) {
        currentLanguage = newLang;
        prefs.edit().putString("app_language", newLang).apply();
        updateTextsByLanguage(newLang);
        updateComposeUI();
    }

    private void updateComposeUI() {
        if (composeView == null) return;
        ComposeBridge.setMenuContent(
            composeView,
            newUser,
            () -> { // onStartCamera
                Intent intent = new Intent(MenuActivity.this, CameraActivity.class);
                intent.putExtra("activeUser", newUser);
                startActivity(intent);
                // Removed finish() to allow Back to Menu
            },
            () -> { // onSettings
                Intent intent = new Intent(MenuActivity.this, SettingActivity.class);
                intent.putExtra("activeUser", newUser);
                startActivity(intent);
                // Removed finish() to allow Back to Menu
            },
            () -> { // onGallery
                Intent intent = new Intent(MenuActivity.this, AlbumActivity.class);
                startActivity(intent);
            },
            this::performLogout,
            isLogoutDialogVisible,
            () -> { // onLogoutDismiss
                isLogoutDialogVisible = false;
                updateComposeUI();
            },
            this::showLogoutConfirmation,
            currentLanguage,
            lang -> {
                changeLanguage(lang);
                return kotlin.Unit.INSTANCE;
            }
        );
    }

    public void getInfoByEmpNo(String cardId, boolean showReport) {
        fetchInfo(cardId, showReport, true); // lần đầu, cho phép thử lại
    }

    private void fetchInfo(String cardId, boolean showReport, boolean allowRetryWithTrimmedZero) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://gmo021.cansportsvg.com/api/camera-api/getInfoByEmpNo";

        RequestBody formBody = new FormBody.Builder()
                .add("empno", cardId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Log.d("API_CALL", "🌐 Sending request with cardId = " + cardId);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("API_CALL", "❌ Network failure for cardId = " + cardId + ": " + e.getMessage());
                showAccountNotFoundDialog();

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("API_CALL", "📡 Got HTTP " + response.code() + " for cardId = " + cardId);

                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d("API_CALL", "📥 Response body for cardId " + cardId + ": " + body);
                    handleUserInfoResponse(body, cardId, showReport);
                } else {
                    Log.w("API_CALL", "⚠️ Failed response for cardId = " + cardId);

                    // Retry nếu được phép và cardId bắt đầu bằng "0" và dài hơn 5 chữ số
                    if (allowRetryWithTrimmedZero && cardId.length() > 5 && cardId.startsWith("0")) {
                        String fallbackCardId = cardId.replaceFirst("^0+", ""); // bỏ tất cả số 0 đầu
                        Log.d("API_CALL", "🔁 Retrying with trimmed cardId = " + fallbackCardId);
                        fetchInfo(fallbackCardId, showReport, false);
                    } else {
                        Log.e("API_CALL", "❌ No fallback or fallback already attempted.");
                        showAccountNotFoundDialog();

                    }
                }
            }
        });
    }
    private void handleUserInfoResponse(String body, String cardId, boolean showReport) {
        try {
            userJson = new JSONObject(body);
            String apiLanguage = userJson.optString("language", "en");

            runOnUiThread(() -> {
                Log.d("LANGUAGE", "🌐 API Language for " + cardId + ": " + apiLanguage);
                
                // Quy tắc: 
                // 1. Nếu là luồng Đăng nhập (vừa quét mặt xong) -> Ưu tiên API tuyệt đối
                // 2. Nếu không phải luồng đăng nhập (ví dụ từ Camera quay lại) -> Chỉ lấy API nếu user chưa có lựa chọn nào lưu trong prefs
                
                SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                String savedLang = prefs.getString("app_language", null);
                
                if (isLoginFlow || savedLang == null) {
                    currentLanguage = apiLanguage;
                    prefs.edit().putString("app_language", currentLanguage).apply();
                    updateTextsByLanguage(currentLanguage);
                    updateComposeUI();
                } else {
                    currentLanguage = savedLang;
                    // Không cần lưu lại prefs vì ta ưu tiên cái cũ đang dùng
                    updateTextsByLanguage(currentLanguage);
                }

                isLanguageDone = true;
                checkAllDone();
            });

        } catch (JSONException e) {
            Log.e("API_CALL", "❌ JSON error for " + cardId + ": " + e.getMessage());
        }
    }

    private void checkAllDone() {
        if (isLanguageDone && isReasonsDone) {
            if (pendingOrdersToSelect != null) {
                JSONArray orders = pendingOrdersToSelect;
                pendingOrdersToSelect = null; // show only once
                showOrderSelectionDialog(orders, () -> continueWithLeftoverMedia(showReport));
            } else {
                continueWithLeftoverMedia(showReport);
            }
        }
    }

    private void continueWithLeftoverMedia(boolean showReport) {
        if (showReport) {
            int imageCount = countMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            int videoCount = countMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            if (imageCount != 0 || videoCount != 0) {
                showReportDialog(imageCount, videoCount);
            }
        }
    }

    private void showNotification(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
    private int countMedia(Uri uri) {
        int count = 0;
        String[] projection = {MediaStore.MediaColumns._ID};

        try (Cursor cursor = getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null)) {
            if (cursor != null) {
                count = cursor.getCount();
            }
        }

        return count;
    }

    private void showReportDialog(int imageCount, int videoCount) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_report, null);

        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);
        TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
        Button btnKeep = dialogView.findViewById(R.id.btnKeep);
        Button btnDeleteAll = dialogView.findViewById(R.id.btnDeleteAll);

        txtTitle.setText(reportTitle);
        
        String formattedMsg = String.format(reportMessageTemplate, 
            "<font color='#FF4D4D'><b>" + imageCount + "</b></font>", 
            "<font color='#FF4D4D'><b>" + videoCount + "</b></font>");
        txtMessage.setText(android.text.Html.fromHtml(formattedMsg.replace("\n", "<br>"), android.text.Html.FROM_HTML_MODE_LEGACY));
        
        btnKeep.setText(reportKeep);
        btnDeleteAll.setText(reportDeleteAll);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // Nếu muốn người dùng phải chọn
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnKeep.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnDeleteAll.setOnClickListener(v -> {
            deleteAllMediaItems();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void showAccountNotFoundDialog() {
        if (isErrorDialogShown) return;
        isErrorDialogShown = true;
        runOnUiThread(() -> showCustomDialog(
                R.drawable.ic_x_circle,
                R.color.red,
                "Thông báo",
                "Tài khoản của bạn chưa tồn tại. Vui lòng đăng ký hoặc liên hệ PS #172",
                "OK",
                () -> {
                    // optional: đá về màn login
                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        ));
    }

    public void showCustomDialog(int iconResId, int iconTintColorResId,
                                 String title, String message,
                                 String buttonText, Runnable onClose) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        btn.setOnClickListener(v -> {
            dialog.dismiss();
            if (onClose != null) onClose.run();
        });

        dialog.show();
    }

    private void deleteAllMediaItems() {
        deleteQueue.clear();
        List<Uri> batchUris = new ArrayList<>();

        Uri[] mediaUris = {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        };

        for (Uri collection : mediaUris) {
            String[] projection = { MediaStore.MediaColumns._ID };
            try (Cursor cursor = getContentResolver().query(collection, projection, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                        Uri uri = ContentUris.withAppendedId(collection, id);
                        deleteQueue.add(uri);
                        batchUris.add(uri);
                    }
                }
            }
        }

        if (batchUris.isEmpty()) {
            showCustomDialog(R.drawable.check_circle, R.color.bluesuccess, deleteSuccessTitle, deleteSuccessMessage, deleteButtonText, () -> {});
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && !android.os.Environment.isExternalStorageManager()) {
            try {
                android.app.PendingIntent pi = MediaStore.createDeleteRequest(getContentResolver(), batchUris);
                startIntentSenderForResult(pi.getIntentSender(), REQUEST_DELETE_PERMISSION, null, 0, 0, 0, null);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                deleteNextFromQueue(); // Fallback to Android 10 loop
            }
        } else {
            deleteNextFromQueue(); // Android 10 individual deletes OR MANAGE_EXTERNAL_STORAGE is granted
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deleteNextFromQueue() {
        if (deleteQueue.isEmpty()) {
            showCustomDialog(
                    R.drawable.check_circle,
                    R.color.bluesuccess,
                    deleteSuccessTitle,
                    deleteSuccessMessage,
                    deleteButtonText,
                    () -> {
                    }
            );
            return;
        }

        Uri uri = deleteQueue.poll();
        try {
            String filePath = getRealPathFromURI(this, uri);
            boolean deletedFile = false;
            if (filePath != null) {
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
                    deletedFile = file.delete();
                }
            }
            
            if (deletedFile) {
                android.media.MediaScannerConnection.scanFile(this, new String[]{filePath}, null, null);
            } else {
                getContentResolver().delete(uri, null, null);
            }
            deleteNextFromQueue();
        } catch (RecoverableSecurityException e) {
            pendingDeleteUri = uri;
            IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
            try {
                startIntentSenderForResult(intentSender, REQUEST_DELETE_PERMISSION, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deleteNextFromQueue();
        }
    }

//    public void getInfoByEmpNo(String cardId, boolean showReport) {
//        OkHttpClient client = new OkHttpClient();
//        Log.e("LANGUAGE", "🟢 getInfoByEmpNo() called with cardId = " + cardId);
//
//        String url = "http://gmo021.cansportsvg.com/api/camera-api/getInfoByEmpNo";
//        RequestBody formBody = new FormBody.Builder()
//                .add("empno", cardId)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build();
//
//        Log.d("API_CALL", "🌐 Sending request to: " + url + " with empno=" + cardId);
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Log.e("API_CALL", "❌ onFailure: Request failed for empno = " + cardId + ". Error: " + e.getMessage(), e);
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                Log.d("API_CALL", "✅ onResponse: HTTP " + response.code() + " for empno = " + cardId);
//
//                if (response.isSuccessful()) {
//                    String body = response.body().string();
//                    Log.d("API_CALL", "📥 Response body: " + body);
//
//                    try {
//                        userJson = new JSONObject(body);
//                        currentLanguage = userJson.optString("language", "en");
//
//                        Log.d("LANGUAGE", "🔍 Extracted language from JSON: " + currentLanguage);
//
//                        runOnUiThread(() -> {
//                            Log.d("LANGUAGE", "🧠 Setting UI language to: " + currentLanguage);
//                            updateTextsByLanguage(currentLanguage);
//
//                            int position = -1;
//                            for (int i = 0; i < languageList.size(); i++) {
//                                if (languageList.get(i).getValue().equals(currentLanguage)) {
//                                    position = i;
//                                    break;
//                                }
//                            }
//
//                            if (position >= 0) {
//                                Log.d("LANGUAGE", "🎯 Found language in spinner at position: " + position);
//                                spinnerLanguage.setSelection(position);
//                            } else {
//                                Log.w("LANGUAGE", "⚠️ Language not found in spinner list: " + currentLanguage);
//                            }
//
//                            SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
//                            prefs.edit().putString("app_language", currentLanguage).apply();
//                            Log.d("LANGUAGE", "💾 Saved language to SharedPreferences: " + currentLanguage);
//
//                            if (showReport) {
//                                int imageCount = countMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                                int videoCount = countMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//                                Log.d("LANGUAGE", "🖼 Media found — Images: " + imageCount + ", Videos: " + videoCount);
//
//                                if (imageCount != 0 || videoCount != 0) {
//                                    showReportDialog(imageCount, videoCount);
//                                }
//                            }
//                        });
//
//                    } catch (JSONException e) {
//                        Log.e("API_CALL", "❌ Failed to parse JSON response for empno = " + cardId + ". Error: " + e.getMessage(), e);
//                    }
//                } else {
//                    Log.e("API_CALL", "❌ API response failed for empno = " + cardId + ". HTTP code: " + response.code());
//                }
//            }
//        });
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DELETE_PERMISSION && resultCode == RESULT_OK) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && pendingDeleteUri == null) {
                // Batch delete already performed by OS in Android 11+
                deleteQueue.clear();
                showCustomDialog(R.drawable.check_circle, R.color.bluesuccess, deleteSuccessTitle, deleteSuccessMessage, deleteButtonText, () -> {});
            } else if (pendingDeleteUri != null) {
                // Android 10 fallback
                try {
                    getContentResolver().delete(pendingDeleteUri, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pendingDeleteUri = null;
                deleteNextFromQueue();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void updateTextsByLanguage(String lang) {
        switch (lang) {
            case "vi":
                textExitTitle = "Xác nhận";
                textExitMessage = "Bạn có muốn đăng xuất và thoát ứng dụng không?";
                textExitPositive = "Thoát";
                textExitNegative = "Hủy";
                reportTitle = "Báo cáo VG-Camera";
                reportMessageTemplate = "Hiện tại còn %s ảnh và %s video còn trong máy.\nBạn muốn giữ lại ảnh và video hay xóa tất cả?";
                reportKeep = "Giữ lại";
                reportDeleteAll = "Xóa tất cả";
                deleteSuccessTitle = "Đã xóa";
                deleteSuccessMessage = "Tất cả ảnh và video đã được xóa thành công.";
                deleteButtonText = "OK";
                break;
            case "en":
                textExitTitle = "Confirmation";
                textExitMessage = "Do you want to log out and exit the app?";
                textExitPositive = "Exit";
                textExitNegative = "Cancel";
                reportTitle = "VG-Camera Report";
                reportMessageTemplate = "There are currently %s images and %s videos remaining.\nDo you want to keep them or delete all?";
                reportKeep = "Keep";
                reportDeleteAll = "Delete All";
                deleteSuccessTitle = "Deleted";
                deleteSuccessMessage = "All images and videos have been successfully deleted.";
                deleteButtonText = "OK";
                break;
            case "cn":
                textExitTitle = "确认";
                textExitMessage = "您是否要注销并退出应用程序？";
                textExitPositive = "退出";
                textExitNegative = "取消";
                reportTitle = "VG-Camera 报告";
                reportMessageTemplate = "当前设备中还有 %s 张图片和 %s 个视频。\n您想保留它们还是全部删除？";
                reportKeep = "保留";
                reportDeleteAll = "全部删除";
                deleteSuccessTitle = "已删除";
                deleteSuccessMessage = "所有图片和视频已成功删除。";
                deleteButtonText = "确定";
                break;
            default:
                textExitTitle = "Confirmation";
                textExitMessage = "Do you want to log out and exit the app?";
                textExitPositive = "Exit";
                textExitNegative = "Cancel";
                reportTitle = "VG-Camera Report";
                reportMessageTemplate = "There are currently %s images and %s videos remaining.\nDo you want to keep them or delete all?";
                reportKeep = "Keep";
                reportDeleteAll = "Delete All";
                deleteSuccessTitle = "Deleted";
                deleteSuccessMessage = "All images and videos have been successfully deleted.";
                deleteButtonText = "OK";
                break;
        }
    }


    // ========== ✅ NEW: Approved Reasons API Integration ==========

    /**
     * Fetch approved reasons from API
     */
    private void fetchApprovedReasons(String empno, String date, boolean showReport) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://gmo021.cansportsvg.com/api/camera-api/getApprovedReasonsByEmpnoAndDate";

        RequestBody formBody = new FormBody.Builder()
                .add("empno", empno)
                .add("date", date)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Log.d("APPROVED_REASONS", "🌐 Fetching approved reasons for empno=" + empno + ", date=" + date);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("APPROVED_REASONS", "❌ Network failure: " + e.getMessage());
                // Don't block user, just log the error
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d("APPROVED_REASONS", "📥 Response: " + body);
                    try {
                        JSONObject json = new JSONObject(body);
                        runOnUiThread(() -> handleApprovedReasonsResponse(json, showReport));
                    } catch (JSONException e) {
                        Log.e("APPROVED_REASONS", "❌ JSON parse error: " + e.getMessage());
                    }
                } else {
                    Log.w("APPROVED_REASONS", "⚠️ API failed with code: " + response.code());
                }
            }
        });
    }

    /**
     * Handle API response for approved reasons
     */
    private void handleApprovedReasonsResponse(JSONObject response, boolean showReport) {
        try {
            boolean ok = response.optBoolean("ok", false);
            int countOrders = response.optInt("count_orders", 0);

            Log.d("APPROVED_REASONS", "📊 ok=" + ok + ", count_orders=" + countOrders);

            if (!ok || countOrders == 0) {
                // Show error dialog and logout
                showNoOrderDialog();
                return;
            }

            JSONArray orders = response.getJSONArray("orders");

            if (countOrders == 1) {
                // Auto-save reasons from the single order
                JSONObject order = orders.getJSONObject(0);
                JSONArray reasons = order.getJSONArray("reasons");
                saveReasonsToPreferences(reasons);
                Log.d("APPROVED_REASONS", "✅ Auto-saved reasons from single order");
                
                isReasonsDone = true;
                checkAllDone();
            } else if (countOrders >= 2) {
                // Găm lại đợi Language tải xong rồi mới hiển thị Dialog bằng tiếng tương ứng
                pendingOrdersToSelect = orders;
                isReasonsDone = true;
                checkAllDone();
            }

        } catch (JSONException e) {
            Log.e("APPROVED_REASONS", "❌ Error handling response: " + e.getMessage());
        }
    }

    /**
     * Show dialog when no orders found
     */
    private void showNoOrderDialog() {
        if (isErrorDialogShown) return;
        isErrorDialogShown = true;
        String title, message, buttonText;

        String lang = (currentLanguage != null) ? currentLanguage : "en";
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
                    // Logout on close
                    SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        );
    }

    /**
     * Show dialog to select between 2 orders
     */
    private void showOrderSelectionDialog(JSONArray orders, Runnable onSelected) {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_selection, null);

            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
            Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

            // Set title and message based on language
            String title, message, confirmText;
            switch (currentLanguage) {
                case "vi":
                    title = "Chọn lý do chụp ảnh";
                    message = "Do bạn có nhiều đơn trong ngày, nên hãy chọn lý do chụp ảnh nhé.";
                    confirmText = "Xác nhận";
                    break;

                case "cn":
                    title = "選擇拍照原因";
                    message = "由於您當天有多筆申請，請選擇拍照的原因。";
                    confirmText = "確認";
                    break;

                default: // en
                    title = "Select Photo Reason";
                    message = "Since you have multiple orders on the same day, please select the reason for taking the photo.";
                    confirmText = "Confirm";
                    break;
            }


            dialogTitle.setText(title);
            dialogMessage.setText(message);
            btnConfirm.setText(confirmText);

            // Get the 2 order item views from XML
            View orderItem1 = dialogView.findViewById(R.id.orderItem1);
            View orderItem2 = dialogView.findViewById(R.id.orderItem2);

            // Populate Order 1
            JSONObject order1 = orders.getJSONObject(0);
            JSONArray reasons1 = order1.getJSONArray("reasons");
            populateOrderItem(orderItem1, order1);

            // Populate Order 2
            JSONObject order2 = orders.getJSONObject(1);
            JSONArray reasons2 = order2.getJSONArray("reasons");
            populateOrderItem(orderItem2, order2);

            // Track selected item (default to first)
            final int[] selectedIndex = {0};
            updateCardSelection(orderItem1, orderItem2, 0);

            // Set click listeners
            orderItem1.setOnClickListener(v -> {
                selectedIndex[0] = 0;
                updateCardSelection(orderItem1, orderItem2, 0);
            });

            orderItem2.setOnClickListener(v -> {
                selectedIndex[0] = 1;
                updateCardSelection(orderItem1, orderItem2, 1);
            });

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            // ✅ Set rounded corner background
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            btnConfirm.setOnClickListener(v -> {
                try {
                    JSONObject order1_obj = orders.getJSONObject(0);
                    JSONObject order2_obj = orders.getJSONObject(1);
                    JSONArray selectedReasons = selectedIndex[0] == 0 ? order1_obj.getJSONArray("reasons") : order2_obj.getJSONArray("reasons");
                    saveReasonsToPreferences(selectedReasons);
                } catch (JSONException e) {}
                dialog.dismiss();
                Log.d("APPROVED_REASONS", "✅ User selected order " + (selectedIndex[0] + 1) + ", reasons saved");
                if (onSelected != null) onSelected.run();
            });

            dialog.show();

        } catch (JSONException e) {
            Log.e("APPROVED_REASONS", "❌ Error showing order selection dialog: " + e.getMessage());
        }
    }

    /**
     * Update card selection visual state
     */
    private void updateCardSelection(View card1, View card2, int selectedIndex) {
        ImageView check1 = card1.findViewById(R.id.checkIcon);
        ImageView check2 = card2.findViewById(R.id.checkIcon);

        if (selectedIndex == 0) {
            check1.setVisibility(View.VISIBLE);
            check2.setVisibility(View.GONE);
            card1.setAlpha(1.0f);
            card2.setAlpha(0.6f);
        } else {
            check1.setVisibility(View.GONE);
            check2.setVisibility(View.VISIBLE);
            card1.setAlpha(0.6f);
            card2.setAlpha(1.0f);
        }
    }

    /**
     * Helper method to populate an order item view with data
     */
    private void populateOrderItem(View itemView, JSONObject order) throws JSONException {
        TextView tvOrderTitle = itemView.findViewById(R.id.tvOrderTitle);
        TextView tvReasonsLabel = itemView.findViewById(R.id.tvReasonsLabel);
        LinearLayout layoutReasons = itemView.findViewById(R.id.layoutReasons);

        int orderId = order.getInt("order_id");
        JSONArray reasons = order.getJSONArray("reasons");

        // Set order title based on language
        String orderTitle;
        if (currentLanguage.equals("vi")) {
            orderTitle = "Đơn PAB #" + orderId;
        } else if (currentLanguage.equals("cn")) {
            orderTitle = "PAB訂單 #" + orderId;
        } else {
            orderTitle = "Order #" + orderId;
        }
        tvOrderTitle.setText(orderTitle);

        // Set reasons label
        String reasonsLabel = currentLanguage.equals("vi") ? "Lý do:" :
                              currentLanguage.equals("cn") ? "原因:" : "Reasons:";
        tvReasonsLabel.setText(reasonsLabel);

        // Clear existing reasons (if any) and add new ones
        layoutReasons.removeAllViews();
        for (int j = 0; j < reasons.length(); j++) {
            JSONObject reason = reasons.getJSONObject(j);
            TextView tvReason = new TextView(this);
            tvReason.setText("• " + reason.getString(currentLanguage));
            tvReason.setTextSize(14);
            tvReason.setTextColor(0xFF616161);
            tvReason.setPadding(0, 6, 0, 6);
            layoutReasons.addView(tvReason);
        }
    }

    /**
     * Save reasons array to SharedPreferences
     */
    private void saveReasonsToPreferences(JSONArray reasons) {
        SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        prefs.edit().putString("approved_reasons_json", reasons.toString()).apply();
        Log.d("APPROVED_REASONS", "💾 Saved to SharedPreferences: " + reasons.toString());
    }


}
