package com.example.vgcamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingActivity extends AppCompatActivity {
    Spinner spinnerPhotoRes, spinnerVideoRes;
    Toolbar toolbar;
    private User newUser;
    TextView photoResolution,videoResolution;
    String currentLanguage = "en";
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_setting);
        newUser = (User) getIntent().getSerializableExtra("activeUser");
         toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hiển thị nút back trên toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // Nếu có icon back custom
            getSupportActionBar().setTitle("Settings");
        }
        prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        currentLanguage = prefs.getString("app_language", "en"); // "en" mặc định

        // Các spinner và shared prefs như cũ...

        // Xử lý sự kiện nút back toolbar
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, MenuActivity.class);
            intent.putExtra("activeUser", newUser);
            startActivity(intent);
            finish();
        });
        spinnerPhotoRes = findViewById(R.id.spinnerPhotoRes);
        spinnerVideoRes = findViewById(R.id.spinnerVideoRes);
        photoResolution = findViewById(R.id.photoResolution);
        videoResolution = findViewById(R.id.videoResolution);
         prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
        updateTextsByLanguage(currentLanguage);
        setupSpinnersByLanguage(currentLanguage);
        spinnerPhotoRes.setSelection(prefs.getInt("photo_resolution_index", 2)); // default: High
        spinnerVideoRes.setSelection(prefs.getInt("video_resolution_index", 2));

        spinnerPhotoRes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("photo_resolution_index", position).apply();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerVideoRes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("video_resolution_index", position).apply();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
    private void setupSpinnersByLanguage(String lang) {
        int photoArrayId;
        int videoArrayId;

        switch (lang) {
            case "vi":
                photoArrayId = R.array.photo_resolutions_vi;
                videoArrayId = R.array.video_resolutions_vi;
                break;
            case "cn":
                photoArrayId = R.array.photo_resolutions_cn;
                videoArrayId = R.array.video_resolutions_cn;
                break;
            case "en":
            default:
                photoArrayId = R.array.photo_resolutions_en;
                videoArrayId = R.array.video_resolutions_en;
                break;
        }

        // Tạo adapter và gán cho spinner Photo
        ArrayAdapter<CharSequence> photoAdapter = ArrayAdapter.createFromResource(this,
                photoArrayId, android.R.layout.simple_spinner_item);
        photoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhotoRes.setAdapter(photoAdapter);

        // Tạo adapter và gán cho spinner Video
        ArrayAdapter<CharSequence> videoAdapter = ArrayAdapter.createFromResource(this,
                videoArrayId, android.R.layout.simple_spinner_item);
        videoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVideoRes.setAdapter(videoAdapter);
    }
    private void updateTextsByLanguage(String lang) {
        switch (lang) {
            case "vi":
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Cài đặt");
                }
                photoResolution.setText("Độ phân giải ảnh");
                videoResolution.setText("Độ phân giải video");
                break;
            case "en":
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Settings");
                }
                photoResolution.setText("Photo Resolution");
                videoResolution.setText("Video Resolution");
                break;
            case "cn":
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("设置");
                }
                photoResolution.setText("照片分辨率");
                videoResolution.setText("视频分辨率");
                break;
            default:
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Settings");
                }
                photoResolution.setText("Photo Resolution");
                videoResolution.setText("Video Resolution");
                break;
        }
    }
    @Override
    public void onBackPressed() {
        // Không gọi super -> chặn quay lại
    }

}