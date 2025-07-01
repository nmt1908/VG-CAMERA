package com.example.vgcamera;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.util.Size;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private Button btnZoom1x,btnZoom2x,btnZoom3x;
    private TextView txtPhotoMode, txtVideoMode;
    private ImageButton btnAction,btnExitCamera;

    private PreviewView previewView;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;
    private ImageButton  btnFlash,btnImageAlbum ;
    private boolean isVideoMode = false;
    private boolean flashEnabled = false;

    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording activeRecording;

    private ExecutorService cameraExecutor;
    private Camera camera;
    private TextView txtTimer,txtInfo;
    private OrientationEventListener orientationEventListener;
    private int currentRotation = Surface.ROTATION_0;

    private User newUser;
    private long startTime = 0;
    private Runnable timerRunnable;
    private final Handler handler = new Handler();
    private String currentVideoFileName;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    ImageButton photoMode,videoMode;
    private Runnable stopRecordingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_camera);
        newUser = (User) getIntent().getSerializableExtra("activeUser");
        if (newUser == null) {
            Toast.makeText(this, "Please identify your face", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CameraActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Dừng không chạy tiếp
        }

        FocusRingView focusRingView = findViewById(R.id.focusRingView);

        txtInfo=findViewById(R.id.txtInfo);
        txtInfo.setText(newUser.getCardId()+" "+newUser.getName());
        btnExitCamera=findViewById(R.id.btnExitCamera);
        previewView = findViewById(R.id.previewView);
        btnFlash = findViewById(R.id.btnFlash);
        photoMode = findViewById(R.id.btnPhotoMode);
        videoMode = findViewById(R.id.btnVideoMode);
        btnAction = findViewById(R.id.btnAction);
        txtTimer = findViewById(R.id.txtTimer);
        btnImageAlbum = findViewById(R.id.btnImageAlbum);
//        Button btnZoom1x = findViewById(R.id.btnZoom1x);
//        Button btnZoom2x = findViewById(R.id.btnZoom2x);
//        Button btnZoom5x = findViewById(R.id.btnZoom5x);
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;

                int rotation;
                int viewRotation;

                if (orientation >= 315 || orientation < 45) {
                    rotation = Surface.ROTATION_0;
                    viewRotation = 0;
                } else if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                    viewRotation = 270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                    viewRotation = 180;
                } else {
                    rotation = Surface.ROTATION_90;
                    viewRotation = 90;
                }

                if (rotation != currentRotation) {
                    currentRotation = rotation;
                    Log.d("ORIENTATION", "Device rotation changed to " + currentRotation);

                    if (videoCapture != null) {
                        videoCapture.setTargetRotation(currentRotation);
                    }

                    if (imageCapture != null) {
                        imageCapture.setTargetRotation(currentRotation);
                    }

                    rotateViewSmoothly(photoMode, viewRotation);
                    rotateViewSmoothly(videoMode, viewRotation);
                    rotateViewSmoothly(btnImageAlbum, viewRotation);
                    rotateViewSmoothly(btnAction, viewRotation);
                    rotateViewSmoothly(btnFlash, viewRotation);

                }
            }
        };
        orientationEventListener.enable();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocation();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        photoMode.setOnClickListener(v -> {
            isVideoMode = false;
            updateModeUI();
        });

        videoMode.setOnClickListener(v -> {
            isVideoMode = true;
            updateModeUI();
        });

// Nút hành động
        btnAction.setOnClickListener(v -> {
            if (isVideoMode) {
                captureVideo();
            } else {
                takePhoto();
            }
        });
        btnExitCamera.setOnClickListener(v->{
            Intent intent = new Intent(CameraActivity.this, MenuActivity.class);
            intent.putExtra("activeUser", newUser);
            startActivity(intent);
            finish();
        });
        btnFlash.setOnClickListener(v -> {
            flashEnabled = !flashEnabled;
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                camera.getCameraControl().enableTorch(flashEnabled);
                btnFlash.setImageResource(flashEnabled ? R.drawable.baseline_flash_off_24 : R.drawable.baseline_flash_on_24);
            }
        });

