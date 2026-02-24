package com.ghisguth.ux;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;

public class ColorPickerPreference extends DialogPreference {

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getDialogLayoutResource() {
        return 0;
    }

    public int getColor() {
        return getPersistedInt(Color.WHITE);
    }

    public void setColor(int color) {
        if (callChangeListener(color)) {
            persistInt(color);
        }
    }
}
