package com.ghisguth.ux;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ColorPickerPreferenceFragmentCompat extends PreferenceDialogFragmentCompat {

    private int color_;

    public static ColorPickerPreferenceFragmentCompat newInstance(String key) {
        final ColorPickerPreferenceFragmentCompat fragment =
                new ColorPickerPreferenceFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        ColorPickerPreference preference = (ColorPickerPreference) getPreference();
        if (preference != null) {
            color_ = preference.getColor();
        } else {
            color_ = Color.WHITE;
        }

        LinearLayout layout = new LinearLayout(context);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.addView(new ColorPickerView(context, new ColorPicked(), color_));
        return layout;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            ColorPickerPreference preference = (ColorPickerPreference) getPreference();
            if (preference != null) {
                preference.setColor(color_);
            }
        }
    }

    private interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private class ColorPicked implements OnColorChangedListener {
        public void colorChanged(int color) {
            color_ = color;
        }
    }

    private class ColorPickerView extends View {
        private static final int CENTER_X = 100;
        private static final int CENTER_Y = 100;
        private static final int CENTER_RADIUS = 32;
        private static final float PI = 3.1415926f;
        private final int[] colors_;
        private Paint paint_;
        private Paint centerPaint_;
        private OnColorChangedListener listener_;

        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            listener_ = l;
            colors_ =
                    new int[] {
                        0xFF000000,
                        0xFF0000FF,
                        0xFF00FF00,
                        0xFF00FFFF,
                        0xFFFFFFFF,
                        0xFFFFFF00,
                        0xFFFF00FF,
                        0xFFFF0000,
                        0xFF000000
                    };
            Shader s = new SweepGradient(0, 0, colors_, null);

            paint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint_.setShader(s);
            paint_.setStyle(Paint.Style.STROKE);
            paint_.setStrokeWidth(32);

            centerPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
            centerPaint_.setColor(color);
            centerPaint_.setStrokeWidth(5);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - paint_.getStrokeWidth() * 0.5f;

            canvas.translate(CENTER_X, CENTER_X);

            canvas.drawOval(new RectF(-r, -r, r, r), paint_);
            canvas.drawCircle(0, 0, CENTER_RADIUS, centerPaint_);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
        }

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

            int c0 = colors[i];
            int c1 = colors[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float angle = (float) java.lang.Math.atan2(y, x);
                    float unit = angle / (2 * PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    centerPaint_.setColor(interpColor(colors_, unit));
                    listener_.colorChanged(centerPaint_.getColor());
                    invalidate();
                    break;
            }
            return true;
        }
    }
}