//        btnSwitchMode.setOnClickListener(v -> {
//            isVideoMode = !isVideoMode;
//            btnSwitchMode.setImageResource(isVideoMode ? R.drawable.ic_video_camera : R.drawable.ic_photo_camera);
//        });
        updateModeUI();
        cameraExecutor = Executors.newSingleThreadExecutor();
        previewView.setOnTouchListener(new View.OnTouchListener() {
            private float currentZoomRatio = 1f;
            private float maxZoomRatio = 1f;
            private float minZoomRatio = 1f;
            private ScaleGestureDetector scaleGestureDetector;

            {
                scaleGestureDetector = new ScaleGestureDetector(CameraActivity.this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (camera != null) {
                            float scaleFactor = detector.getScaleFactor();
                            currentZoomRatio = camera.getCameraInfo().getZoomState().getValue().getZoomRatio();
                            maxZoomRatio = camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
                            minZoomRatio = camera.getCameraInfo().getZoomState().getValue().getMinZoomRatio();

                            float newZoom = currentZoomRatio * scaleFactor;

                            // Giới hạn zoom
                            newZoom = Math.max(minZoomRatio, Math.min(newZoom, maxZoomRatio));
                            camera.getCameraControl().setZoomRatio(newZoom);
                        }
                        return true;
                    }
                });
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (camera != null) {
                        float x = event.getX();
                        float y = event.getY();

                        MeteringPointFactory factory = previewView.getMeteringPointFactory();
                        MeteringPoint point = factory.createPoint(x, y);

                        FocusMeteringAction action = new FocusMeteringAction.Builder(point,
                                FocusMeteringAction.FLAG_AF)
                                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                .build();

                        camera.getCameraControl().startFocusAndMetering(action);

                        // Hiển thị vòng focus
                        focusRingView.showFocusRing(x, y);

                        // Ẩn vòng focus sau 1.5 giây
                        focusRingView.postDelayed(() -> {
                            focusRingView.hideFocusRing();
                        }, 700);
                    }
                }
                return true;
            }
        });

