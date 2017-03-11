package com.github.jordicurto.autochecker.adapter;

import android.view.View;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jordi on 13/02/17.
 */
public class AutoCheckerWeekDayCharts {

    private static final int GRAPH_DURATION_HOURS = 8 * DateUtils.HOURS_PER_MILLISECOND;
    private static final float MAX_GRAPH_VALUE =
            GRAPH_DURATION_HOURS / DateUtils.MINS_PER_MILLISECOND;

    private LocalDateTime graphStart;

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

    public AutoCheckerWeekDayCharts (View view) {

        colors[0] = view.getContext().getResources().getColor(R.color.colorOutLocation);
        colors[1] = view.getContext().getResources().getColor(R.color.colorInLocation);

        charts.put(PART_OF_DAY.MORNING, (HorizontalBarChart) view.findViewById(R.id.graphMorning));
        charts.put(PART_OF_DAY.EVENING, (HorizontalBarChart) view.findViewById(R.id.graphEvening));
        charts.put(PART_OF_DAY.NIGHT, (HorizontalBarChart) view.findViewById(R.id.graphNight));

        configureCharts();
    }

    public void configureStartGraph(LocalDate weekDay, LocalTime startTime) {
        graphStart = new LocalDateTime(weekDay.getYear(), weekDay.getMonthOfYear(),
                weekDay.getDayOfMonth(), startTime.getHourOfDay(), startTime.getMinuteOfHour());
    }

    private LocalDateTime toLocalTime(float value, PART_OF_DAY dayPart) {
        return graphStart.plus(Duration.millis(
                (Math.round(value) * DateUtils.MINS_PER_MILLISECOND) +
                (GRAPH_DURATION_HOURS * dayPart.getIndex())));
    }

    private float toValue(LocalDateTime time) {
        return ((time.toDateTime().getMillis() - graphStart.toDateTime().getMillis()) *
                MAX_GRAPH_VALUE / GRAPH_DURATION_HOURS) % MAX_GRAPH_VALUE;
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
                    return DateUtils.hourFormat.print(toLocalTime(value, dayPart));
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

        LocalDateTime downLimit, upLimit, checkIn, checkOut;
        long checkInMs, checkOutMs, downLimitMs, upLimitMs;
        int index = 0;

        for (PART_OF_DAY dayPart : PART_OF_DAY.values()) {

            List<Float> steps = new ArrayList<>();
            float prevStep = 0;

            downLimit = graphStart.plus(Duration.millis(GRAPH_DURATION_HOURS * dayPart.getIndex()));
            upLimit = downLimit.plus(Duration.millis(GRAPH_DURATION_HOURS));

            if (!records.isEmpty() && index < records.size()) {

                do {

                    WatchedLocationRecord record = records.get(index++);
                    checkIn = record.getCheckIn();
                    checkOut = (record.isActive()) ?
                            DateUtils.getCurrentDate() : record.getCheckOut();

                    if (checkIn.isBefore(downLimit))
                        steps.add(0f);
                    else if (checkIn.isBefore(upLimit))
                        steps.add(toValue(checkIn) - prevStep);

                    if (!steps.isEmpty())
                        prevStep += steps.get(steps.size() - 1);

                    if (checkOut.isAfter(upLimit)) {
                        steps.add(MAX_GRAPH_VALUE);
                        index--;
                    } else
                        steps.add(toValue(checkOut) - prevStep);

                    prevStep += steps.get(steps.size() - 1);

                } while (index < records.size() && checkOut.isBefore(upLimit));

            }

            if (!steps.isEmpty() && steps.get(steps.size() - 1) < MAX_GRAPH_VALUE)
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
