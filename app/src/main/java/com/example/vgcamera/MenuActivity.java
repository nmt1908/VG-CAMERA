package com.example.vgcamera;

import android.app.RecoverableSecurityException;
import android.content.ContentUris;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    Button  btnExit,btnStartCamera,btnSetting;
    private TextView txtUserName,txtClock,txtHello,txtQuestion;
    private TextView txtNotification;
    private Spinner spinnerLanguage;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_menu);

        // Khởi tạo UI
        txtNotification = findViewById(R.id.txtNotification);
        btnExit = findViewById(R.id.btnExit);
        btnStartCamera = findViewById(R.id.btnStartCamera);
        txtUserName = findViewById(R.id.txtUserName);
        txtClock = findViewById(R.id.txtClock);
        txtHello = findViewById(R.id.txtHello);
        txtQuestion = findViewById(R.id.txtQuestion);
        btnSetting = findViewById(R.id.btnSetting);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        // Khởi tạo danh sách ngôn ngữ và adapter trước khi dùng
        languageList = new ArrayList<>();
        languageList.add(new LanguageItem("VN", "vi"));
        languageList.add(new LanguageItem("EN", "en"));
        languageList.add(new LanguageItem("CN", "cn"));
        adapter = new LanguageAdapter(this, languageList);
        spinnerLanguage.setAdapter(adapter);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!languageInitialized) {
                    languageInitialized = true;
                    return;
                }

                currentLanguage = languageList.get(position).getValue();
                updateTextsByLanguage(currentLanguage);

                SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                prefs.edit().putString("app_language", currentLanguage).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        currentLanguage = prefs.getString("app_language", null);

        if (currentLanguage == null) {
            // Lấy ngôn ngữ từ API bất đồng bộ
            getInfoByEmpNo(newUser.getCardId(), showReport);
        } else {
            // Ngôn ngữ đã có sẵn, xử lý ngay
            updateTextsByLanguage(currentLanguage);

            // Set spinner
            int position = -1;
            for (int i = 0; i < languageList.size(); i++) {
                if (languageList.get(i).getValue().equals(currentLanguage)) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                spinnerLanguage.setSelection(position);
            }

            // Gọi showReportDialog ngay nếu chuỗi đã sẵn sàng
            if (showReport) {
                int imageCount = countMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                int videoCount = countMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                if (imageCount != 0 || videoCount != 0) {
                    showReportDialog(imageCount, videoCount);
                }
            }
        }

        handler.post(updateTimeRunnable);
        txtUserName.setText(newUser.getName());

        btnExit.setOnClickListener(v -> {
            prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnStartCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, CameraActivity.class);
            intent.putExtra("activeUser", newUser);
            startActivity(intent);
            finish();
        });

        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, SettingActivity.class);
            intent.putExtra("activeUser", newUser);
            startActivity(intent);
            finish();
        });

        int photoResIndex = prefs.getInt("photo_resolution_index", 2);
        int videoResIndex = prefs.getInt("video_resolution_index", 0);
        if (photoResIndex == 0 || photoResIndex == 1 ) {
            showNotification("You have selected low or medium resolution!");
        }
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
                    }
                }
            }
        });
    }
    private void handleUserInfoResponse(String body, String cardId, boolean showReport) {
        try {
            userJson = new JSONObject(body);
            currentLanguage = userJson.optString("language", "en");

            runOnUiThread(() -> {
                Log.d("LANGUAGE", "🌐 Language for " + cardId + ": " + currentLanguage);
                updateTextsByLanguage(currentLanguage);

                int position = -1;
                for (int i = 0; i < languageList.size(); i++) {
                    if (languageList.get(i).getValue().equals(currentLanguage)) {
                        position = i;
                        break;
                    }
                }
                if (position >= 0) {
                    spinnerLanguage.setSelection(position);
                }

                SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                prefs.edit().putString("app_language", currentLanguage).apply();

                if (showReport) {
                    int imageCount = countMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    int videoCount = countMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    if (imageCount != 0 || videoCount != 0) {
                        showReportDialog(imageCount, videoCount);
                    }
                }
            });

        } catch (JSONException e) {
            Log.e("API_CALL", "❌ JSON error for " + cardId + ": " + e.getMessage());
        }
    }

    private void showNotification(String message) {
        txtNotification.setText(message);
        txtNotification.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left_to_right);
        txtNotification.startAnimation(slideIn);
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
        txtMessage.setText(String.format(reportMessageTemplate, imageCount, videoCount));
        btnKeep.setText(reportKeep);
        btnDeleteAll.setText(reportDeleteAll);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // Nếu muốn người dùng phải chọn
                .create();

        btnKeep.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnDeleteAll.setOnClickListener(v -> {
            deleteAllMediaItems();
            dialog.dismiss();
        });

        dialog.show();
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
        icon.setColorFilter(ContextCompat.getColor(this, iconTintColorResId));
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(this, iconTintColorResId));
        messageView.setText(message);
        btn.setText(buttonText);

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

        Uri[] mediaUris = {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        };

        // Thu thập tất cả media URI vào hàng đợi
        for (Uri collection : mediaUris) {
            String[] projection = { MediaStore.MediaColumns._ID };
            try (Cursor cursor = getContentResolver().query(collection, projection, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                        Uri uri = ContentUris.withAppendedId(collection, id);
                        deleteQueue.add(uri);
                    }
                }
            }
        }

        // Bắt đầu xóa
        deleteNextFromQueue();
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
            int deleted = getContentResolver().delete(uri, null, null);
            if (deleted == 0) {
                // Không xóa được, tiếp tục cái tiếp theo
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
        if (requestCode == REQUEST_DELETE_PERMISSION && resultCode == RESULT_OK && pendingDeleteUri != null) {
            try {
                getContentResolver().delete(pendingDeleteUri, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pendingDeleteUri = null;
            deleteNextFromQueue(); // Tiếp tục xóa phần còn lại
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimeRunnable);
    }
    private void updateTextsByLanguage(String lang) {
        switch (lang) {
            case "vi":
                txtHello.setText("Xin chào");
                txtUserName.setText(newUser.getName());
                btnExit.setText("Thoát");
                btnStartCamera.setText("Bắt đầu Camera");
                btnSetting.setText("Cài đặt");
                txtQuestion.setText("Bạn muốn làm gì hôm nay?");
                txtNotification.setText("Bạn đã chọn độ phân giải thấp hoặc trung bình!");
                textExitTitle = "Xác nhận";
                textExitMessage = "Bạn có muốn đăng xuất và thoát ứng dụng không?";
                textExitPositive = "Thoát";
                textExitNegative = "Hủy";
                reportTitle = "VG-Camera Báo cáo";
                reportMessageTemplate = "Hiện tại còn %d ảnh và %d video còn trong máy.\nBạn muốn giữ lại ảnh và video hay xóa tất cả?";
                reportKeep = "Giữ lại";
                reportDeleteAll = "Xóa tất cả";
                deleteSuccessTitle = "Đã xóa";
                deleteSuccessMessage = "Tất cả ảnh và video đã được xóa thành công.";
                deleteButtonText = "OK";
                break;
            case "en":
                txtHello.setText("Hello");
                txtUserName.setText(newUser.getName());
                btnExit.setText("Exit");
                btnStartCamera.setText("Start Camera");
                btnSetting.setText("Settings");
                txtQuestion.setText("What would you like to do today?");
                txtNotification.setText("You have selected low or medium resolution!");
                textExitTitle = "Confirmation";
                textExitMessage = "Do you want to log out and exit the app?";
                textExitPositive = "Exit";
                textExitNegative = "Cancel";
                reportTitle = "VG-Camera Report";
                reportMessageTemplate = "There are currently %d images and %d videos remaining.\nDo you want to keep them or delete all?";
                reportKeep = "Keep";
                reportDeleteAll = "Delete All";
                deleteSuccessTitle = "Deleted";
                deleteSuccessMessage = "All images and videos have been successfully deleted.";
                deleteButtonText = "OK";
                break;
            case "cn":
                txtHello.setText("你好");
                txtUserName.setText(newUser.getName());
                btnExit.setText("退出");
                btnStartCamera.setText("开始摄像头");
                btnSetting.setText("设置");
                txtQuestion.setText("你今天想做什么？");
                txtNotification.setText("您选择了低或中等分辨率！");
                textExitTitle = "确认";
                textExitMessage = "您是否要注销并退出应用程序？";
                textExitPositive = "退出";
                textExitNegative = "取消";
                reportTitle = "VG-Camera 报告";
                reportMessageTemplate = "当前设备中还有 %d 张图片和 %d 个视频。\n您想保留它们还是全部删除？";
                reportKeep = "保留";
                reportDeleteAll = "全部删除";
                deleteSuccessTitle = "已删除";
                deleteSuccessMessage = "所有图片和视频已成功删除。";
                deleteButtonText = "确定";
                break;
            default:
                // Mặc định English
                txtHello.setText("Hello");
                txtUserName.setText(newUser.getName());
                btnExit.setText("Exit");
                btnStartCamera.setText("Start Camera");
                btnSetting.setText("Settings");
                txtQuestion.setText("What would you like to do today?");
                txtNotification.setText("You have selected low or medium resolution!");
                textExitTitle = "Confirmation";
                textExitMessage = "Do you want to log out and exit the app?";
                textExitPositive = "Exit";
                textExitNegative = "Cancel";
                deleteSuccessTitle = "Deleted";
                deleteSuccessMessage = "All images and videos have been successfully deleted.";
                deleteButtonText = "OK";
                break;
        }
    }

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            txtClock.setText(currentTime);
            handler.postDelayed(this, 1000); // cập nhật mỗi giây
        }
    };
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(textExitTitle)
                .setMessage(textExitMessage)
                .setPositiveButton(textExitPositive, (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    finish();
                })
                .setNegativeButton(textExitNegative, null)
                .show();
    }


}
