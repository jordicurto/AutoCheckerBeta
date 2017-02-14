package com.github.jordicurto.autochecker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.adapter.AutoCheckerWeekRecordsAdapter;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.interfaces.OnListFragmentInteractionListener;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.Duration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AutoCheckerWeekRecordsFragment extends Fragment {

    public static final String ARG_LOC_ID = "loc_id";
    public static final String ARG_START_DATE = "start_date";
    public static final String ARG_START_DAY_HOUR = "start_day_hour";
    public static final String ARG_SHOW_WEEKENDS = "show_weekends";

    private WatchedLocation location;
    private List<Date> weekDays;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AutoCheckerWeekRecordsFragment() {
    }


    public static AutoCheckerWeekRecordsFragment newInstance(int locationId, Date startDate,
                                                             int startDayHour,
                                                             boolean showWeekends) {
        AutoCheckerWeekRecordsFragment fragment = new AutoCheckerWeekRecordsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LOC_ID, locationId);
        args.putLong(ARG_START_DATE, startDate.getTime());
        args.putInt(ARG_START_DAY_HOUR, startDayHour);
        args.putBoolean(ARG_SHOW_WEEKENDS, showWeekends);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle args = getArguments();
            Date startDate = new Date(args.getLong(ARG_START_DATE));
            Date endDate = DateUtils.getEndDate(startDate, args.getBoolean(ARG_SHOW_WEEKENDS));
            location = AutoCheckerBusinessManager.getManager
                    (getContext()).getWatchedLocation(args.getInt(ARG_LOC_ID));
            weekDays = DateUtils.getDateIntervals(startDate, endDate,
                    args.getInt(ARG_START_DAY_HOUR), DateUtils.DAY_INTERVAL_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_checker_records, container, false);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recordsView);

        List<AutoCheckerWeekDayRecords> rows = new ArrayList<>();
        Duration totalDuration = new Duration();

        for (int i = 0; i < weekDays.size() - 1; i++) {

            List<WatchedLocationRecord> records = AutoCheckerBusinessManager.getManager(getContext()).
                    getIntervalWatchedLocationRecord(location, weekDays.get(i), weekDays.get(i + 1));

            if (weekDays.get(i).before(DateUtils.getCurrentDate())) {
                AutoCheckerWeekDayRecords row = new AutoCheckerWeekDayRecords(weekDays.get(i), records);
                totalDuration.add(row.getDuration());
                rows.add(row);
            }
        }

        TextView totalDurationText = (TextView) view.findViewById(R.id.total_duration);
        totalDurationText.setText(totalDuration.toString());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new AutoCheckerWeekRecordsAdapter(rows));

        return view;
    }
}
