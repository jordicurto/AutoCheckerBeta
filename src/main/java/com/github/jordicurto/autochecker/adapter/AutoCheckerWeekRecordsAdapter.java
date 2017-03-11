package com.github.jordicurto.autochecker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.fragment.AutoCheckerWeekDayRecords;

import org.joda.time.LocalTime;

import java.util.List;
import java.util.Vector;

public class AutoCheckerWeekRecordsAdapter extends
        RecyclerView.Adapter<AutoCheckerWeekRecordsAdapter.ViewHolder> {

    private List<AutoCheckerWeekDayRecords> mRecords;
    private int mStartDayHour;
    private Vector<AutoCheckerWeekRecordsAdapter.ViewHolder> mViewHolders;

    private int mExpandedIndex = -1;

    public AutoCheckerWeekRecordsAdapter(List<AutoCheckerWeekDayRecords> records,
                                         int startDayHour) {
        mRecords = records;
        mStartDayHour = startDayHour;
        mViewHolders = new Vector<>(mRecords.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_auto_checker_day_records, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        mViewHolders.add(position, holder);
        holder.mCharts.configureStartGraph(
                mRecords.get(position).getWeekDay(), new LocalTime(mStartDayHour, 0));
        holder.updateRecordDuration(position);
        holder.setIsRecyclable(false);
        holder.mExpandableLinearLayout.setInRecyclerView(true);
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mExpandedIndex == -1) {
                    expandViewHolder(holder);
                    mExpandedIndex = holder.getAdapterPosition();
                } else {
                    if (mExpandedIndex != holder.getAdapterPosition()) {
                        collapseViewHolder(mViewHolders.get(mExpandedIndex));
                        expandViewHolder(holder);
                        mExpandedIndex = holder.getAdapterPosition();
                    } else {
                        collapseViewHolder(holder);
                        mExpandedIndex = -1;
                    }
                }
            }

            private void expandViewHolder(ViewHolder holder) {
                holder.mImageSwitcher.setImageResource(R.drawable.ic_collapse);
                holder.mExpandableLinearLayout.expand();
            }

            private void collapseViewHolder(ViewHolder holder) {
                holder.mImageSwitcher.setImageResource(R.drawable.ic_expand);
                holder.mExpandableLinearLayout.collapse();
            }
        });
        holder.mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(holder.getView().getContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new
                        ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.WRAP_CONTENT,
                        ImageSwitcher.LayoutParams.WRAP_CONTENT));
                myView.setImageResource(R.drawable.ic_expand);
                return myView;
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
        private final ImageSwitcher mImageSwitcher;
        private final ExpandableLinearLayout mExpandableLinearLayout;
        private final AutoCheckerWeekDayCharts mCharts;

        public ViewHolder(final View view) {
            super(view);
            mView = view;
            mDayText = (TextView) view.findViewById(R.id.week_day);
            mDurationText = (TextView) view.findViewById(R.id.day_record_duration);
            mImageSwitcher = (ImageSwitcher) view.findViewById(R.id.expand_collapse_icon);
            mExpandableLinearLayout =
                   (ExpandableLinearLayout) view.findViewById(R.id.expand_usage_graphs);
            mExpandableLinearLayout.initLayout();
            mCharts = new AutoCheckerWeekDayCharts(view);
        }

        public void updateRecordDuration(int position) {
            AutoCheckerWeekDayRecords records = mRecords.get(position);
            mDayText.setText(records.getWeekDayString());
            mDurationText.setText(records.getDurationString());
            mCharts.setRecords(records.getRecords());
        }

        public View getView() {
            return mView;
        }
    }
}
