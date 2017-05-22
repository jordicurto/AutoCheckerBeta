package com.github.jordicurto.autochecker.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.fragment.AutoCheckerWeekDayRecords;

import org.joda.time.LocalTime;

import java.util.List;

public class AutoCheckerWeekRecordsAdapter extends
        RecyclerView.Adapter<AutoCheckerWeekRecordsAdapter.ViewHolder> {

    private List<AutoCheckerWeekDayRecords> mRecords;
    private int mStartDayHour;
    private SparseBooleanArray mExpandState = new SparseBooleanArray();

    public AutoCheckerWeekRecordsAdapter(List<AutoCheckerWeekDayRecords> records,
                                         int startDayHour) {
        mRecords = records;
        mStartDayHour = startDayHour;
        for (int i = 0; i < mRecords.size(); i++)
            mExpandState.append(i, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_auto_checker_day_records, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        AutoCheckerWeekDayRecords records = mRecords.get(position);
        holder.mCharts.configureStartGraph(
                records.getWeekDay(), new LocalTime(mStartDayHour, 0));
        holder.updateRecordDuration(records);
        holder.setIsRecyclable(false);
        holder.mExpandableLinearLayout.setInRecyclerView(true);
        holder.mExpandableLinearLayout.setExpanded(mExpandState.get(position));
        holder.mImageExpand.setRotation(mExpandState.get(position) ? 180f : 0f);
        holder.mExpandableLinearLayout.setListener(new ExpandableLayoutListenerAdapter() {

            @Override
            public void onPreOpen() {
                mExpandState.put(holder.getAdapterPosition(), true);
                holder.expandIcon();
            }

            @Override
            public void onPreClose() {
                mExpandState.put(holder.getAdapterPosition(), false);
                holder.collapseIcon();
            }
        });
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mExpandableLinearLayout.getVisibility() == View.VISIBLE) {
                    holder.mExpandableLinearLayout.toggle();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mDayText;
        private final TextView mDurationText;
        private final ImageView mImageExpand;
        private final ExpandableLinearLayout mExpandableLinearLayout;
        private final AutoCheckerWeekDayCharts mCharts;
        private final Animation expandAnim, collapseAnim;

        public ViewHolder(final View view) {
            super(view);
            mView = view;
            mDayText = (TextView) view.findViewById(R.id.week_day);
            mDurationText = (TextView) view.findViewById(R.id.day_record_duration);
            mImageExpand = (ImageView) view.findViewById(R.id.expand_collapse);
            mExpandableLinearLayout =
                    (ExpandableLinearLayout) view.findViewById(R.id.expand_usage_graphs);
            mExpandableLinearLayout.initLayout();
            mCharts = new AutoCheckerWeekDayCharts(view);
            expandAnim = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_expand);
            collapseAnim = AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_collapse);
        }

        public void updateRecordDuration(AutoCheckerWeekDayRecords records) {
            mDayText.setText(records.getWeekDayString());
            mDurationText.setText(records.getDurationString());
            if(records.getRecords().isEmpty()) {
                mExpandableLinearLayout.setVisibility(View.INVISIBLE);
                mImageExpand.setVisibility(View.INVISIBLE);
            } else {
                mExpandableLinearLayout.setVisibility(View.VISIBLE);
                mCharts.setRecords(records.getRecords());
            }
        }

        public void expandIcon() {
            mImageExpand.startAnimation(expandAnim);
        }

        public void collapseIcon() {
            mImageExpand.startAnimation(collapseAnim);
        }

        public View getView() {
            return mView;
        }
    }
}
