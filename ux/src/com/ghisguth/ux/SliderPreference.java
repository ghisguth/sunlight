package com.ghisguth.ux;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {
	private SeekBar seekBar_;
	private TextView valueText_;
	private int value_ = 127;

	public SliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

        int resourceId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android","defaultValue", 0);
        if(resourceId > 0)
        {
            value_ = context.getResources().getInteger(resourceId);
        }

        this.setPersistent(true);
		this.setDialogLayoutResource(R.layout.slider_preference);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		this.seekBar_ = (SeekBar) view.findViewById(R.id.data);
		this.seekBar_.setProgress(value_);
		this.seekBar_.setOnSeekBarChangeListener(this);

        TextView title = (TextView) view.findViewById(R.id.data_value);
        title.setText("");

		this.valueText_ = (TextView) view.findViewById(R.id.data_value);
		this.valueText_.setText(Integer.toString(value_));
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);

        final int defaultInteger = defaultValue == null ? 127 : (Integer) defaultValue;
		final int value = restore ? this.getPersistedInt(defaultInteger) : defaultInteger;

        this.value_ = value;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		value_ = value;
		valueText_.setText(String.valueOf(value));
		callChangeListener(new Integer(value));
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (this.callChangeListener(value_)) {
				this.persistInt(value_);
			}
		}
	}

	public void onStartTrackingTouch(SeekBar seek) {
	}

	public void onStopTrackingTouch(SeekBar seek) {
	}
}
