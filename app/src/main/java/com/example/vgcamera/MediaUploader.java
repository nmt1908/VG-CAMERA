package com.example.vgcamera;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MediaUploader {
    private static final String TAG = "MediaUploader";

    private final AlbumActivity activity;
    private final JSONObject userJson;
    private final OkHttpClient client;
    private final CustomProgressDialog progressDialog;

    private String folderName;
    private long folderCreatedTime = 0;

    private int totalMediaCount = 0;
    private int uploadedCount = 0;
    private int videoGlobalIndex = 0;


    public MediaUploader(AlbumActivity activity, JSONObject userJson) {
        this.activity = activity;
        this.userJson = userJson;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
                .readTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
                .build();

        this.progressDialog = new CustomProgressDialog(activity);
        this.progressDialog.setMessage("Đang tải lên...");
        this.progressDialog.setCancelable(false);
    }

    public void uploadSelectedMedia(List<MediaItem> selectedItems) {
        totalMediaCount = selectedItems.size();
        uploadedCount = 0;
        videoGlobalIndex = 0;

        Log.d(TAG, "Tổng media được chọn: " + totalMediaCount);

        progressDialog.show();

        List<MediaItem> images = new ArrayList<>();
        List<MediaItem> videos = new ArrayList<>();

        for (MediaItem item : selectedItems) {
            if (item.isVideo) videos.add(item);
            else images.add(item);
        }

        Log.d(TAG, "Ảnh: " + images.size() + ", Video: " + videos.size());

        uploadImagesInBatches(images, 0, () -> uploadVideosOneByOne(videos, 0));
    }

    private void uploadImagesInBatches(List<MediaItem> images, int index, Runnable onComplete) {
        if (index >= images.size()) {
            onComplete.run();
            return;
        }

        int end = Math.min(index + 20, images.size());
        List<MediaItem> batch = new ArrayList<>(images.subList(index, end));

        Log.d(TAG, "Upload ảnh batch từ index " + index + " đến " + (end - 1) + ", batch size = " + batch.size());

        new Thread(() -> {
            boolean success = uploadImageBatch(batch, index);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (success) {
                    uploadedCount += batch.size();
                    updateProgress();
                    uploadImagesInBatches(images, end, onComplete);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(activity, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean uploadImageBatch(List<MediaItem> imageBatch, int startIndex) {
        try {
            JSONArray dataArray = new JSONArray();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            for (int i = 0; i < imageBatch.size(); i++) {
                MediaItem item = imageBatch.get(i);
                Uri uri = Uri.parse(item.uri);
                Log.d(TAG, "Ảnh uri: " + item.uri);

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                String base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);

                JSONObject photo = new JSONObject();
                photo.put("photo", "data:image/jpeg;base64," + base64);
                photo.put("pos", activity.getExifLocationFromUri(uri));
                photo.put("index", startIndex + i); // 👈 Thêm index toàn cục vào JSON
                dataArray.put(photo);
            }

            JSONObject payload = buildBasePayload();
            payload.put("data", dataArray);
            builder.addFormDataPart("payload", payload.toString());

            return sendRequest(builder);
        } catch (Exception e) {
            Log.e(TAG, "Exception in uploadImageBatch", e);
            return false;
        }
    }


    private void uploadVideosOneByOne(List<MediaItem> videos, int index) {
        if (index >= videos.size()) {
            progressDialog.dismiss();
            activity.showUploadSuccessDialog();
            notifyUploadCompleted();
            return;
        }

        Log.d(TAG, "Bắt đầu upload video index: " + index);

        new Thread(() -> {
            boolean success = uploadSingleVideo(videos.get(index), index);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (success) {
                    uploadedCount++;
                    updateProgress();
                    uploadVideosOneByOne(videos, index + 1);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(activity, "Video upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    private void notifyUploadCompleted() {
        new Thread(() -> {
            try {
                JSONObject payload = buildBasePayload(); // dùng lại hàm này
                RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url("http://gmo021.cansportsvg.com/api/camera-api/notifyUploadComplete2")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Email notification sent successfully");
                } else {
                    Log.e(TAG, "Failed to notify server for email. Code: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception in notifyUploadCompleted", e);
            }
        }).start();
    }
    private void updateProgress() {
        int percent = (int) ((uploadedCount / (float) totalMediaCount) * 100);
        Log.d(TAG, "Uploaded: " + uploadedCount + "/" + totalMediaCount + " (" + percent + "%)");
        new Handler(Looper.getMainLooper()).post(() -> progressDialog.updateProgress(percent));
    }

    private boolean uploadSingleVideo(MediaItem item, int videoIndex) {
        try {
            Uri uri = Uri.parse(item.uri);
            Log.d(TAG, "Video uri: " + item.uri);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(activity, uri);

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr);
            long x60Duration = (duration / 1000) * 60;

            JSONObject pos = activity.getVideoGpsFromSidecar(uri);
            retriever.release();

            String filePath = FileUtils.getPath(activity, uri);
            File file = new File(filePath);

            if (!file.exists()) {
                Log.e(TAG, "Video file không tồn tại: " + file.getAbsolutePath());
                return false;
            }

            Log.d(TAG, "File video path: " + file.getAbsolutePath());
            Log.d(TAG, "Duration: " + duration + " ms, x60Duration: " + x60Duration);

            // 🔥 Đặt tên duy nhất cho video
            String videoFileName = "video-" + videoGlobalIndex + "-" + System.currentTimeMillis() + ".mp4";

            RequestBody videoBody = RequestBody.create(file, MediaType.parse("video/mp4"));
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("videos[]", videoFileName, videoBody);
            builder.addFormDataPart("video_times[]", String.valueOf(x60Duration));
            builder.addFormDataPart("video_positions[]", pos != null ? pos.toString() : "");

            JSONObject videoJson = new JSONObject();
            videoJson.put("filename", videoFileName);
            videoJson.put("time_video", x60Duration);
            videoJson.put("pos", pos != null ? pos : JSONObject.NULL);
            videoJson.put("index", videoGlobalIndex); // 💡 thêm index nếu server cần

            JSONArray dataArray = new JSONArray();
            dataArray.put(videoJson);

            JSONObject payload = buildBasePayload();
            payload.put("data", dataArray);
            builder.addFormDataPart("payload", payload.toString());

            videoGlobalIndex++; // 👈 tăng để không trùng

            return sendRequest(builder);
        } catch (Exception e) {
            Log.e(TAG, "Exception in uploadSingleVideo", e);
            return false;
        }
    }


    private boolean sendRequest(MultipartBody.Builder builder) {
        try {
            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url("http://gmo021.cansportsvg.com/api/camera-api/uploadMediaForAndroidApp2")
                    .post(requestBody)
                    .build();

            Log.d(TAG, "Sending request to: " + request.url());

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e(TAG, "Upload thất bại: HTTP " + response.code());
                String errorBody = response.body() != null ? response.body().string() : "null";
                Log.e(TAG, "Response error body: " + errorBody);
            } else {
                Log.d(TAG, "Upload thành công: HTTP " + response.code());
            }

            return response.isSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Exception during sendRequest", e);
            return false;
        }
    }

    private JSONObject buildBasePayload() throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("empid", userJson.optInt("id"));
        payload.put("username", userJson.optString("username"));
        payload.put("password", userJson.optString("password"));
        payload.put("name", userJson.optString("name"));
        payload.put("email", userJson.optString("email"));
        payload.put("empno", userJson.optString("empno"));
        payload.put("high_dept", userJson.optString("high_dept"));
        payload.put("dept", userJson.optString("dept"));
        payload.put("folder", getOrCreateFolderName());
        return payload;
    }

    private String getOrCreateFolderName() {
        long now = System.currentTimeMillis();
        long tenMinutes = 10 * 60 * 1000;

        if (folderName == null || (now - folderCreatedTime) > tenMinutes) {
            folderCreatedTime = now;
            folderName = generateFolderName();
        }

        return folderName;
    }

    private String generateFolderName() {
        String empno = userJson.optString("empno");
        String time = new SimpleDateFormat("ddMMyy-HHmmss").format(new Date());
        return empno + "-" + time;
    }
}
