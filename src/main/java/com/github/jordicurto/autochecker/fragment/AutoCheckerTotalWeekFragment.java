package com.github.jordicurto.autochecker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jordicurto.autochecker.R;

/**
 *
 */
public class AutoCheckerTotalWeekFragment extends Fragment {

    private TextView mTotalDurationText;
    private SparseArray<String> mDurationTexts = new SparseArray<>();

    public AutoCheckerTotalWeekFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_checker_total_week, container, false);
        mTotalDurationText = (TextView) view.findViewById(R.id.total_duration);
        return view;
    }

    public void updateTotalDuration(String durationText, int pos) {
        mDurationTexts.put(pos, durationText);
    }

    public void showDurationText(int selectedTabPosition) {
        mTotalDurationText.setText(mDurationTexts.get(selectedTabPosition));
    }
}
