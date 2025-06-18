package com.example.vgcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

public class StrokeTextView extends androidx.appcompat.widget.AppCompatTextView {
    private int strokeColor = Color.YELLOW;
    private float strokeWidth = 4;

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
        // Lưu màu chữ gốc
        int currentTextColor = getCurrentTextColor();

        // Vẽ viền (stroke)
        TextPaint paint = getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        // Vẽ chữ bên trong trong suốt (màu trong suốt)
        paint.setStyle(Paint.Style.FILL);
        setTextColor(Color.TRANSPARENT);
        super.onDraw(canvas);

        // Reset lại màu chữ gốc (phòng trường hợp)
        setTextColor(currentTextColor);
    }
}
