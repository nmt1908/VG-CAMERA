package com.example.vgcamera;

import static com.example.vgcamera.AlbumActivity.REQUEST_DELETE_PERMISSION;
import static okhttp3.internal.concurrent.TaskLoggerKt.formatDuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Iterator;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private List<MediaItem> mediaItems;
    private final Runnable updateTitleCallback;
    private boolean selectionMode = false;
    private final Context context;
    private final String language;


    public AlbumAdapter(List<MediaItem> mediaItems, Runnable updateTitleCallback, Context context) {
        this.mediaItems = mediaItems;
        this.updateTitleCallback = updateTitleCallback;
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("VGCameraPrefs", Context.MODE_PRIVATE);
        this.language = prefs.getString("app_language", "en");

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);
        Glide.with(holder.context)
                .load(Uri.parse(item.uri))
                .centerCrop()
                .into(holder.thumbnail);

        holder.selectIcon.setVisibility(item.isSelected ? View.VISIBLE : View.INVISIBLE);
        holder.selectIcon.setImageResource(item.isSelected ?
                R.drawable.ic_selected_circle : R.drawable.ic_unselected_circle);
        if (item.isVideo) {
            holder.videoDuration.setVisibility(View.VISIBLE);
            holder.videoDuration.setText(formatDuration(item.duration));
        } else {
            holder.videoDuration.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            item.isSelected = !item.isSelected;
            selectionMode = true;
            notifyItemChanged(position);
            updateTitleCallback.run();
            return true;
        });


        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                // Toggle selection on item click
                item.isSelected = !item.isSelected;
                notifyItemChanged(position);
                updateTitleCallback.run();

                if (getSelectedCount() == 0) {
                    selectionMode = false;
                    updateTitleCallback.run();
                }
            } else {
                // Gọi hàm showPreviewDialog với vị trí hiện tại (position)
                if (context instanceof AlbumActivity) {
                    ((AlbumActivity) context).showPreviewDialog(position);
                }
            }
        });

    }
    private String getLocalizedString(String key) {
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
    public int getItemCount() {
        return mediaItems.size();
    }

    public void selectAll() {
        for (MediaItem item : mediaItems) {
            item.isSelected = true;
        }
        selectionMode = true;
        notifyDataSetChanged();
        updateTitleCallback.run();
    }
    private String formatDuration(long durationMs) {
        int seconds = (int) (durationMs / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void deleteSelectedItems() {
        new android.app.AlertDialog.Builder(context)
                .setTitle(getLocalizedString("delete_confirm_title"))
                .setMessage(getLocalizedString("delete_confirm_message"))
                .setPositiveButton(getLocalizedString("delete"), (dialog, which) -> {
                    boolean allDeleted = true;

                    Iterator<MediaItem> iterator = mediaItems.iterator();
                    while (iterator.hasNext()) {
                        MediaItem item = iterator.next();
                        if (item.isSelected) {
                            Uri uri = Uri.parse(item.uri);
                            try {
                                int deleted = context.getContentResolver().delete(uri, null, null);
                                if (deleted > 0) {
                                    iterator.remove();
                                } else {
                                    allDeleted = false;
                                }
                            } catch (RecoverableSecurityException e) {
                                IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                                if (context instanceof AlbumActivity) {
                                    AlbumActivity activity = (AlbumActivity) context;
                                    activity.setPendingDeleteUri(uri);
                                    try {
                                        activity.startIntentSenderForResult(
                                                intentSender,
                                                REQUEST_DELETE_PERMISSION,
                                                null, 0, 0, 0
                                        );
                                        return;
                                    } catch (IntentSender.SendIntentException sendEx) {
                                        sendEx.printStackTrace();
                                    }
                                }
                                allDeleted = false;
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                                allDeleted = false;
                            }
                        }
                    }

                    notifyDataSetChanged();
                    selectionMode = false;
                    updateTitleCallback.run();

                    if (allDeleted) {
                        showCustomDialog(
                                R.drawable.check_circle,
                                R.color.bluesuccess,
                                getLocalizedString("delete_success_title"),
                                getLocalizedString("delete_success_message"),
                                getLocalizedString("ok"),
                                () -> {
                                    deselectAll();
                                    notifyDataSetChanged();
                                }
                        );
                    } else {
                        showCustomDialog(
                                R.drawable.ic_x_circle,
                                R.color.red,
                                getLocalizedString("delete_failed_title"),
                                getLocalizedString("delete_failed_message"),
                                getLocalizedString("close"),
                                null
                        );
                    }

                    dialog.dismiss();
                })
                .setNegativeButton(getLocalizedString("cancel"), (dialog, which) -> dialog.dismiss())
                .show();
    }


    public void showCustomDialog(int iconResId, int iconTintColorResId,
                                 String title, String message,
                                 String buttonText, Runnable onClose) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_common, null);
        builder.setView(dialogView);

        ImageView icon = dialogView.findViewById(R.id.dialogIcon);
        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageView = dialogView.findViewById(R.id.dialogMessage);
        Button btn = dialogView.findViewById(R.id.dialogButton);

        icon.setImageResource(iconResId);
        icon.setColorFilter(ContextCompat.getColor(context, iconTintColorResId));
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(context, iconTintColorResId));
        messageView.setText(message);
        btn.setText(buttonText);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        btn.setOnClickListener(v -> {
            dialog.dismiss();
            if (onClose != null) onClose.run();
        });

        dialog.show();
    }






    public void deselectAll() {
        for (MediaItem item : mediaItems) {
            item.isSelected = false;
        }
        selectionMode = false;
        notifyDataSetChanged();
        updateTitleCallback.run();
    }


    public boolean isSelectionMode() {
        return selectionMode;
    }


    public int getSelectedCount() {
        int count = 0;
        for (MediaItem item : mediaItems) {
            if (item.isSelected) count++;
        }
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, selectIcon;
        TextView videoDuration;
        Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            thumbnail = itemView.findViewById(R.id.thumbnail);
            selectIcon = itemView.findViewById(R.id.selectIcon);
            videoDuration = itemView.findViewById(R.id.videoDuration);
        }
    }
}
