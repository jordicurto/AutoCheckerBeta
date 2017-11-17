package com.github.jordicurto.autochecker.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.github.jordicurto.autochecker.R;

/**
 * Created by jordi on 16/05/17.
 */

public class AutoCheckerDayRecordsViewHolder extends ParentViewHolder {

    private final TextView mDayText;
    private final TextView mDurationText;
    private final ImageView mImageExpand;
    private final Animation mExpandAnim;
    private final Animation mCollapseAnim;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public AutoCheckerDayRecordsViewHolder(@NonNull View itemView) {
        super(itemView);
        mDayText = (TextView) itemView.findViewById(R.id.week_day);
        mDurationText = (TextView) itemView.findViewById(R.id.day_record_duration);
        mImageExpand = (ImageView) itemView.findViewById(R.id.expand_collapse);
        mExpandAnim = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.anim_expand);
        mCollapseAnim = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.anim_collapse);
    }

    public void bind(AutoCheckerDayRecords dayRecords) {
        mDayText.setText(dayRecords.getWeekDayString());
        mDurationText.setText(dayRecords.getDurationString());
        if(dayRecords.getRecords().isEmpty()) {
            mImageExpand.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        if(mImageExpand.getVisibility() == View.VISIBLE) {
            if (expanded)
                mImageExpand.startAnimation(mCollapseAnim);
            else
                mImageExpand.startAnimation(mExpandAnim);
        }
    }
}
