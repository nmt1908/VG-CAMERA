package com.example.vgcamera;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog {
    private final Dialog dialog;
    private final ProgressBar progressBar;
    private final TextView progressText;
    private final Handler handler = new Handler();

    public CustomProgressDialog(Context context) {
        dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getColor(R.color.bluesuccess)));
        dialog.setCancelable(false);

        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.progress_text);
    }

    public void show() {
        dialog.show();
    }
    public void setProgress(int percent) {
        updateProgress(percent);
    }
    public void dismiss() {
        dialog.dismiss();
    }
    public void setMessage(String message) {
        handler.post(() -> {
            progressText.setText(message); // Nếu bạn có TextView để hiển thị message
        });
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }

    public void updateProgress(int percent) {
        handler.post(() -> {
            progressBar.setProgress(percent);
            progressText.setText(percent + "%");
        });
    }
}
