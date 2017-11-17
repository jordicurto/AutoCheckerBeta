package com.github.jordicurto.autochecker.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordi on 28/05/17.
 */

public class AutoCheckerDayPartRecordsViewHolderLine extends ChildViewHolder {

    private static final int GRAPH_DURATION_HOURS =
            AutoCheckerConstants.HOURS_BETWEEN_DAY_PART * DateUtils.HOURS_PER_MILLISECOND;
    private static final float MAX_GRAPH_VALUE =
            GRAPH_DURATION_HOURS / DateUtils.MINS_PER_MILLISECOND;

    private int colorChart;
    private int colorChartNow;

    private LocalDateTime graphStart;

    private ImageView image;

    private LineChart chart;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public AutoCheckerDayPartRecordsViewHolderLine(@NonNull View itemView) {
        super(itemView);
        chart = (LineChart) itemView.findViewById(R.id.graph);
        image = (ImageView) itemView.findViewById(R.id.img_graph);

        colorChartNow = itemView.getContext().getResources().getColor(R.color.colorAccent);
        colorChart = itemView.getContext().getResources().getColor(R.color.colorPrimary);

        configureChart();
    }

    private void configureChart() {

        Description desc = new Description();
        desc.setText("");
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDescription(desc);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setAxisMinimum(0f);
        chart.getXAxis().setAxisMaximum(MAX_GRAPH_VALUE);
        chart.getXAxis().setLabelCount(9, true);
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setCenterAxisLabels(false);
        chart.getXAxis().setAxisLineWidth(2f);
        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(0.2f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
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

        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateUtils.hourFormat.print(
                        toLocalTime(value, dayPartRecords.getDayPart()));
            }
        });

        switch (dayPartRecords.getDayPart()) {
            case MORNING:
                image.setImageResource(R.drawable.ic_morning);
                break;
            case AFTERNOON:
                image.setImageResource(R.drawable.ic_afternoon);
                break;
            case NIGHT:
                image.setImageResource(R.drawable.ic_night);
                break;
        }
    }

    public void setRecords(List<WatchedLocationRecord> records,
                           final DateUtils.PART_OF_DAY dayPart) {

        final LocalDateTime now = DateUtils.getCurrentDate();
        LocalDateTime downLimit, upLimit, checkIn, checkOut;
        List<ILineDataSet> dataSets = new ArrayList<>();

        downLimit = graphStart.plus(Duration.millis(GRAPH_DURATION_HOURS * dayPart.getIndex()));
        upLimit = downLimit.plus(Duration.millis(GRAPH_DURATION_HOURS));

        for (WatchedLocationRecord record : records) {

            List<Entry> values = new ArrayList<>();

            checkIn = record.getCheckIn();
            checkOut = (record.isActive()) ? now : record.getCheckOut();

            if (checkIn.isBefore(downLimit))
                values.add(new Entry(-1, 0.1f));
            else
                values.add(new Entry(toValue(checkIn), 0.1f));

            if (checkOut.isAfter(upLimit))
                values.add(new Entry(MAX_GRAPH_VALUE + 1, 0.1f));
            else
                values.add(new Entry(toValue(checkOut), 0.1f));

            LineDataSet dataSet = new LineDataSet(values, "");
            dataSet.setCircleRadius(6);
            dataSet.setDrawCircleHole(false);
            dataSet.setLineWidth(5);
            if (record.isActive()) {
                dataSet.setColor(colorChartNow);
                dataSet.setCircleColor(colorChartNow);
            } else {
                dataSet.setColor(colorChart);
                dataSet.setCircleColor(colorChart);
            }
            dataSets.add(dataSet);
        }

        LineData data = new LineData(dataSets);
        data.setDrawValues(true);
        data.setHighlightEnabled(false);
        data.setValueTextSize(11f);
        data.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry,
                                            int dataSetIndex, ViewPortHandler viewPortHandler) {
                return DateUtils.timeFormat.print(toLocalTime(entry.getX(), dayPart));
            }
        });

        chart.setData(data);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}
