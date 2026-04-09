package com.example.vgcamera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.compose.ui.platform.ComposeView;
import com.example.vgcamera.ui.ComposeBridge;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.otaliastudios.zoom.ZoomLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlbumActivity extends AppCompatActivity {
    private MenuItem selectAllItem;
    private ProgressDialog progressDialog;
    private Uri pendingDeleteUri = null;
    public static final int REQUEST_DELETE_PERMISSION = 1001;

    private static final int REQUEST_PERMISSION = 1001;
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private List<MediaItem> mediaItems = new ArrayList<>();
    private TextView titleText;
    private boolean isAllSelected = false;
    private LinearLayout actionBarLayout;

    private ImageView deleteButton;

    private LinearLayout uploadButton;
    private TextView uploadText;
    private User newUser;
    JSONObject userJson;
    String currentLanguage = "en";
    private SharedPreferences prefs;
    private String uploadDialogTitle;
    private String uploadDialogMessage;
    private String yesText;
    private String noText;
    private String loadingText;
    private String permissionDeniedText;
    private String defaultTitle = "Album";
    private String uploadSuccess = "", uploadMessage = "", uploadFailed = "", uploadFailedMessage = "", uploadButtonText = "", uploadGalleryText = "";
    private String deleteTitleTrans = "", deleteMsgTrans = "", deleteConfirmTrans = "";
    private String cancelTextTrans = "Hủy bỏ";
    private String uploadTitleTrans = "", uploadMsgTrans = "", uploadConfirmTrans = "";
    private android.widget.Button selectAllText;
    private String selectAllTextString = "Chọn tất cả";
    private String deselectAllTextString = "Bỏ chọn";

    private List<String> allowedSSIDs = new ArrayList<>();
    final float[] downY = new float[1];
    final long[] downTime = new long[1];
    String currentSSID;
    
    // ❌ OLD: Purpose dialog logic (now handled in MenuActivity)
    // private final List<Purpose> purposeList = new ArrayList<>();
    // private boolean[] purposeChecked;
    // private final List<Purpose> selectedPurposes = new ArrayList<>();

    private ComposeView composeOverlay;
    private boolean isUploadDialogVisible = false;
    private boolean isDeleteDialogVisible = false;
    private boolean isMessageDialogVisible = false;
    private String messageTitle = "";
    private String messageText = "";
    private Runnable onMessageConfirmRunnable = null;
    private List<MediaItem> pendingUploadMedia = null;
    private String authRequiredTitle, authRequiredMessage;
    private String wifiInvalidTitle, wifiInvalidMessage;
    private String infoMissingTitle, infoMissingMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_album);
        prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        currentLanguage = prefs.getString("app_language", "en"); // "en" mặc định

        recyclerView = findViewById(R.id.recyclerView);
        titleText = findViewById(R.id.titleText);
        uploadText=findViewById(R.id.uploadText);
        actionBarLayout = findViewById(R.id.actionBarLayout);
        deleteButton = findViewById(R.id.deleteButton);
        uploadButton = findViewById(R.id.uploadButton);
        selectAllText = findViewById(R.id.selectAllText);
        newUser = (User) getIntent().getSerializableExtra("activeUser");
        composeOverlay = findViewById(R.id.composeOverlay);

        // ✅ Cập nhật ngôn ngữ TRƯỚC khi vẽ UI để tránh NullPointerException
        prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        currentLanguage = prefs.getString("app_language", "en");
        updateTextsByLanguage(currentLanguage);

        updateAlbumComposeUI();

        if (newUser == null) {
            showModernMessage(authRequiredTitle, authRequiredMessage, () -> {
                Intent intent = new Intent(AlbumActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            return;
        }

        // ✅ NEW: Validate approved reasons exist
        String approvedReasons = prefs.getString("approved_reasons_json", null);
        if (approvedReasons == null) {
            Log.w("APPROVED_REASONS", "⚠️ No approved reasons found - redirecting to MenuActivity");
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish(); // Go back to MenuActivity
            return;
        }

        getSSIDAllowed();
        // ❌ OLD: fetchPurposes(); // Now handled in MenuActivity

        getInfoByEmpNo(newUser.getCardId());
        deleteButton.setOnClickListener(v -> {
            if (adapter.getSelectedCount() > 0) {
                adapter.deleteSelectedItems();
            } else {
                Toast.makeText(this, "Chưa chọn ảnh/video", Toast.LENGTH_SHORT).show();
            }
        });

        uploadButton.setOnClickListener(v -> {
            String allowedSSIDsStr = TextUtils.join(", ", allowedSSIDs);

            Log.d("CURRENT SSID", currentSSID);

            if (currentSSID != null && currentSSID.startsWith("\"") && currentSSID.endsWith("\"")) {
                currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
            }

            Log.e("SSID_LIST", currentSSID);

            if (!allowedSSIDs.contains(currentSSID)) {
                showModernMessage(wifiInvalidTitle, wifiInvalidMessage + ": " + allowedSSIDsStr, null);
                return;
            }

            if (userJson == null ||
                    !userJson.has("username") || !userJson.has("empno") || !userJson.has("name")) {
                showModernMessage(infoMissingTitle, infoMissingMessage, null);
                return;
            }

            // ✅ lấy media đã chọn
            List<MediaItem> selectedMedia = new ArrayList<>();
            for (MediaItem item : mediaItems) {
                if (item.isSelected) selectedMedia.add(item);
            }

            if (selectedMedia.isEmpty()) {
                Toast.makeText(this, "Chưa chọn ảnh/video", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ NEW: Load reasons from SharedPreferences and show confirm dialog
            pendingUploadMedia = selectedMedia;
            isUploadDialogVisible = true;
            updateAlbumComposeUI();
        });







        ImageView backArrow = findViewById(R.id.backArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                if (adapter.isSelectionMode()) {
                    adapter.deselectAll();
                    updateTitle();
                    updateSelectAllIcon();
                } else {
                    finish();
                }
            });
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY[0] = event.getY();
                    downTime[0] = System.currentTimeMillis();
                    break;

                case MotionEvent.ACTION_UP:
                    float deltaY = Math.abs(event.getY() - downY[0]);
                    long elapsed = System.currentTimeMillis() - downTime[0];

                    // Nếu không cuộn (tap nhẹ, ngắn) thì mới hủy chọn
                    if (deltaY < 20 && elapsed < 200) {
                        if (adapter.isSelectionMode()) {
                            Log.d("AlbumActivity", "RecyclerView tapped (not scrolled). Deselecting all.");
                            adapter.deselectAll();
                            updateTitle();
                            updateSelectAllIcon();
                            return true;
                        }
                    }
                    break;
            }
            return false;
        });
        adapter = new AlbumAdapter(mediaItems, this::updateTitle, this);

        recyclerView.setAdapter(adapter);
        selectAllText.setOnClickListener(v -> {
            isAllSelected = !isAllSelected;
            if (isAllSelected) {
                adapter.selectAll();
            } else {
                adapter.deselectAll();
            }
            updateSelectAllIcon();
            updateTitle();
        });

        updateTextsByLanguage(currentLanguage);
        checkPermissionsAndLoad();
    }

    private void updateAlbumComposeUI() {
        if (composeOverlay == null) return;
        ComposeBridge.setAlbumOverlayContent(
                composeOverlay,
                isUploadDialogVisible,
                isDeleteDialogVisible,
                isMessageDialogVisible,
                messageTitle,
                messageText,
                uploadTitleTrans,
                uploadMsgTrans,
                uploadConfirmTrans,
                cancelTextTrans,
                deleteTitleTrans,
                deleteMsgTrans,
                deleteConfirmTrans,
                cancelTextTrans,
                () -> {
                    // Upload Confirm
                    isUploadDialogVisible = false;
                    executeUpload(pendingUploadMedia);
                    updateAlbumComposeUI();
                },
                () -> {
                    // Delete Confirm
                    isDeleteDialogVisible = false;
                    adapter.deleteSelectedItems();
                    updateTitle();
                    updateAlbumComposeUI();
                },
                () -> {
                    // Message Confirm / Dismiss
                    isMessageDialogVisible = false;
                    if (onMessageConfirmRunnable != null) {
                        onMessageConfirmRunnable.run();
                    }
                    updateAlbumComposeUI();
                },
                () -> {
                    // General Dismiss
                    isUploadDialogVisible = false;
                    isDeleteDialogVisible = false;
                    isMessageDialogVisible = false;
                    updateAlbumComposeUI();
                }
        );
    }

    private void showModernMessage(String title, String message, Runnable onConfirm) {
        runOnUiThread(() -> {
            messageTitle = title;
            messageText = message;
            onMessageConfirmRunnable = onConfirm;
            isMessageDialogVisible = true;
            updateAlbumComposeUI();
        });
    }

    private void executeUpload(List<MediaItem> selectedMedia) {
        if (selectedMedia == null) return;
        List<Purpose> approvedReasons = loadApprovedReasonsFromPreferences();
        uploadButton.setEnabled(false);

        new MediaUploader(
                AlbumActivity.this,
                userJson,
                approvedReasons,
                currentLanguage
        ).uploadSelectedMedia(selectedMedia);
        logSelectedPurposesOnly(selectedMedia, approvedReasons);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            uploadButton.setEnabled(true);
        }, 2000);
    }

    // ✅ NEW: Load reasons from SharedPreferences and upload
    // (Modern Upload Dialog is handled via updateAlbumComposeUI state)
    
    /**
     * Load approved reasons from SharedPreferences (saved in MenuActivity)
     */
    private List<Purpose> loadApprovedReasonsFromPreferences() {
        List<Purpose> reasons = new ArrayList<>();
        try {
            String reasonsJson = prefs.getString("approved_reasons_json", null);
            if (reasonsJson != null) {
                Log.d("APPROVED_REASONS", "📦 Raw JSON from SharedPreferences: " + reasonsJson);
                JSONArray jsonArray = new JSONArray(reasonsJson);
                reasons = Purpose.listFromJsonArray(jsonArray);
                Log.d("APPROVED_REASONS", "✅ Successfully parsed " + reasons.size() + " reasons");
            } else {
                Log.e("APPROVED_REASONS", "❌ CRITICAL: No approved reasons found in SharedPreferences! Upload might miss metadata.");
            }
        } catch (JSONException e) {
            Log.e("APPROVED_REASONS", "❌ JSON structure error! Data might be 'orders' instead of 'reasons': " + e.getMessage());
        }
        return reasons;
    }
    private void logSelectedPurposesOnly(List<MediaItem> selectedMedia, List<Purpose> purposes) {
        try {
            JSONArray purposesArr = new JSONArray();
            for (Purpose p : purposes) {
                purposesArr.put(p.toJson());
            }

            JSONObject preview = new JSONObject();
            preview.put("empno", userJson != null ? userJson.optString("empno") : "null");
            preview.put("name",  userJson != null ? userJson.optString("name") : "null");
            preview.put("selected_media_count", selectedMedia != null ? selectedMedia.size() : 0);
            preview.put("purpose", purposesArr);

            Log.e("UPLOAD_PREVIEW", "===== PURPOSE PREVIEW (FROM SHAREDPREFERENCES) =====");
            Log.e("UPLOAD_PREVIEW", preview.toString(2));
            Log.e("UPLOAD_PREVIEW", "====================================================");
        } catch (Exception e) {
            Log.e("UPLOAD_PREVIEW", "logSelectedPurposesOnly error", e);
        }
    }

    // ❌ Removed OLD purpose log/fetch methods



    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            getInfoByEmpNo(newUser.getCardId());
            if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d("NETWORK", "📶 Wi-Fi connected, fetching SSID list...");
                getSSIDAllowed(); // tự động fetch lại khi kết nối wifi
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                currentSSID = wifiInfo.getSSID();
                Log.e("Current SSID",currentSSID);
            } else {
                Log.d("NETWORK", "❌ Mất kết nối hoặc không phải Wi-Fi");
            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (newUser != null && newUser.getCardId() != null) {
            getInfoByEmpNo(newUser.getCardId());
            Log.d("LIFECYCLE", "onResume: Fetching user info again");
        }
    }

    // Hàm xử lý upload
    private void getSSIDAllowed() {
        String url = "http://gmo021.cansportsvg.com/api/camera-api/getSSIDAllowed";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Gán mặc định "gmo032" nếu lỗi mạng
                allowedSSIDs.clear();
                allowedSSIDs.add("gmo032");

                new Handler(getMainLooper()).post(() -> {
                    Log.w("SSID_LIST", "API call failed. Using default SSID: gmo032");
                    Toast.makeText(AlbumActivity.this, "Failed to fetch SSIDs. Using default: gmo032", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Gán mặc định "gmo032" nếu server trả lỗi
                    allowedSSIDs.clear();
                    allowedSSIDs.add("gmo032");

                    new Handler(getMainLooper()).post(() -> {
                        Log.w("SSID_LIST", "Server error. Using default SSID: gmo032");
                        Toast.makeText(AlbumActivity.this, "Server error. Using default: gmo032", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String jsonString = response.body().string();

                try {
                    JSONArray ssidArray = new JSONArray(jsonString);
                    allowedSSIDs.clear();
                    for (int i = 0; i < ssidArray.length(); i++) {
                        JSONObject obj = ssidArray.getJSONObject(i);
                        String ssid = obj.getString("ssid");
                        allowedSSIDs.add(ssid);
                    }

                    new Handler(getMainLooper()).post(() ->
                            Log.d("SSID_LIST", "Fetched SSIDs: " + allowedSSIDs)
                    );

                } catch (JSONException e) {
                    // Gán mặc định "gmo032" nếu lỗi phân tích JSON
                    allowedSSIDs.clear();
                    allowedSSIDs.add("gmo032");

                    new Handler(getMainLooper()).post(() -> {
                        Log.w("SSID_LIST", "JSON parse error. Using default SSID: gmo032");
                        Toast.makeText(AlbumActivity.this, "Parse error. Using default: gmo032", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }




    private void updateTextsByLanguage(String lang) {
        switch (lang) {
            case "vi":
                defaultTitle = "Bộ sưu tập";
                titleText.setText(defaultTitle);
                selectAllTextString = "Chọn tất cả";
                if (adapter != null) updateTitle();
                uploadText.setText(uploadGalleryText);

                uploadDialogTitle = "Xác nhận";
                uploadDialogMessage = "Bạn có chắc muốn tải lên các mục đã chọn không?";
                yesText = "Có";
                noText = "Không";
                loadingText = "Đang tải...";
                permissionDeniedText = "Không thể xóa mục đã chọn vì chưa được phép";
                uploadSuccess = "Tải ảnh/video Thành Công!";
                uploadMessage = "Tất cả hình ảnh và video đã được tải lên.";
                uploadFailed = "Upload thất bại!";
                uploadFailedMessage = "Vui lòng thử lại sau.";
                uploadButtonText = "Đóng";
                uploadGalleryText = "TẢI LÊN THƯ VIỆN";
                authRequiredTitle = "Yêu cầu đăng nhập";
                authRequiredMessage = "Vui lòng đăng nhập lại để truy cập Album.";
                wifiInvalidTitle = "Wifi không hợp lệ";
                wifiInvalidMessage = "Vui lòng kết nối đúng Wifi để tải lên";
                infoMissingTitle = "Thiếu thông tin";
                infoMissingMessage = "Thông tin tài khoản chưa được cập nhật. Vui lòng liên hệ PS#172.";
                uploadTitleTrans = "Xác nhận Tải lên";
                uploadMsgTrans = "Bạn có chắc chắn muốn tải lên các hình ảnh/video đã chọn lên hệ thống không?";
                uploadConfirmTrans = "Tải lên";
                deleteTitleTrans = "Xác nhận Xóa";
                deleteMsgTrans = "Các tệp tin bị xóa sẽ không thể khôi phục. Bạn có chắc chắn muốn tiếp tục không?";
                deleteConfirmTrans = "Xóa bỏ";
                cancelTextTrans = "Hủy bỏ";
                selectAllTextString = "Chọn tất cả";
                deselectAllTextString = "Bỏ chọn";
                uploadText.setText("Tải lên");
                break;

            case "cn":
                defaultTitle = "相册"; // nghĩa là "album ảnh" trong tiếng Trung
                titleText.setText(defaultTitle);
                if (adapter != null) updateTitle();
                selectAllTextString = "全选";
                deselectAllTextString = "取消全选";
                uploadText.setText("上传");

                uploadDialogTitle = "确认";
                uploadDialogMessage = "您确定要上传所选的媒体吗？";
                yesText = "是";
                noText = "否";
                loadingText = "加载中...";
                permissionDeniedText = "未获得权限，无法删除所选项";
                uploadSuccess = "上传成功！";
                uploadMessage = "所有图片都已上传。";
                uploadFailed = "上传失败！";
                uploadFailedMessage = "请稍后再试。";
                uploadButtonText = "关闭";
                uploadGalleryText = "上传图库";
                authRequiredTitle = "需要登录";
                authRequiredMessage = "请重新登录以访问相册。";
                wifiInvalidTitle = "Wifi无效";
                wifiInvalidMessage = "请连接到正确的 Wifi 以进行上传";
                infoMissingTitle = "信息缺失";
                infoMissingMessage = "账号信息未更新。请联系 PS#172。";
                uploadTitleTrans = "确认上传";
                uploadMsgTrans = "您确定要将选定的媒体上传到系统吗？";
                uploadConfirmTrans = "上传";
                deleteTitleTrans = "确认删除";
                deleteMsgTrans = "删除的文件无法恢复。您要继续吗？";
                deleteConfirmTrans = "删除";
                cancelTextTrans = "取消";
                break;

            case "en":
            default:
                defaultTitle = "Album";
                titleText.setText(defaultTitle);
                if (adapter != null) updateTitle();
                uploadText.setText("Upload");
                selectAllTextString = "Select All";
                deselectAllTextString = "Deselect All";

                uploadDialogTitle = "Confirmation";
                uploadDialogMessage = "Are you sure you want to upload the selected media?";
                yesText = "Yes";
                noText = "No";
                loadingText = "Loading...";
                permissionDeniedText = "Could not delete selected items because permission was not granted";
                uploadSuccess = "Upload Successful!";
                uploadMessage = "All images and videos have been uploaded.";
                uploadFailed = "Upload Failed!";
                uploadFailedMessage = "Please try again later.";
                uploadButtonText = "Close";
                uploadGalleryText = "UPLOAD TO GALLERY";
                authRequiredTitle = "Login Required";
                authRequiredMessage = "Please log in again to access Album.";
                wifiInvalidTitle = "Invalid Wifi";
                wifiInvalidMessage = "Please connect to the correct Wifi to upload";
                infoMissingTitle = "Information Missing";
                infoMissingMessage = "Account info not updated. Please contact PS#172.";
                uploadTitleTrans = "Confirm Upload";
                uploadMsgTrans = "Are you sure you want to upload the selected media to the system?";
                uploadConfirmTrans = "Upload";
                deleteTitleTrans = "Confirm Delete";
                deleteMsgTrans = "Deleted files cannot be recovered. Do you want to continue?";
                deleteConfirmTrans = "Delete";
                cancelTextTrans = "Cancel";
                break;
        }
        if (!isAllSelected && selectAllText != null) {
            selectAllText.setText(selectAllTextString);
        }
    }
    // ❌ Removed OLD showCustomDialog method

    public void uploadSelectedMedia(List<MediaItem> selectedItems) {
        // Hiển thị loading trước khi upload
        runOnUiThread(() -> {
            progressDialog = new ProgressDialog(AlbumActivity.this);
            progressDialog.setMessage(loadingText);
            progressDialog.setCancelable(false); // Không cho cancel khi đang upload
            progressDialog.show();
        });

        OkHttpClient client = new OkHttpClient();

        // Tạo folderName tương tự
        String folderName = "";
        try {
            String empno = userJson.getString("empno");
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-HHmmss");
            String currentDateTime = sdf.format(new Date());
            folderName = empno + "-" + currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý lỗi
            return;
        }

        JSONArray dataArray = new JSONArray();
        List<MultipartBody.Part> videoParts = new ArrayList<>();

        // Chuẩn bị MultipartBody.Builder
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // Xử lý từng item
        int videoIndex = 0;
        for (MediaItem item : selectedItems) {
            if (!item.isVideo) {
                try {
                    Uri imageUri = Uri.parse(item.uri);

                    // Khắc phục độ trễ của MediaStore Database bằng cách đọc EXIF trực tiếp từ File vật lý
                    int orientation = ExifInterface.ORIENTATION_NORMAL;
                    String[] proj = { MediaStore.Images.Media.DATA };
                    try (Cursor cursor = getContentResolver().query(imageUri, proj, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            String filePath = cursor.getString(colIndex);
                            if (filePath != null) {
                                ExifInterface exif = new ExifInterface(filePath);
                                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Đọc file ảnh dưới dạng InputStream
                    InputStream input = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    Matrix matrix = new Matrix();
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        matrix.postRotate(90);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        matrix.postRotate(180);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        matrix.postRotate(270);
                    }

                    // Quay cứng Bitmap vật lý nếu có góc xoay
                    if (!matrix.isIdentity() && bitmap != null) {
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        if (rotatedBitmap != bitmap) {
                            bitmap.recycle();
                            bitmap = rotatedBitmap;
                        }
                    }

                    if (bitmap != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        String base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);

                        JSONObject photoItem = new JSONObject();
                        photoItem.put("photo", "data:image/jpeg;base64," + base64Image);

                        JSONObject pos = getExifLocationFromUri(imageUri);
                        photoItem.put("pos", pos);

                        dataArray.put(photoItem);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Uri videoUri = Uri.parse(item.uri);

                    // --- Bổ sung đoạn lấy metadata và GPS vị trí video ở đây ---
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(AlbumActivity.this, videoUri);

                    // Lấy duration
                    String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long duration = durationStr != null ? Long.parseLong(durationStr) : 0;

                    // Lấy resolution (có thể thêm nếu cần)
                    String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

                    // Lấy GPS ưu tiên từ file sidecar
                    JSONObject posJson = getVideoGpsFromSidecar(videoUri);

                    // Nếu không có GPS từ file sidecar, fallback lấy từ metadata video
                    if (posJson == null) {
                        String locationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
                        if (locationStr != null) {
                            try {
                                String latStr = locationStr.substring(0, locationStr.indexOf('-', 1));
                                String lonStr = locationStr.substring(locationStr.indexOf('-', 1));
                                posJson = new JSONObject();
                                posJson.put("lat", Double.parseDouble(latStr));
                                posJson.put("long", Double.parseDouble(lonStr));
                            } catch (Exception e) {
                                posJson = null;
                            }
                        }
                    }

                    retriever.release();
                    long formatDuration = duration/1000;
                    long x60Duration = formatDuration*60;
                    Log.e("x60Duration", String.valueOf(x60Duration));
                    // Thêm video file vào multipart
                    File videoFile = new File(FileUtils.getPath(AlbumActivity.this, videoUri));
                    RequestBody videoBody = RequestBody.create(videoFile, MediaType.parse("video/mp4"));
                    multipartBuilder.addFormDataPart("videos[]", "video-" + videoIndex + ".mp4", videoBody);
                    multipartBuilder.addFormDataPart("video_times[]", String.valueOf(x60Duration));
                    // Tạo JSONObject cho video metadata gửi lên server (có pos và duration)
                    JSONObject videoJson = new JSONObject();
                    videoJson.put("filename", "video-" + videoIndex + ".mp4");

                    videoJson.put("time_video", x60Duration);
                    videoJson.put("pos", posJson != null ? posJson : JSONObject.NULL);
                    multipartBuilder.addFormDataPart("video_positions[]", posJson != null ? posJson.toString() : "");
                    // Thêm video metadata vào dataArray
                    dataArray.put(videoJson);

                    Log.d("UploadPayload", "Video[" + videoIndex + "]: uri=" + videoUri.toString() +
                            ", fileName=" + videoFile.getName() +
                            ", fileSize=" + videoFile.length() + " bytes" +
                            ", time_video=" + x60Duration +
                            ", pos=" + (posJson != null ? posJson.toString() : "null"));

                    videoIndex++;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("empid", userJson.optInt("id"));
            payload.put("username", userJson.optString("username"));
            payload.put("password", userJson.optString("password")); // Cẩn thận
            payload.put("name", userJson.optString("name"));
            payload.put("email", userJson.optString("email"));
            payload.put("empno", userJson.optString("empno"));
            payload.put("high_dept", userJson.optString("high_dept"));
            payload.put("dept", userJson.optString("dept"));
            payload.put("folder", folderName);
            payload.put("data", dataArray);
            Log.d("UploadPayload", payload.toString(4));
            multipartBuilder.addFormDataPart("payload", payload.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody requestBody = multipartBuilder.build();

        String url = "http://gmo021.cansportsvg.com/api/camera-api/uploadMediaForAndroidApp";

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.os.Handler mainHandler = new android.os.Handler(AlbumActivity.this.getMainLooper());
                mainHandler.post(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(AlbumActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                android.os.Handler mainHandler = new android.os.Handler(AlbumActivity.this.getMainLooper());
                mainHandler.post(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    if (response.isSuccessful()) {
                        adapter.showCustomDialog(
                                R.drawable.check_circle,
                                R.color.bluesuccess,
                                uploadSuccess,
                                uploadMessage,
                                getLocalizedString("ok"),
                                () -> {
                                    adapter.deselectAll();
                                    adapter.notifyDataSetChanged();
                                }
                        );
                    } else {
                        adapter.showCustomDialog(
                                R.drawable.ic_x_circle,
                                R.color.red,
                                uploadFailed,
                                uploadFailedMessage,
                                getLocalizedString("close"),
                                null
                        );
                    }

                });
            }

        });
    }


    public void showUploadSuccessDialog() {
        adapter.showCustomDialog(
                R.drawable.check_circle,
                R.color.bluesuccess,
                uploadSuccess,
                uploadMessage,
                getLocalizedString("ok"),
                () -> {
                    adapter.deselectAll();
                    adapter.notifyDataSetChanged();
                }
        );
    }
    private void updateTitle() {
        long selectedCount = mediaItems.stream().filter(m -> m.isSelected).count();
        boolean selectionMode = selectedCount > 0;

        if (selectionMode) {
            String selectedText;
            switch (currentLanguage) {
                case "vi":
                    selectedText = selectedCount + " mục đã chọn";
                    break;
                case "cn":
                    selectedText = "已选择 " + selectedCount + " 项";
                    break;
                case "en":
                default:
                    selectedText = selectedCount + " selected";
                    break;
            }
            titleText.setText(selectedText);

            deleteButton.setVisibility(View.VISIBLE);
            actionBarLayout.setVisibility(View.VISIBLE);
        } else {
            titleText.setText(defaultTitle); // dùng title theo ngôn ngữ hiện tại
            deleteButton.setVisibility(View.GONE);
            actionBarLayout.setVisibility(View.GONE);
        }

        isAllSelected = selectedCount == mediaItems.size() && mediaItems.size() > 0;
        updateSelectAllIcon();
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu, menu);
        selectAllItem = menu.findItem(R.id.select_all);
        selectAllItem.setVisible(false); // ẩn mặc định
        return true;
    }
    private void logVideoMetadata(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);

            Log.d("VIDEO_META", "Duration: " + duration + " ms");
            Log.d("VIDEO_META", "Resolution: " + width + "x" + height);

            if (location != null) {
                Log.d("VIDEO_META", "Location: " + location);
            } else {
                Log.d("VIDEO_META", "No GPS metadata found in video");

                String videoName = queryDisplayNameFromUri(videoUri); // ví dụ trả về "20250530_110038.mp4"
                if (videoName != null) {
                    // Bỏ phần mở rộng ".mp4" để lấy đúng tên file sidecar
                    if (videoName.endsWith(".mp4")) {
                        videoName = videoName.substring(0, videoName.length() - 4);
                    }

                    File videoFolder = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    File gpsFile = new File(videoFolder, videoName + ".mp4.txt");

                    Log.d("VIDEO_META", "Looking for sidecar GPS file at: " + gpsFile.getAbsolutePath());

                    if (gpsFile.exists()) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(gpsFile))) {
                            String line;
                            String lat = null, lon = null;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("Latitude:")) {
                                    lat = line.substring("Latitude:".length()).trim();
                                } else if (line.startsWith("Longitude:")) {
                                    lon = line.substring("Longitude:".length()).trim();
                                }
                            }
                            if (lat != null && lon != null) {
                                Log.d("VIDEO_META", "GPS from sidecar file - Latitude: " + lat + ", Longitude: " + lon);
                            } else {
                                Log.d("VIDEO_META", "Sidecar GPS file found but no valid data");
                            }
                        } catch (IOException e) {
                            Log.e("VIDEO_META", "Failed to read GPS sidecar file: " + e.getMessage());
                        }
                    } else {
                        Log.d("VIDEO_META", "No GPS sidecar file found");
                    }
                } else {
                    Log.d("VIDEO_META", "Cannot get video name from URI");
                }
            }

        } catch (Exception e) {
            Log.e("VIDEO_META", "Failed to retrieve metadata: " + e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e("VIDEO_META", "Failed to release retriever: " + e.getMessage());
            }
        }
    }
    public JSONObject getVideoGpsFromSidecar(Uri videoUri) {
        try {
            String videoName = queryDisplayNameFromUri(videoUri);
            if (videoName != null && videoName.endsWith(".mp4")) {
                videoName = videoName.substring(0, videoName.length() - 4);
            } else {
                return null;
            }

            File videoFolder = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            File gpsFile = new File(videoFolder, videoName + ".mp4.txt");
            if (!gpsFile.exists()) return null;

            String lat = null, lon = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(gpsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Latitude:")) {
                        lat = line.substring("Latitude:".length()).trim();
                    } else if (line.startsWith("Longitude:")) {
                        lon = line.substring("Longitude:".length()).trim();
                    }
                }
            }

            if (lat != null && lon != null) {
                JSONObject posJson = new JSONObject();
                posJson.put("lat", Double.parseDouble(lat));
                posJson.put("long", Double.parseDouble(lon));
                return posJson;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getExifLocationFromUri(Uri imageUri) {
        JSONObject pos = new JSONObject();
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(imageUri);
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                if (lat != null && lon != null && latRef != null && lonRef != null) {
                    double latitude = convertToDegree(lat);
                    if (latRef.equals("S")) latitude = -latitude;

                    double longitude = convertToDegree(lon);
                    if (lonRef.equals("W")) longitude = -longitude;

                    pos.put("lat", latitude);
                    pos.put("long", longitude);
                }

                inputStream.close();
            }
        } catch (IOException | JSONException e) {
            Log.e("EXIF", "Error reading EXIF: " + e.getMessage());
        }

        return pos;
    }


    // Hàm phụ để lấy tên file video từ URI (MediaStore)
    private String queryDisplayNameFromUri(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e("QUERY_NAME", "Failed to get display name: " + e.getMessage());
        }
        return null;
    }





    private void logExifFromUri(Uri imageUri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(imageUri);
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                // Ví dụ lấy một số trường thông dụng
                String make = exif.getAttribute(ExifInterface.TAG_MAKE);
                String model = exif.getAttribute(ExifInterface.TAG_MODEL);
                String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                String latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                if (lat != null && lon != null && latRef != null && lonRef != null) {
                    double latitude = convertToDegree(lat);
                    if (latRef.equals("S")) latitude = -latitude;

                    double longitude = convertToDegree(lon);
                    if (lonRef.equals("W")) longitude = -longitude;

                    Log.d("EXIF_LOG", "Latitude: " + latitude + ", Longitude: " + longitude);
                } else {
                    Log.d("EXIF_LOG", "No GPS info found");
                }
                Log.d("EXIF_LOG", "Make: " + make);
                Log.d("EXIF_LOG", "Model: " + model);
                Log.d("EXIF_LOG", "Orientation: " + orientation);
                Log.d("EXIF_LOG", "DateTime: " + dateTime);

                inputStream.close();
            }
        } catch (IOException e) {
            Log.e("EXIF_LOG", "Failed to read EXIF: " + e.getMessage());
        }
    }
    private double convertToDegree(String stringDMS) {
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        double D0 = Double.parseDouble(stringD[0]);
        double D1 = Double.parseDouble(stringD[1]);
        double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        double M0 = Double.parseDouble(stringM[0]);
        double M1 = Double.parseDouble(stringM[1]);
        double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        double S0 = Double.parseDouble(stringS[0]);
        double S1 = Double.parseDouble(stringS[1]);
        double FloatS = S0 / S1;

        return FloatD + (FloatM / 60) + (FloatS / 3600);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (adapter.isSelectionMode()) {
                adapter.deselectAll();
                updateTitle();
                return true;
            }
            finish(); // quay lại màn hình trước
            return true;
        } else if (item.getItemId() == R.id.select_all) {
            adapter.selectAll();
            updateTitle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSelectAllIcon() {
        if (isAllSelected) {
            selectAllText.setText(deselectAllTextString);
        } else {
            selectAllText.setText(selectAllTextString);
        }
    }




    private void checkPermissionsAndLoad() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Media permissions
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // Location permission for SSID
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Nearby Wi-Fi permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSION
            );
            return;
        }

        // Nếu đã đủ quyền thì chạy bình thường
        getSSIDAllowed(); // lấy danh sách SSID được phép
        loadMedia();      // load media như cũ
    }

    public void showPreviewDialog(int startPosition) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_preview);

        TextView mediaPositionText = dialog.findViewById(R.id.mediaPositionText);
        SubsamplingScaleImageView imageView = dialog.findViewById(R.id.imageView);
        ZoomablePlayerView playerView = dialog.findViewById(R.id.playerView);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setControllerAutoHideTime(1000);

        ImageView closeBtn = dialog.findViewById(R.id.btnClose);
        TextView watermarkDiagonal2 = dialog.findViewById(R.id.watermarkDiagonal2);


        SimpleExoPlayer exoPlayer = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        final int[] currentIndex = {startPosition};

        Runnable displayMedia = () -> {
            if (currentIndex[0] < 0) currentIndex[0] = mediaItems.size() - 1;
            if (currentIndex[0] >= mediaItems.size()) currentIndex[0] = 0;

            MediaItem item = mediaItems.get(currentIndex[0]);
            Uri mediaUri = Uri.parse(item.getUri());

            String watermarkText = newUser.getCardId() + " - " + newUser.getName() + " - " + newUser.getCardId();
            watermarkDiagonal2.setText(watermarkText);
            mediaPositionText.setText((currentIndex[0] + 1) + "/" + mediaItems.size());

            if (item.isVideo()) {
                exoPlayer.stop();
                imageView.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);
                playerView.resetZoom();
                exoPlayer.setMediaItem(androidx.media3.common.MediaItem.fromUri(mediaUri));
                exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                exoPlayer.prepare();
                exoPlayer.play();
            }  else {
                exoPlayer.pause();
                playerView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                new Thread(() -> {
                    try {
                        String path = FileUtils.getPath(this, mediaUri); // dùng biến mediaUri đã có sẵn

                        ExifInterface exif = new ExifInterface(path);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

                        Matrix matrix = new Matrix();
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
                            case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
                            case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                        runOnUiThread(() -> imageView.setImage(ImageSource.bitmap(rotatedBitmap)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> imageView.setImage(ImageSource.uri(mediaUri))); // fallback
                    }
                }).start();

            }
        };

        displayMedia.run();

        closeBtn.setOnClickListener(v -> {
            exoPlayer.release();
            dialog.dismiss();
        });

        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_MIN_DISTANCE = 30;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_MIN_DISTANCE) {
                    if (diffX > 0 && currentIndex[0] > 0) {
                        currentIndex[0]--;
                        displayMedia.run();
                    } else if (diffX < 0 && currentIndex[0] < mediaItems.size() - 1) {
                        currentIndex[0]++;
                        displayMedia.run();
                    }
                    return true;
                } else if (diffY > SWIPE_MIN_DISTANCE && velocityY > 1000) {
                    // Vuốt đủ dài và đủ nhanh theo trục dọc
                    View decor = dialog.getWindow().getDecorView();
                    decor.animate()
                            .translationY(decor.getHeight())
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> {
                                exoPlayer.release();
                                dialog.dismiss();
                            })
                            .start();
                    return true;
                }

                return false;
            }

        });

        View dialogView = dialog.getWindow().getDecorView();
        dialogView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        imageView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        playerView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        dialog.show();
    }







    private void loadMedia() {
        mediaItems.clear();
        ContentResolver resolver = getContentResolver();

        Uri collection = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Video.VideoColumns.DURATION  // Chỉ có hiệu lực với video
        };

        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        Cursor cursor = resolver.query(collection, projection, selection, null, sortOrder);
        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
            int typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                int mediaType = cursor.getInt(typeColumn);

                Uri contentUri;
                boolean isVideo;
                long duration = 0;

                if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    isVideo = false;
                } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    isVideo = true;
                    if (durationColumn != -1) {
                        duration = cursor.getLong(durationColumn);
                    }
                } else {
                    continue; // Skip unsupported
                }

                mediaItems.add(new MediaItem(contentUri.toString(), false, isVideo, duration));
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                getSSIDAllowed(); // Gọi lại lấy SSID nếu cần
                loadMedia();      // Tiếp tục logic chính
            } else {
                Toast.makeText(this, "Bạn cần cấp đủ quyền để sử dụng ứng dụng", Toast.LENGTH_LONG).show();
                finish(); // Đóng nếu không đủ quyền
            }
        }
    }

    public void getInfoByEmpNo(String cardId) {
        getInfoByEmpNoInternal(cardId, true);
    }

    private void getInfoByEmpNoInternal(String cardId, boolean tryFallback) {
        OkHttpClient client = new OkHttpClient();

        String url = "http://gmo021.cansportsvg.com/api/camera-api/getInfoByEmpNo";
        RequestBody formBody = new FormBody.Builder()
                .add("empno", cardId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("API_CALL", "Request failed: " + e.getMessage());

                // Nếu lỗi và cho phép fallback, thử lại bằng cách bỏ ký tự đầu
                if (tryFallback && cardId.length() > 1) {
                    String fallbackCardId = cardId.substring(1);
                    Log.d("API_CALL", "Retrying with fallback cardId: " + fallbackCardId);
                    getInfoByEmpNoInternal(fallbackCardId, false); // không retry lần nữa nếu tiếp tục fail
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    try {
                        userJson = new JSONObject(body);
                        // Xử lý JSON tại đây
                        Log.d("API_CALL", "User info: " + userJson.toString());
                    } catch (JSONException e) {
                        Log.e("API_CALL", "Failed to parse JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("API_CALL", "Request failed with code: " + response.code());

                    // Nếu HTTP response không thành công và cho phép fallback
                    if (tryFallback && cardId.length() > 1) {
                        String fallbackCardId = cardId.substring(1);
                        Log.d("API_CALL", "Retrying with fallback cardId: " + fallbackCardId);
                        getInfoByEmpNoInternal(fallbackCardId, false);
                    }
                }
            }
        });
    }

    public void setPendingDeleteUri(Uri uri) {
        this.pendingDeleteUri = uri;
    }

    public Uri getPendingDeleteUri() {
        return pendingDeleteUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DELETE_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                if (pendingDeleteUri != null) {
                    try {
                        int deleted = getContentResolver().delete(pendingDeleteUri, null, null);
                        if (deleted > 0) {
                            // Xóa khỏi danh sách và cập nhật UI
                            Iterator<MediaItem> iterator = mediaItems.iterator();
                            while (iterator.hasNext()) {
                                MediaItem item = iterator.next();
                                if (item.uri.equals(pendingDeleteUri.toString())) {
                                    iterator.remove();
                                    break;
                                }
                            }
                            adapter.deselectAll();
                            adapter.notifyDataSetChanged();

                            adapter.showCustomDialog(
                                    R.drawable.check_circle,
                                    R.color.bluesuccess,
                                    getLocalizedString("delete_success_title"),
                                    getLocalizedString("delete_success_message"),
                                    getLocalizedString("ok"),
                                    null
                            );
                        } else {
                            adapter.showCustomDialog(
                                    R.drawable.ic_x_circle,
                                    R.color.red,
                                    getLocalizedString("delete_failed_title"),
                                    getLocalizedString("delete_failed_message"),
                                    getLocalizedString("close"),
                                    null
                            );
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    } finally {
                        pendingDeleteUri = null;
                    }
                }
            } else {
                Toast.makeText(this, permissionDeniedText, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getLocalizedString(String key) {
        String language = prefs.getString("app_language", "en");
        switch (language) {
            case "vi":
                switch (key) {
                    case "delete_confirm_title": return "Xác nhận xóa";
                    case "delete_confirm_message": return "Bạn có chắc muốn xóa? Ảnh/video đã chọn sẽ bị mất vĩnh viễn.";
                    case "delete": return "Xóa";
                    case "cancel": return "Hủy";
                    case "deleted": return "Đã xóa mục đã chọn";
                    case "upload_done": return "Tải lên hoàn tất";
                    case "delete_success_title": return "Xóa Thành Công!";
                    case "delete_success_message": return "Tất cả hình ảnh/video đã được xóa.";
                    case "delete_failed_title": return "Xóa Thất Bại!";
                    case "delete_failed_message": return "Không thể xóa một số mục. Vui lòng kiểm tra quyền truy cập.";
                    case "close": return "Đóng";
                    case "ok": return "OK";
                }
                break;
            case "cn":
                switch (key) {
                    case "delete_confirm_title": return "删除确认";
                    case "delete_confirm_message": return "您确定要删除吗？所选的照片/视频将被永久删除。";
                    case "delete": return "删除";
                    case "cancel": return "取消";
                    case "deleted": return "已删除所选项";
                    case "upload_done": return "上传完成";
                    case "delete_success_title": return "删除成功！";
                    case "delete_success_message": return "所有图片都已删除。";
                    case "delete_failed_title": return "删除失败！";
                    case "delete_failed_message": return "无法删除某些项。请检查访问权限。";
                    case "close": return "关闭";
                    case "ok": return "好";
                }
                break;
            case "en":
            default:
                switch (key) {
                    case "delete_confirm_title": return "Delete Confirmation";
                    case "delete_confirm_message": return "Are you sure you want to delete? The selected photos/videos will be permanently lost.";
                    case "delete": return "Delete";
                    case "cancel": return "Cancel";
                    case "deleted": return "Selected items deleted";
                    case "upload_done": return "Upload completed";
                    case "delete_success_title": return "Delete Successful!";
                    case "delete_success_message": return "All selected images/videos have been deleted.";
                    case "delete_failed_title": return "Delete Failed!";
                    case "delete_failed_message": return "Some items could not be deleted. Please check your permissions.";
                    case "close": return "Close";
                    case "ok": return "OK";
                }
                break;
        }
        return key; // fallback
    }
    @Override
    public void onBackPressed() {
        if (adapter != null && adapter.isSelectionMode()) {
            adapter.deselectAll();
            updateTitle();
        } else {
            super.onBackPressed();
        }
    }
}
