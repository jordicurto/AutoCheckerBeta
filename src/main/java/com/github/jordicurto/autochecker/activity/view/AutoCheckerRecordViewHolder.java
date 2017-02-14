package com.github.jordicurto.autochecker.activity.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.Duration;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * TODO: document your custom view class.
 */
public class AutoCheckerRecordViewHolder extends RecyclerView.ViewHolder {

    private static final int GRAPH_DURATION_HOURS = 8 * Duration.HOURS_PER_MILLISECOND;

    private static final float MAX_GRAPH_VALUE = GRAPH_DURATION_HOURS / Duration.MINS_PER_MILLISECOND;

    private Context context = null;
    private long startDay = 0;
    private long graphStart = 0;

    private enum PART_OF_DAY {
        MORNING(0),
        EVENING(1),
        NIGHT(2);

        private int index;

        PART_OF_DAY(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private HashMap<PART_OF_DAY, HorizontalBarChart> charts = new HashMap<>();

    private int[] colors = new int[2];

    public AutoCheckerRecordViewHolder(View itemView, Date startDate) {
        super(itemView);
        context = itemView.getContext();
        startDay = startDate.getTime() / Duration.DAYS_PER_MILLISECOND;
        graphStart = startDate.getTime() % Duration.DAYS_PER_MILLISECOND;
        colors[0] = context.getResources().getColor(R.color.colorOutLocation);
        colors[1] = context.getResources().getColor(R.color.colorInLocation);
        charts.put(PART_OF_DAY.MORNING, (HorizontalBarChart) itemView.findViewById(R.id.graphMorning));
        charts.put(PART_OF_DAY.EVENING, (HorizontalBarChart) itemView.findViewById(R.id.graphEvening));
        charts.put(PART_OF_DAY.NIGHT, (HorizontalBarChart) itemView.findViewById(R.id.graphNight));
        configureCharts();
    }

    private Calendar toDate(float value, PART_OF_DAY dayPart) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(graphStart +
                (Math.round(value) * Duration.MINS_PER_MILLISECOND) + (GRAPH_DURATION_HOURS * dayPart.getIndex()));
        return calendar;
    }

    private float toValue(long milliseconds) {
        return ((milliseconds - graphStart) * MAX_GRAPH_VALUE / GRAPH_DURATION_HOURS) % MAX_GRAPH_VALUE;
    }

    private void configureCharts() {

        for (final PART_OF_DAY dayPart : PART_OF_DAY.values()) {
            HorizontalBarChart chart = charts.get(dayPart);
            chart.setPinchZoom(false);
            chart.setDoubleTapToZoomEnabled(false);
            chart.getAxisLeft().setEnabled(true);
            chart.getAxisLeft().setAxisMinValue(0f);
            chart.getAxisLeft().setAxisMaxValue(MAX_GRAPH_VALUE);
            chart.getAxisLeft().setLabelCount(9, true);
            chart.getAxisLeft().setDrawAxisLine(true);
            chart.getAxisLeft().setDrawGridLines(false);
            chart.getAxisLeft().setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, YAxis yAxis) {
                    return String.valueOf(toDate(value, dayPart).get(Calendar.HOUR_OF_DAY));
                }
            });
            chart.getAxisRight().setEnabled(true);
            chart.getAxisRight().setDrawAxisLine(true);
            chart.getAxisRight().setDrawLabels(false);
            chart.getAxisRight().setDrawGridLines(false);
            chart.getLegend().setEnabled(false);
            chart.getXAxis().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setDragEnabled(false);
            chart.setScaleEnabled(false);
        }
    }

    public void setRecords(List<WatchedLocationRecord> records) {

        long checkInMs, checkOutMs, downLimitMs, upLimitMs;
        int index = 0;

        for (PART_OF_DAY dayPart : PART_OF_DAY.values()) {

            List<Float> steps = new ArrayList<>();
            float prevStep = 0;

            downLimitMs = graphStart + (GRAPH_DURATION_HOURS * dayPart.getIndex());
            upLimitMs = graphStart + (GRAPH_DURATION_HOURS * (dayPart.getIndex() + 1));

            if (!records.isEmpty() && index < records.size()) {

                do {

                    WatchedLocationRecord record = records.get(index++);
                    long checkIn = record.getCheckIn().getTime();
                    long checkOut = record.getCheckOut().getTime();
                    checkInMs = (checkIn % Duration.DAYS_PER_MILLISECOND) +
                            ((checkIn / Duration.DAYS_PER_MILLISECOND) - startDay) * Duration.DAYS_PER_MILLISECOND;
                    checkOutMs = (checkOut % Duration.DAYS_PER_MILLISECOND) +
                            ((checkOut / Duration.DAYS_PER_MILLISECOND) - startDay) * Duration.DAYS_PER_MILLISECOND;

                    if (checkInMs < downLimitMs)
                        steps.add(0f);
                    else if (checkInMs < upLimitMs)
                        steps.add(toValue(checkInMs) - prevStep);

                    prevStep += steps.get(steps.size() - 1);

                    if (checkOutMs > upLimitMs) {
                        steps.add(MAX_GRAPH_VALUE);
                        index--;
                    } else
                        steps.add(toValue(checkOutMs) - prevStep);

                    prevStep += steps.get(steps.size() - 1);

                } while (index < records.size() && checkOutMs <= upLimitMs);

            }

            if (steps.get(steps.size() - 1) < MAX_GRAPH_VALUE)
                steps.add(MAX_GRAPH_VALUE);

            float[] stepArray = new float[steps.size()];

            int i = 0;
            for (Float step : steps) {
                stepArray[i++] = step;
            }

            List<BarEntry> yValues = new ArrayList<>();
            yValues.add(new BarEntry(stepArray, 0, dayPart.name()));
            BarDataSet dataSet = new BarDataSet(yValues, dayPart.name());
            dataSet.setColors(colors);
            dataSet.setDrawValues(false);
            BarData data = new BarData(new String[]{dayPart.name()}, dataSet);
            data.setDrawValues(false);
            data.setHighlightEnabled(false);

            charts.get(dayPart).setData(data);
            charts.get(dayPart).notifyDataSetChanged();
            charts.get(dayPart).invalidate();
        }
    }
}
