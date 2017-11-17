package com.github.jordicurto.autochecker.adapter;

import android.support.annotation.NonNull;
import android.view.View;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordi on 16/05/17.
 */

public class AutoCheckerDayPartRecordsViewHolder extends ChildViewHolder {

    private static final int GRAPH_DURATION_HOURS =
            AutoCheckerConstants.HOURS_BETWEEN_DAY_PART * DateUtils.HOURS_PER_MILLISECOND;
    private static final float MAX_GRAPH_VALUE =
            GRAPH_DURATION_HOURS / DateUtils.MINS_PER_MILLISECOND;

    private int[] colors = new int[2];

    private LocalDateTime graphStart;

    private HorizontalBarChart chart;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public AutoCheckerDayPartRecordsViewHolder(@NonNull View itemView) {
        super(itemView);
        chart = (HorizontalBarChart) itemView.findViewById(R.id.graph);

        colors[0] = itemView.getContext().getResources().getColor(R.color.colorPrimaryLight);
        colors[1] = itemView.getContext().getResources().getColor(R.color.colorPrimary);

        configureChart();
    }

    private void configureChart() {

        Description desc = new Description();
        desc.setText("");
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setAxisMinValue(0f);
        chart.getAxisLeft().setAxisMaxValue(MAX_GRAPH_VALUE);
        chart.getAxisLeft().setLabelCount(9, true);
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setDrawAxisLine(true);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDescription(desc);
    }

    private LocalDateTime toLocalTime(float value, DateUtils.PART_OF_DAY dayPart) {
        return graphStart.plus(Duration.millis(
                (Math.round(value) * DateUtils.MINS_PER_MILLISECOND) +
                        (GRAPH_DURATION_HOURS * dayPart.getIndex())));
    }

    private float toValue(LocalDateTime time) {
        return ((time.toDateTime().getMillis() - graphStart.toDateTime().getMillis()) *
                MAX_GRAPH_VALUE / GRAPH_DURATION_HOURS) % MAX_GRAPH_VALUE;
    }

    public void bind(final AutoCheckerDayPartRecords dayPartRecords) {

        graphStart = dayPartRecords.getWeekDayStart().toLocalDateTime();

        setRecords(dayPartRecords.getRecords(), dayPartRecords.getDayPart());

        chart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateUtils.hourFormat.print(toLocalTime(value, dayPartRecords.getDayPart()));
            }
        });
    }

    public void setRecords(List<WatchedLocationRecord> records, DateUtils.PART_OF_DAY dayPart) {

        final LocalDateTime now = DateUtils.getCurrentDate();
        LocalDateTime downLimit, upLimit, checkIn, checkOut;

        List<Float> steps = new ArrayList<>();
        float prevStep = 0, nextStep;

        downLimit = graphStart.plus(Duration.millis(GRAPH_DURATION_HOURS * dayPart.getIndex()));
        upLimit = downLimit.plus(Duration.millis(GRAPH_DURATION_HOURS));

        for (WatchedLocationRecord record : records) {

            checkIn = record.getCheckIn();
            checkOut = (record.isActive()) ? now : record.getCheckOut();

            if (checkIn.isBefore(downLimit))
                nextStep = 0f;
            else
                nextStep = toValue(checkIn) - prevStep;

            steps.add(nextStep);
            prevStep += nextStep;

            if (checkOut.isAfter(upLimit))
                nextStep = MAX_GRAPH_VALUE - prevStep;
            else
                nextStep = toValue(checkOut) - prevStep;

            steps.add(nextStep);
            prevStep += nextStep;
        }

        if (steps.isEmpty() || steps.get(steps.size() - 1) < MAX_GRAPH_VALUE) {
            if (downLimit.isBefore(now)) {
                if (upLimit.isAfter(now)) {
                    steps.add(toValue(now) - prevStep);
                } else {
                    steps.add(MAX_GRAPH_VALUE - prevStep);
                }
            }
        }

        float[] stepArray = new float[steps.size()];
        //float stepSum = 0f;

        for (int i = 0; i < steps.size(); i++) {
            stepArray[i] = steps.get(i);
/*            stepSum += stepArray[i];
            if (stepSum > 0 && stepSum < MAX_GRAPH_VALUE && i < (steps.size() - 1)) {
                LimitLine lm = new LimitLine(stepSum,
                        //DateUtils.timeFormat.print(toLocalTime(stepSum, dayPart)));
                        "");
                chart.getAxisLeft().addLimitLine(lm);
            }*/
        }

        List<BarEntry> yValues = new ArrayList<>();
        yValues.add(new BarEntry(0, stepArray, dayPart.name()));
        BarDataSet dataSet = new BarDataSet(yValues, dayPart.name());
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        BarData data = new BarData(dataSet);
        data.setDrawValues(false);
        data.setHighlightEnabled(false);

        chart.setData(data);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}
