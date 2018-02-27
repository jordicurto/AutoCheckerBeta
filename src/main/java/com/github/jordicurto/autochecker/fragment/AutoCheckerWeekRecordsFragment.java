package com.github.jordicurto.autochecker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.adapter.AutoCheckerDayRecords;
import com.github.jordicurto.autochecker.adapter.AutoCheckerDayRecordsAdapterLine;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerPreferencesManager;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.github.jordicurto.autochecker.util.DividerItemDecoration;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


public class AutoCheckerWeekRecordsFragment extends Fragment {

    public static final String ARG_START_DATE = "start_date";

    private WatchedLocation location;
    private List<Interval> weekDays;

    private RecyclerView recyclerView;
    private OnTotalDurationUpdateListener mListener;

    private AutoCheckerPreferencesManager preferencesManager = new AutoCheckerPreferencesManager();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AutoCheckerWeekRecordsFragment() {
    }

    public static AutoCheckerWeekRecordsFragment newInstance(LocalDate startDate) {
        AutoCheckerWeekRecordsFragment fragment = new AutoCheckerWeekRecordsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_START_DATE, startDate.toDateTimeAtStartOfDay().getMillis());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager.updatePreferences(getContext());

        Bundle args = getArguments();
        if (args != null) {
            LocalDate startDate = new LocalDate(args.getLong(ARG_START_DATE));
            LocalDate endDate = DateUtils.getEndDate(startDate, preferencesManager.isShowWeekends());
            location = AutoCheckerBusinessManager.getManager
                    (getContext()).getWatchedLocation(preferencesManager.getCurrentLocationName());
            weekDays = DateUtils.getDateIntervals(startDate, endDate,
                    DateUtils.INTERVAL_TYPE.DAYS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_checker_records, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recordsView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        return view;
    }

    private DateTime getStartWeekTime() {
        return weekDays.get(0).getStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        int startHourDay = preferencesManager.getStartDayHour();

        List<AutoCheckerDayRecords> rows = new ArrayList<>();
        Duration totalDuration = Duration.ZERO;

        AutoCheckerBusinessManager manager = AutoCheckerBusinessManager.getManager(getContext());

        for (int i = 0; i < weekDays.size(); i++) {

            List<WatchedLocationRecord> records = manager.
                    getIntervalWatchedLocationRecord(location, weekDays.get(i),
                            startHourDay);

            if (weekDays.get(i).getStart().plusHours(startHourDay).toLocalDateTime()
                    .isBefore(DateUtils.getCurrentDate())) {

                AutoCheckerDayRecords row =
                        new AutoCheckerDayRecords(weekDays.get(i), records, preferencesManager);
                totalDuration = totalDuration.plus(row.getDuration());
                rows.add(row);
            }
        }

        mListener.updateTotalDuration(DateUtils.getDurationString(totalDuration,
                preferencesManager.isRelativeDurations(),
                preferencesManager.getDuration(DateUtils.INTERVAL_TYPE.WEEKS)),
                getStartWeekTime());

        recyclerView.setAdapter(new AutoCheckerDayRecordsAdapterLine(getContext(), rows));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTotalDurationUpdateListener) {
            mListener = (OnTotalDurationUpdateListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnTotalDurationUpdateListener {
        void updateTotalDuration(String durationText, DateTime start);
    }

}
