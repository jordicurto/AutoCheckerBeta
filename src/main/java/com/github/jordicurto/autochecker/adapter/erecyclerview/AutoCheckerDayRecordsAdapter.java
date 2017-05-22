package com.github.jordicurto.autochecker.adapter.erecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.github.jordicurto.autochecker.R;

import java.util.List;

/**
 * Created by jordi on 16/05/17.
 */

public class AutoCheckerDayRecordsAdapter extends ExpandableRecyclerAdapter
        <AutoCheckerDayRecords, AutoCheckerDayPartRecords,
                AutoCheckerDayRecordsViewHolder, AutoCheckerDayPartRecordsViewHolder> {

    private LayoutInflater mInflater;

    /**
     * Primary constructor. Sets up {@link #mParentList} and {@link #mFlatItemList}.
     * <p>
     * Any changes to {@link #mParentList} should be made on the original instance, and notified via
     * {@link #notifyParentInserted(int)}
     * {@link #notifyParentRemoved(int)}
     * {@link #notifyParentChanged(int)}
     * {@link #notifyParentRangeInserted(int, int)}
     * {@link #notifyChildInserted(int, int)}
     * {@link #notifyChildRemoved(int, int)}
     * {@link #notifyChildChanged(int, int)}
     * methods and not the notify methods of RecyclerView.Adapter.
     *
     * @param parentList List of all parents to be displayed in the RecyclerView that this
     *                   adapter is linked to
     */
    public AutoCheckerDayRecordsAdapter(Context context,
                                        @NonNull List<AutoCheckerDayRecords> parentList) {
        super(parentList);
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AutoCheckerDayRecordsViewHolder onCreateParentViewHolder(
            @NonNull ViewGroup parentViewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.parent_layout, parentViewGroup, false);
        return new AutoCheckerDayRecordsViewHolder(view);
    }

    @NonNull
    @Override
    public AutoCheckerDayPartRecordsViewHolder onCreateChildViewHolder(
            @NonNull ViewGroup childViewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.child_layout, childViewGroup, false);

        return new AutoCheckerDayPartRecordsViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(@NonNull AutoCheckerDayRecordsViewHolder parentViewHolder,
                                       int parentPosition, @NonNull AutoCheckerDayRecords parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull AutoCheckerDayPartRecordsViewHolder childViewHolder,
                                      int parentPosition, int childPosition,
                                      @NonNull AutoCheckerDayPartRecords child) {
        childViewHolder.bind(child);
    }
}
