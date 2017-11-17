package com.github.jordicurto.autochecker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.adapter.AutoCheckerDayRecords;
import com.github.jordicurto.autochecker.adapter.AutoCheckerDayRecordsAdapterLine;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.DividerItemDecoration;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


public class AutoCheckerWeekRecordsFragment extends Fragment {

    public static final String ARG_LOC_ID = "loc_id";
    public static final String ARG_START_DATE = "start_date";
    public static final String ARG_START_DAY_HOUR = "start_day_hour";
    public static final String ARG_SHOW_WEEKENDS = "show_weekends";

    private WatchedLocation location;
    private List<Interval> weekDays;
    private int startHourDay;

    private RecyclerView recyclerView;
    private TextView totalDurationText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AutoCheckerWeekRecordsFragment() {
    }


    public static AutoCheckerWeekRecordsFragment newInstance(int locationId,
                                                             LocalDate startDate,
                                                             int startDayHour,
                                                             boolean showWeekends) {
        AutoCheckerWeekRecordsFragment fragment = new AutoCheckerWeekRecordsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LOC_ID, locationId);
        args.putLong(ARG_START_DATE, startDate.toDateTimeAtStartOfDay().getMillis());
        args.putInt(ARG_START_DAY_HOUR, startDayHour);
        args.putBoolean(ARG_SHOW_WEEKENDS, showWeekends);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            LocalDate startDate = new LocalDate(args.getLong(ARG_START_DATE));
            LocalDate endDate = DateUtils.getEndDate(startDate, args.getBoolean(ARG_SHOW_WEEKENDS));
            location = AutoCheckerBusinessManager.getManager
                    (getContext()).getWatchedLocation(args.getInt(ARG_LOC_ID));
            weekDays = DateUtils.getDateIntervals(startDate, endDate,
                    DateUtils.INTERVAL_TYPE.DAYS);
            startHourDay = args.getInt(ARG_START_DAY_HOUR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_checker_records, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recordsView);
        totalDurationText = (TextView) view.findViewById(R.id.total_duration);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //List<AutoCheckerWeekDayRecords> rows = new ArrayList<>();
        List<AutoCheckerDayRecords> rows = new ArrayList<>();
        Duration totalDuration = Duration.ZERO;

        AutoCheckerBusinessManager manager = AutoCheckerBusinessManager.getManager(getContext());

        for (int i = 0; i < weekDays.size(); i++) {

            List<WatchedLocationRecord> records = manager.
                    getIntervalWatchedLocationRecord(location, weekDays.get(i), startHourDay);

            if (weekDays.get(i).getStart().plusHours(startHourDay).toLocalDateTime()
                    .isBefore(DateUtils.getCurrentDate())) {

                AutoCheckerDayRecords row =
                        new AutoCheckerDayRecords(weekDays.get(i), startHourDay, records);
                totalDuration = totalDuration.plus(row.getDuration());
                rows.add(row);
            }
        }
        
        totalDurationText.setText(DateUtils.getDurationString(totalDuration));

        //recyclerView.setAdapter(new AutoCheckerDayRecordsAdapter(getContext(), rows));
        recyclerView.setAdapter(new AutoCheckerDayRecordsAdapterLine(getContext(), rows));
    }

}
