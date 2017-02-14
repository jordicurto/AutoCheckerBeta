package com.github.jordicurto.autochecker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.fragment.AutoCheckerWeekRecordsFragment;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AutoCheckerMainActivity extends AppCompatActivity {

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

    private WatchedLocation location;

    private List<Date> intervalTabDates = new ArrayList<>();
    private int startDayHour;
    private boolean showWeekends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_checker_main);

        loadContents();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new AutoCheckerLocationRecordPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    private void loadContents() {

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        String currentLocationName = prefs.getString(AutoCheckerConstants.CURRENT_LOCATION_ID,
                AutoCheckerConstants.WL_GTD.getName());

        AutoCheckerBusinessManager manager = AutoCheckerBusinessManager.getManager(this);

        if (!manager.existsWatchedLocation(currentLocationName)) {
            manager.insertWatchedLocation(AutoCheckerConstants.WL_GTD);
        }

        location = manager.getWatchedLocation(currentLocationName);
        startDayHour = prefs.getInt(AutoCheckerConstants.PREF_START_DAY_HOUR,
                AutoCheckerConstants.DEFAULT_START_DAY_HOUR);
        intervalTabDates = manager.getDateIntervals(location, startDayHour,
                DateUtils.WEEK_INTERVAL_TYPE);
        showWeekends = prefs.getBoolean(AutoCheckerConstants.PREF_SHOW_WEEKENDS, false);

        setTitle(currentLocationName);

        if (!prefs.getBoolean(AutoCheckerConstants.FIRST_RUN, false)) {
            sendBroadcast(new Intent(AutoCheckerConstants.LOCATIONS_ACTIVITY_FIRST_RUN));
            prefs.edit().putBoolean(AutoCheckerConstants.FIRST_RUN, true).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auto_checker_main, menu);
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
            return AutoCheckerWeekRecordsFragment.newInstance(location.getId(),
                    intervalTabDates.get(i), startDayHour, showWeekends);
        }

        @Override
        public int getCount() {
            return intervalTabDates.size() - 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return DateUtils.getDateIntervalString(intervalTabDates, position);
        }
    }
}
