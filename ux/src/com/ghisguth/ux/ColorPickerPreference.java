package com.ghisguth.ux;

import android.content.Context;
import android.graphics.*;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class ColorPickerPreference extends DialogPreference {
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public ColorPickerPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        ctx_ = ctx;
        color_ = getPersistedInt(Color.WHITE);
    }

    @Override
    protected View onCreateDialogView() {
        super.onCreateDialogView();
        LinearLayout layout = new LinearLayout(ctx_);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.addView(new ColorPickerView(ctx_, new ColorPicked(), color_));
        return layout;
    }

    @Override
    protected void onDialogClosed(boolean positive_result) {
        super.onDialogClosed(positive_result);
        if (positive_result) {
            persistInt(color_);
        }
    }

    private class ColorPicked implements OnColorChangedListener {
        public void colorChanged(int color) {
            color_ = color;
        }
    }

    private class ColorPickerView extends View {
        private Paint mPaint;
        private Paint mCenterPaint;
        private final int[] mColors;
        private OnColorChangedListener mListener;

        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            mListener = l;
            mColors = new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00,
                    0xFF00FFFF, 0xFFFFFFFF, 0xFFFFFF00, 0xFFFF00FF, 0xFFFF0000,
                    0xFF000000};
            Shader s = new SweepGradient(0, 0, mColors, null);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32);

            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(5);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;

            canvas.translate(CENTER_X, CENTER_X);

            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
            canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
        }

        private static final int CENTER_X = 100;
        private static final int CENTER_Y = 100;
        private static final int CENTER_RADIUS = 32;

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int) p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float angle = (float) java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle / (2 * PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    mCenterPaint.setColor(interpColor(mColors, unit));
                    mListener.colorChanged(mCenterPaint.getColor());
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private Context ctx_;
    private int color_;
}
