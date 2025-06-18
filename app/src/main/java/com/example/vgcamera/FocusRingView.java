package com.example.vgcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FocusRingView extends View {
    private float x = -1;
    private float y = -1;
    private float radius = 80f;
    private Paint paint;
    private boolean visible = false;

    public FocusRingView(Context context) {
        super(context);
        init();
    }

    public FocusRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
    }

    public void showFocusRing(float x, float y) {
        this.x = x;
        this.y = y;
        visible = true;
        invalidate();
    }

    public void hideFocusRing() {
        visible = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (visible && x >= 0 && y >= 0) {
            canvas.drawCircle(x, y, radius, paint);
        }
    }
}

