package com.github.jordicurto.autochecker.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.Duration;
import org.joda.time.Period;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerPreference;

/**
 *
 */
public class AutoCheckerTimeDurationPickerPreference extends TimeDurationPickerPreference {

    private static final String PLACEHOLDER_HOURS_MINUTES = "${h:mm}";

    public AutoCheckerTimeDurationPickerPreference(Context context) {
        super(context);
    }

    public AutoCheckerTimeDurationPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        getTimeDurationPicker().setTimeUnits(TimeDurationPicker.HH_MM);
    }

    private void updateDescription() {
        Period period = new Duration(getDuration()).toPeriod();
        final String summary = getSummary().toString()
                .replace(PLACEHOLDER_HOURS_MINUTES, DateUtils.durationFormat.print(period));
        setSummary(summary);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        updateDescription();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        updateDescription();
    }
}
