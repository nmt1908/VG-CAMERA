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
import java.io.IOException;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

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
    private final List<Purpose> selectedPurposes;
    private final String currentLanguage;


//    public MediaUploader(AlbumActivity activity, JSONObject userJson) {
//        this.activity = activity;
//        this.userJson = userJson;
//        this.client = new OkHttpClient.Builder()
//                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
//                .writeTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
//                .readTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
//                .build();
//
//        this.progressDialog = new CustomProgressDialog(activity);
//        this.progressDialog.setMessage("Đang tải lên...");
//        this.progressDialog.setCancelable(false);
//    }
    public MediaUploader(AlbumActivity activity, JSONObject userJson,
                         List<Purpose> selectedPurposes, String currentLanguage) {
        this.activity = activity;
        this.userJson = userJson;
        this.selectedPurposes = selectedPurposes != null ? selectedPurposes : new ArrayList<>();
        this.currentLanguage = currentLanguage != null ? currentLanguage : "en";

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

        uploadImagesInParallel(images, () -> uploadVideosOneByOne(videos, 0));
    }

    private void uploadImagesInParallel(List<MediaItem> images, Runnable onComplete) {
        if (images.isEmpty()) {
            onComplete.run();
            return;
        }

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(3);
        java.util.concurrent.atomic.AtomicInteger remainingTasks = new java.util.concurrent.atomic.AtomicInteger(images.size());
        java.util.concurrent.atomic.AtomicBoolean hasError = new java.util.concurrent.atomic.AtomicBoolean(false);

        for (int i = 0; i < images.size(); i++) {
            final int globalIndex = i;
            final MediaItem item = images.get(i);

            executor.submit(() -> {
                boolean success = uploadSingleImageMultipart(item, globalIndex);
                if (!success) {
                    hasError.set(true);
                } else {
                    synchronized (MediaUploader.this) {
                        uploadedCount++;
                        updateProgress();
                    }
                }

                if (remainingTasks.decrementAndGet() == 0) {
                    executor.shutdown();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (hasError.get()) {
                            progressDialog.dismiss();
                            Toast.makeText(activity, "Một số ảnh tải lên thất bại", Toast.LENGTH_SHORT).show();
                        } else {
                            onComplete.run();
                        }
                    });
                }
            });
        }
    }

    private boolean uploadSingleImageMultipart(MediaItem item, int globalIndex) {
        try {
            Uri uri = Uri.parse(item.uri);
            Log.d(TAG, "Bắt đầu tải song song ảnh: " + item.uri + ", index = " + globalIndex);

            int orientation = android.media.ExifInterface.ORIENTATION_NORMAL;
            String[] proj = { MediaStore.Images.Media.DATA };
            try (android.database.Cursor cursor = activity.getContentResolver().query(uri, proj, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    String filePath = cursor.getString(colIndex);
                    if (filePath != null) {
                        android.media.ExifInterface exif = new android.media.ExifInterface(filePath);
                        orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap bitmap = decodeAndScaleUri(uri, 1280);

            android.graphics.Matrix matrix = new android.graphics.Matrix();
            if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            if (!matrix.isIdentity() && bitmap != null) {
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (rotatedBitmap != bitmap) {
                    bitmap.recycle();
                    bitmap = rotatedBitmap;
                }
            }

            byte[] imageBytes = new byte[0];
            if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                imageBytes = stream.toByteArray();
                bitmap.recycle();
            }

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            JSONObject pos = activity.getExifLocationFromUri(uri);
            RequestBody imageBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
            builder.addFormDataPart("photos[]", "photo-" + globalIndex + ".jpg", imageBody);
            builder.addFormDataPart("photo_positions[]", pos != null ? pos.toString() : "");
            builder.addFormDataPart("photo_indexes[]", String.valueOf(globalIndex));

            JSONObject payload = buildBasePayload();
            builder.addFormDataPart("payload", payload.toString());

            RequestBody requestBody = builder.build();
            return sendRequest(requestBody);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tải ảnh song song " + globalIndex, e);
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

            RequestBody requestBody = builder.build();
            ProgressRequestBody progressBody = new ProgressRequestBody(requestBody, (bytesWritten, contentLength) -> {
                int basePercent = (int) (((float) uploadedCount / totalMediaCount) * 100);
                int currentBatchPercent = (int) (1.0f / totalMediaCount * 100);
                float progressFraction = contentLength > 0 ? (float) bytesWritten / contentLength : 0;
                int currentPercent = basePercent + (int) (currentBatchPercent * progressFraction);
                new Handler(Looper.getMainLooper()).post(() -> progressDialog.updateProgress(Math.min(currentPercent, 100)));
            });

            return sendRequest(progressBody);
        } catch (Exception e) {
            Log.e(TAG, "Exception in uploadSingleVideo", e);
            return false;
        }
    }


    private boolean sendRequest(RequestBody requestBody) {
        try {
            Request request = new Request.Builder()
                    .url("http://gmo021.cansportsvg.com/api/camera-api/uploadMediaForAndroidApp4")
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

//    private JSONObject buildBasePayload() throws JSONException {
//        JSONObject payload = new JSONObject();
//        payload.put("empid", userJson.optInt("id"));
//        payload.put("username", userJson.optString("username"));
//        payload.put("password", userJson.optString("password"));
//        payload.put("name", userJson.optString("name"));
//        payload.put("email", userJson.optString("email"));
//        payload.put("empno", userJson.optString("empno"));
//        payload.put("high_dept", userJson.optString("high_dept"));
//        payload.put("dept", userJson.optString("dept"));
//        payload.put("folder", getOrCreateFolderName());
//
//        // ✅ add purposes array (full object)
//        JSONArray purposes = new JSONArray();
//        for (Purpose p : selectedPurposes) {
//            purposes.put(p.toJson());
//        }
//        payload.put("purpose", purposes);
//
//        return payload;
//    }
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

        // ✅ add purposes array (full object)
        JSONArray purposes = new JSONArray();
        for (Purpose p : selectedPurposes) {
            purposes.put(p.toJson());
        }
        payload.put("purpose", purposes);

        return payload;
    }



    private Bitmap decodeAndScaleUri(Uri uri, int maxDim) {
        try {
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (java.io.InputStream input = activity.getContentResolver().openInputStream(uri)) {
                android.graphics.BitmapFactory.decodeStream(input, null, options);
            }

            int srcWidth = options.outWidth;
            int srcHeight = options.outHeight;

            int inSampleSize = 1;
            if (srcWidth > maxDim || srcHeight > maxDim) {
                int halfWidth = srcWidth / 2;
                int halfHeight = srcHeight / 2;
                while ((halfWidth / inSampleSize) >= maxDim && (halfHeight / inSampleSize) >= maxDim) {
                    inSampleSize *= 2;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            options.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;

            try (java.io.InputStream input = activity.getContentResolver().openInputStream(uri)) {
                return android.graphics.BitmapFactory.decodeStream(input, null, options);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in decodeAndScaleUri", e);
            return null;
        }
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

    private interface ProgressListener {
        void onProgress(long bytesWritten, long contentLength);
    }

    private static class ProgressRequestBody extends RequestBody {
        private final RequestBody requestBody;
        private final ProgressListener listener;

        ProgressRequestBody(RequestBody requestBody, ProgressListener listener) {
            this.requestBody = requestBody;
            this.listener = listener;
        }

        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            BufferedSink bufferedSink = Okio.buffer(sink(sink));
            requestBody.writeTo(bufferedSink);
            bufferedSink.flush();
        }

        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                long bytesWritten = 0L;
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        contentLength = contentLength();
                    }
                    bytesWritten += byteCount;
                    if (contentLength > 0 && listener != null) {
                        listener.onProgress(bytesWritten, contentLength);
                    }
                }
            };
        }
    }
}
