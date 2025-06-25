package com.example.vgcamera;

import android.app.ProgressDialog;
import android.content.Context;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MediaUploader {
    private final AlbumActivity activity;
    private final JSONObject userJson;
    private final OkHttpClient client;
    private final ProgressDialog progressDialog;

    public MediaUploader(AlbumActivity activity, JSONObject userJson) {
        this.activity = activity;
        this.userJson = userJson;
        this.client = new OkHttpClient();
        this.progressDialog = new ProgressDialog(activity);
        this.progressDialog.setMessage("Uploading...");
        this.progressDialog.setCancelable(false);
    }

    public void uploadSelectedMedia(List<MediaItem> selectedItems) {
        progressDialog.show();
        List<MediaItem> images = new ArrayList<>();
        List<MediaItem> videos = new ArrayList<>();

        for (MediaItem item : selectedItems) {
            if (item.isVideo) videos.add(item);
            else images.add(item);
        }

        uploadImagesInBatches(images, 0, () -> uploadVideosOneByOne(videos, 0));
    }

    private void uploadImagesInBatches(List<MediaItem> images, int index, Runnable onComplete) {
        if (index >= images.size()) {
            onComplete.run();
            return;
        }
        int end = Math.min(index + 20, images.size());
        List<MediaItem> batch = images.subList(index, end);

        new Thread(() -> {
            boolean success = uploadImageBatch(batch);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (success) uploadImagesInBatches(images, end, onComplete);
                else {
                    progressDialog.dismiss();
                    Toast.makeText(activity, "Image upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean uploadImageBatch(List<MediaItem> imageBatch) {
        try {
            JSONArray dataArray = new JSONArray();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            for (MediaItem item : imageBatch) {
                Uri uri = Uri.parse(item.uri);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                String base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);

                JSONObject photo = new JSONObject();
                photo.put("photo", "data:image/jpeg;base64," + base64);
                photo.put("pos", activity.getExifLocationFromUri(uri));
                dataArray.put(photo);
            }

            JSONObject payload = buildBasePayload();
            payload.put("data", dataArray);
            builder.addFormDataPart("payload", payload.toString());

            return sendRequest(builder);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void uploadVideosOneByOne(List<MediaItem> videos, int index) {
        if (index >= videos.size()) {
            progressDialog.dismiss();
            activity.showUploadSuccessDialog();
            return;
        }

        new Thread(() -> {
            boolean success = uploadSingleVideo(videos.get(index), index);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (success) uploadVideosOneByOne(videos, index + 1);
                else {
                    progressDialog.dismiss();
                    Toast.makeText(activity, "Video upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean uploadSingleVideo(MediaItem item, int videoIndex) {
        try {
            Uri uri = Uri.parse(item.uri);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(activity, uri);
            long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            long x60Duration = (duration / 1000) * 60;
            JSONObject pos = activity.getVideoGpsFromSidecar(uri);
            retriever.release();

            File file = new File(FileUtils.getPath(activity, uri));
            RequestBody videoBody = RequestBody.create(file, MediaType.parse("video/mp4"));

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("videos[]", "video-" + videoIndex + ".mp4", videoBody);
            builder.addFormDataPart("video_times[]", String.valueOf(x60Duration));
            builder.addFormDataPart("video_positions[]", pos != null ? pos.toString() : "");

            JSONObject videoJson = new JSONObject();
            videoJson.put("filename", "video-" + videoIndex + ".mp4");
            videoJson.put("time_video", x60Duration);
            videoJson.put("pos", pos != null ? pos : JSONObject.NULL);

            JSONArray dataArray = new JSONArray();
            dataArray.put(videoJson);

            JSONObject payload = buildBasePayload();
            payload.put("data", dataArray);
            builder.addFormDataPart("payload", payload.toString());

            return sendRequest(builder);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendRequest(MultipartBody.Builder builder) {
        try {
            Request request = new Request.Builder()
                    .url("http://gmo021.cansportsvg.com/api/camera-api/uploadMediaForAndroidApp")
                    .post(builder.build())
                    .build();

            Response response = client.newCall(request).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
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
        payload.put("folder", generateFolderName());
        return payload;
    }

    private String generateFolderName() {
        String empno = userJson.optString("empno");
        String time = new SimpleDateFormat("ddMMyy-HHmmss").format(new Date());
        return empno + "-" + time;
    }
}
