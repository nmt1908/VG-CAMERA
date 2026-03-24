package com.example.vgcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

public class StrokeTextView extends androidx.appcompat.widget.AppCompatTextView {

    public StrokeTextView(Context context) {
        super(context);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Lưu màu chữ gốc từ XML
        int currentTextColor = getCurrentTextColor();

        // 1. Vẽ viền (stroke) màu Đen mờ để nổi bật trên nền sáng
        TextPaint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        setTextColor(Color.parseColor("#80000000")); // Black with 50% opacity
        super.onDraw(canvas);

        // 2. Vẽ chữ bên trong (fill) với màu gốc từ XML
        paint.setStyle(Paint.Style.FILL);
        setTextColor(currentTextColor);
        super.onDraw(canvas);
    }
}
