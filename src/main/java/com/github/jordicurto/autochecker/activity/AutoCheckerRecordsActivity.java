package com.github.jordicurto.autochecker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.AutoCheckerDataSource;
import com.github.jordicurto.autochecker.data.exception.NoLocationFoundException;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.data.model.WatchedLocationRecord;
import com.github.jordicurto.autochecker.fragment.AutoCheckerWeekRecordsFragment;
import com.github.jordicurto.autochecker.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AutoCheckerRecordsActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private AutoCheckerLocationRecordPageAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private AutoCheckerDataSource dataSource;

    private WatchedLocation location;

    private List<Date> intervalTabDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_checker_records);

        Log.d(TAG, "Starting records activity");

        if (dataSource == null)
            dataSource = new AutoCheckerDataSource(this);

        dataSource.open();

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        // Notify broadcast receiver of first run
        if (prefs.getBoolean(AutoCheckerConstants.FIRST_RUN, true)) {
            prefs.edit().putBoolean(AutoCheckerConstants.FIRST_RUN, false);
            sendBroadcast(new Intent(AutoCheckerConstants.LOCATIONS_ACTIVITY_FIRST_RUN));
            // TODO: Open create location activity
        }

        //loadContents(prefs.getInt(AutoCheckerConstants.CURRENT_LOCATION_ID, 0));
        loadContents(AutoCheckerConstants.WL_GTD.getName());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new AutoCheckerLocationRecordPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setCurrentTab();

        setTitle(location.getName());

        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    private void setCurrentTab() {
        Date current = new Date();
        int pos = 0;
        for (int i = 0; i < intervalTabDates.size() - 1; i++) {
            if (intervalTabDates.get(i).before(current)) {
                pos = i;
            }
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout.getTabCount() > 0)
            tabLayout.getTabAt(pos).select();
    }

    private void loadContents(String locationName) {

        try {

            location = dataSource.getWatchedLocation(locationName);
            /*if (getPreferences(Context.MODE_PRIVATE).getBoolean(AutoCheckerConstants.PREF_TEST, true)) {
                if (dataSource.getDateIntervals(location, DateUtils.WEEK_INTERVAL_TYPE).isEmpty()) {
                    for (WatchedLocationRecord record : AutoCheckerConstants.getDummyRecords(location))
                        dataSource.insertRecord(record);
                }
            }*/

            //intervalTabDates = dataSource.getDateIntervals(location, DateUtils.WEEK_INTERVAL_TYPE);

        } catch (NoLocationFoundException e) {
            Log.e(TAG, "Location ID not found", e);
        } catch (SQLException e) {
            Log.e(TAG, "Error retrieving records", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auto_checker_records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public AutoCheckerDataSource getDatSource() {
        return dataSource;
    }

    public WatchedLocation getLocation() {
        return location;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class AutoCheckerLocationRecordPageAdapter extends FragmentStatePagerAdapter {

        public AutoCheckerLocationRecordPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return AutoCheckerWeekRecordsFragment.newInstance(location.getId(), intervalTabDates.get(i), 0, false);
        }

        @Override
        public int getCount() {
            return intervalTabDates.size() - 1;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return DateUtils.getDateIntervalString(intervalTabDates, position);
        }
    }
}
