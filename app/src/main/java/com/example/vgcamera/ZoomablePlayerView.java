package com.example.vgcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

@UnstableApi
public class ZoomablePlayerView extends PlayerView {

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private float lastFocusX = 0f;
    private float lastFocusY = 0f;

    private boolean isControllerCurrentlyVisible = false;
    private int controllerShowTimeoutMs = 3000;

    private View videoSurfaceView;

    public ZoomablePlayerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomablePlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomablePlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setControllerShowTimeoutMs(controllerShowTimeoutMs);

        post(() -> {
            videoSurfaceView = getVideoSurfaceView();
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (videoSurfaceView == null) return false;

                float factor = detector.getScaleFactor();
                factor = Math.max(0.98f, Math.min(factor, 1.02f)); // giới hạn tốc độ zoom
                scaleFactor *= factor;
                scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 4.0f));

                videoSurfaceView.setScaleX(scaleFactor);
                videoSurfaceView.setScaleY(scaleFactor);

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                if (videoSurfaceView == null) return false;

                int[] surfaceLocation = new int[2];
                videoSurfaceView.getLocationOnScreen(surfaceLocation);

                float localX = detector.getFocusX() - surfaceLocation[0];
                float localY = detector.getFocusY() - surfaceLocation[1];

                // Chỉ set pivot nếu chưa zoom
                if (scaleFactor <= 1.01f) {
                    videoSurfaceView.setPivotX(localX);
                    videoSurfaceView.setPivotY(localY);
                }

                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (videoSurfaceView == null) return false;

                if (scaleFactor > 1.0f) {
                    resetZoom();
                } else {
                    scaleFactor = 1.5f;

                    int[] surfaceLocation = new int[2];
                    videoSurfaceView.getLocationOnScreen(surfaceLocation);

                    float localX = e.getRawX() - surfaceLocation[0];
                    float localY = e.getRawY() - surfaceLocation[1];

                    videoSurfaceView.setPivotX(localX);
                    videoSurfaceView.setPivotY(localY);
                    videoSurfaceView.setScaleX(scaleFactor);
                    videoSurfaceView.setScaleY(scaleFactor);

                    float offsetX = (scaleFactor - 1f) * (getWidth() / 2f - localX);
                    float offsetY = (scaleFactor - 1f) * (getHeight() / 2f - localY);
                    videoSurfaceView.setTranslationX(offsetX);
                    videoSurfaceView.setTranslationY(offsetY);

                    lastFocusX = localX;
                    lastFocusY = localY;
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (videoSurfaceView == null || scaleFactor <= 1.0f) return false;

                videoSurfaceView.setTranslationX(videoSurfaceView.getTranslationX() - distanceX);
                videoSurfaceView.setTranslationY(videoSurfaceView.getTranslationY() - distanceY);
                return true;
            }
        });

        super.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            isControllerCurrentlyVisible = (visibility == View.VISIBLE);
        });
    }

    public void resetZoom() {
        scaleFactor = 1.0f;
        if (videoSurfaceView != null) {
            videoSurfaceView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationX(0f)
                    .translationY(0f)
                    .setDuration(300) // Thời gian animation 300ms
                    .withStartAction(() -> {
                        // Khôi phục pivot về tâm View (hoặc về 0 nếu bạn muốn)
                        videoSurfaceView.setPivotX(videoSurfaceView.getWidth() / 2f);
                        videoSurfaceView.setPivotY(videoSurfaceView.getHeight() / 2f);
                    })
                    .start();
        }
    }


    public void setControllerAutoHideTime(int millis) {
        this.controllerShowTimeoutMs = millis;
        setControllerShowTimeoutMs(millis);
    }

    public boolean isControllerVisibleCustom() {
        return isControllerCurrentlyVisible;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN && !isControllerCurrentlyVisible) {
            showController();
        }

        if (event.getAction() == MotionEvent.ACTION_UP && videoSurfaceView != null) {
            float tx = videoSurfaceView.getTranslationX();
            float ty = videoSurfaceView.getTranslationY();

            float scaledWidth = videoSurfaceView.getWidth() * scaleFactor;
            float scaledHeight = videoSurfaceView.getHeight() * scaleFactor;

            // Tính biên sau khi scale + dịch
            float left = (getWidth() / 2f) - (scaledWidth / 2f) + tx;
            float right = left + scaledWidth;
            float top = (getHeight() / 2f) - (scaledHeight / 2f) + ty;
            float bottom = top + scaledHeight;

            boolean isLeaking =
                    left > 0 || right < getWidth() || top > 0 || bottom < getHeight();

            // Nếu scale nhỏ hơn 1.01 (zoom out) mà bị lệch hoặc zoom in mà lộ viền → reset
            if ((scaleFactor <= 1.01f && (Math.abs(tx) > 1f || Math.abs(ty) > 1f)) || isLeaking) {
                resetZoom();
            }
        }

        return super.onTouchEvent(event);
    }

}
