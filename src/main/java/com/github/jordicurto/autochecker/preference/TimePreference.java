package com.github.jordicurto.autochecker.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.LocalTime;

/**
 * Created by jordi on 27/02/18.
 */
public class TimePreference extends DialogPreference {

    private static final String PLACEHOLDER_HOURS_MINUTES = "${h:mm}";

    private LocalTime mTime = new LocalTime();
    private TimePicker mPicker = null;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPositiveButtonText(R.string.set);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        mPicker = new TimePicker(getContext());
        return mPicker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        mPicker.setCurrentHour(mTime.getHourOfDay());
        mPicker.setCurrentMinute(mTime.getMinuteOfHour());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            mTime = new LocalTime().withHourOfDay(mPicker.getCurrentHour()).
                    withMinuteOfHour(mPicker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(mTime.getMillisOfDay())) {
                persistInt(mTime.getMillisOfDay());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                mTime = new LocalTime().withMillisOfDay(getPersistedInt(0));
            } else {
                mTime = new LocalTime().withMillisOfDay(
                        Integer.parseInt(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                mTime = new LocalTime().withMillisOfDay(0);
            } else {
                mTime = new LocalTime().withMillisOfDay(Integer.parseInt((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if (mTime != null) {
            summary = summary.toString().replace(PLACEHOLDER_HOURS_MINUTES,
                    DateUtils.timeFormat.print(mTime.getMillisOfDay()));
        }
        return summary;
    }
}