//        btnZoom1x.setOnClickListener(v -> {
//            if (camera != null)
//                camera.getCameraControl().setZoomRatio(1.0f);
//        });
//
//        btnZoom2x.setOnClickListener(v -> {
//            if (camera != null)
//                camera.getCameraControl().setZoomRatio(2.0f);
//        });
//
//        btnZoom5x.setOnClickListener(v -> {
//            if (camera != null)
//                camera.getCameraControl().setZoomRatio(5.0f);
//        });
        Uri latestMediaUri = getLatestMediaUri(this);
        if (latestMediaUri != null) {
            Glide.with(this)
                    .load(latestMediaUri)
                    .centerCrop()
                    .into(btnImageAlbum);
        } else {
            btnImageAlbum.setImageResource(R.drawable.albumnoimage); //
        }

        btnImageAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(CameraActivity.this, AlbumActivity.class);
            intent.putExtra("activeUser", newUser);
            startActivity(intent);
        });


    }
    private void initLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            getLastLocation();
        }
    }
    private void rotateViewSmoothly(View view, float toRotation) {
        view.animate()
                .rotation(toRotation)
                .setDuration(300)
                .setInterpolator(new LinearInterpolator())
                .start();
    }
    private void loadLatestAlbumImage() {
        Uri latestMediaUri = getLatestMediaUri(this);
        if (latestMediaUri != null) {
            Glide.with(this)
                    .load(latestMediaUri)
                    .centerCrop()
                    .into(btnImageAlbum);
        } else {
            Glide.with(this)
                    .load(R.drawable.albumnoimage)
                    .centerCrop()
                    .into(btnImageAlbum);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadLatestAlbumImage();
    }

    private Uri getLatestMediaUri(Context context) {
        Uri collection;
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };

        String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")";


        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        collection = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                null,
                sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));

                if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                }
            }
        }
        return null;
    }

    private void updateModeUI() {
        if (isVideoMode) {
            videoMode.setImageTintList(ColorStateList.valueOf(getColor(R.color.yellow)));
            photoMode.setImageTintList(ColorStateList.valueOf(getColor(android.R.color.white)));
            btnAction.setImageResource(R.drawable.ic_video_camera);
        } else {
            photoMode.setImageTintList(ColorStateList.valueOf(getColor(R.color.yellow)));
            videoMode.setImageTintList(ColorStateList.valueOf(getColor(android.R.color.white)));
            btnAction.setImageResource(R.drawable.ic_photo_camera);
        }
    }

    private Size getPhotoResolutionForIndex(int index) {
        switch (index) {
            case 0: return new Size(480, 640);       // Low - Portrait 4:3
            case 1: return new Size(720, 1280);      // Medium - Portrait 16:9
            case 2: return new Size(1440, 1920);     // High - Portrait 4:3 (max)
            default: return new Size(1440, 1920);     // Medium
        }
    }

    private QualitySelector getVideoQualitySelector(int index) {
        switch (index) {
            case 0: return QualitySelector.from(Quality.SD);      // 480p - 640x480 (Landscape)
            case 1: return QualitySelector.from(Quality.HD);      // 720p - 1280x720 (Landscape)
            case 2: return QualitySelector.from(Quality.FHD);     // 1080p - 1920x1080 (Landscape)
            default: return QualitySelector.from(Quality.FHD);
        }
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Lấy cài đặt độ phân giải từ SharedPreferences
                SharedPreferences prefs = getSharedPreferences("VGCameraPrefs", MODE_PRIVATE);
                int photoResIndex = prefs.getInt("photo_resolution_index", 2); // Default: Medium
                int videoResIndex = prefs.getInt("video_resolution_index", 0); // Default: Medium

                Size targetPhotoSize = getPhotoResolutionForIndex(photoResIndex);
                QualitySelector videoQualitySelector = getVideoQualitySelector(videoResIndex);

                // Cấu hình chụp ảnh
                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(targetPhotoSize)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Cấu hình quay video
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(videoQualitySelector)
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture,
                        videoCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Camera init failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, ContextCompat.getMainExecutor(this));
    }


    private void takePhoto() {
        if (imageCapture == null) return;

        // Log rotation kiểu ngang/dọc
        switch (currentRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                Log.d("PHOTO", "Taking photo - orientation: Portrait (dọc)");
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                Log.d("PHOTO", "Taking photo - orientation: Landscape (ngang)");
                break;
            default:
                Log.d("PHOTO", "Taking photo - orientation: Unknown");
                break;
        }

        Log.d("PHOTO", "Taking photo with currentRotation = " + currentRotation);

        // Set lại target rotation trước khi chụp để CameraX xử lý đúng chiều ảnh
        imageCapture.setTargetRotation(currentRotation);

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VGCamera");
        }

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri != null) {
                            File file = uriToFile(savedUri);
                            try {
                                ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                                int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                                int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                                Log.d("PHOTO", "Image resolution: " + width + " x " + height);
                            } catch (IOException e) {
                                Log.e("PHOTO", "Failed to read image resolution", e);
                            }
                            if (file != null && lastKnownLocation != null) {
                                saveGpsToExif(file, lastKnownLocation); // Ghi GPS nếu có
                            }
                            Glide.with(getApplicationContext())
                                    .load(savedUri)
                                    .centerCrop()
                                    .into(btnImageAlbum);
                        } else {
                            Log.e("PHOTO", "SavedUri is null!");
                        }
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(100);
                            }
                        }

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("PHOTO", "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(getApplicationContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }






    private String convertToDMS(double coord) {
        coord = Math.abs(coord);
        int degrees = (int) coord;
        coord = (coord - degrees) * 60;
        int minutes = (int) coord;
        double seconds = (coord - minutes) * 60;

        return degrees + "/1," + minutes + "/1," + ((int)(seconds * 1000)) + "/1000";
    }
    private void saveGpsToExif(File photoFile, Location location) {
        if (location == null) {
            Log.d("EXIF_LOG", "No location to save to EXIF");
            return;
        }
        try {
            ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());

            // Latitude
            double latitude = location.getLatitude();
            String latitudeRef = latitude >= 0 ? "N" : "S";
            latitude = Math.abs(latitude);
            String latDMS = convertToDMS(latitude);

            // Longitude
            double longitude = location.getLongitude();
            String longitudeRef = longitude >= 0 ? "E" : "W";
            longitude = Math.abs(longitude);
            String lonDMS = convertToDMS(longitude);

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latDMS);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lonDMS);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef);

            exif.saveAttributes();

            Log.d("EXIF_LOG", "Saved GPS to EXIF: " + location.getLatitude() + ", " + location.getLongitude());
        } catch (IOException e) {
            Log.e("EXIF_LOG", "Error saving GPS to EXIF: " + e.getMessage());
        }
    }
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        lastKnownLocation = location;
                        Log.d("GPS_LOG", "Got location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.d("GPS_LOG", "Location is null");
                    }
                })
                .addOnFailureListener(e -> Log.e("GPS_LOG", "Failed to get location: " + e.getMessage()));
    }
    private File uriToFile(Uri uri) {
        File file = null;
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (index != -1) {
                    String filePath = cursor.getString(index);
                    file = new File(filePath);
                }
                cursor.close();
            } else {
                file = new File(uri.getPath()); // fallback
            }
        } catch (Exception e) {
            Log.e("PHOTO", "Failed to convert Uri to File: " + e.getMessage());
        }
        return file;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void captureVideo() {


        if (videoCapture == null) return;
        switch (currentRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                Log.d("VIDEO", "Start recording - orientation: Portrait (dọc)");
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                Log.d("VIDEO", "Start recording - orientation: Landscape (ngang)");
                break;
            default:
                Log.d("VIDEO", "Start recording - orientation: Unknown");
                break;
        }
        startTime = System.currentTimeMillis();
        txtTimer.setVisibility(View.VISIBLE);

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;

                int minutes = (int) (elapsed / 60000);
                int seconds = (int) ((elapsed % 60000) / 1000);
                int millis = (int) ((elapsed % 1000) / 10);

                txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", minutes, seconds, millis));
                handler.postDelayed(this, 50);
            }
        };

        handler.post(timerRunnable);
        stopRecordingRunnable = new Runnable() {
            @Override
            public void run() {
                if (activeRecording != null) {
                    Log.d("VIDEO", "Reached 5-minute limit, stopping recording.");
                    activeRecording.stop();
                    activeRecording = null;
                    handler.removeCallbacks(timerRunnable);
                    txtTimer.setVisibility(View.GONE);
                    btnAction.setImageResource(R.drawable.ic_video_camera);
                    Toast.makeText(CameraActivity.this, "Đã quay đủ 5 phút", Toast.LENGTH_SHORT).show();
                }
            }
        };
        handler.postDelayed(stopRecordingRunnable, 5 * 60 * 1000); // 5 phút
        if (activeRecording != null) {
            // Đang quay -> dừng quay
            activeRecording.stop();
            activeRecording = null;
            handler.removeCallbacks(timerRunnable);
            handler.removeCallbacks(stopRecordingRunnable);
            txtTimer.setVisibility(View.GONE);
            btnAction.setImageResource(R.drawable.ic_video_camera);
            return;
        }

        // Tạo tên file video duy nhất cho lần quay này
        currentVideoFileName = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, currentVideoFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");

        MediaStoreOutputOptions mediaStoreOutput = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build();

        PendingRecording pendingRecording = videoCapture.getOutput().prepareRecording(this, mediaStoreOutput);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pendingRecording.withAudioEnabled();
        }

        btnAction.setImageResource(R.drawable.baseline_pause_circle_24);

        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(this), event -> {
            if (event instanceof VideoRecordEvent.Finalize) {
                btnAction.setImageResource(R.drawable.ic_video_camera);

                Uri savedUri = ((VideoRecordEvent.Finalize) event).getOutputResults().getOutputUri();
                if (savedUri != null) {
                    try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(this, savedUri);
                        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                        Log.d("VIDEO", "Resolution: " + width + " x " + height + ", Bitrate: " + bitrate + " bps");
                        retriever.release();
                    } catch (Exception e) {
                        Log.e("VIDEO", "Failed to retrieve video metadata", e);
                    }
                    Glide.with(this)
                            .load(savedUri)
                            .centerCrop()
                            .into(btnImageAlbum);

                    if (lastKnownLocation != null) {
                        double lat = lastKnownLocation.getLatitude();
                        double lon = lastKnownLocation.getLongitude();
                        Log.d("VIDEO_GPS", "Video Location: " + lat + ", " + lon);

                        // Truyền currentVideoFileName để lưu đúng tên file GPS sidecar
                        saveGpsSidecar(savedUri, lat, lon, currentVideoFileName);
                    }
                }
            }
        });
    }

    private void saveGpsSidecar(Uri videoUri, double lat, double lon, String currentVideoFileName ) {
        File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir == null) {
            Log.e("VIDEO_GPS", "Cannot access app-specific Movies directory");
            return;
        }

        File sidecar = new File(dir, currentVideoFileName  + ".mp4.txt"); // Lưu cùng tên file video + .mp4.txt
        Log.d("VIDEO_GPS", "Sidecar file path: " + sidecar.getAbsolutePath());

        try (FileWriter writer = new FileWriter(sidecar)) {
            writer.write("Latitude: " + lat + "\n");
            writer.write("Longitude: " + lon + "\n");
            writer.flush();
            Log.d("VIDEO_GPS", "Saved GPS to " + sidecar.getAbsolutePath());
        } catch (IOException e) {
            Log.e("VIDEO_GPS", "Failed to write GPS info: " + e.getMessage());
        }
    }




    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }
    @Override
    public void onBackPressed() {
    }



}
