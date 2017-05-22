package com.github.jordicurto.autochecker.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.fragment.AutoCheckerWeekRecordsFragment;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.util.DateUtils;

import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class AutoCheckerMainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String CURRENT_SELECTED_TAB = "CURRENT_SELECTED_TAB";
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
    private TabLayout mTabLayout;

    private WatchedLocation location;

    private List<Interval> intervalTabDates = new ArrayList<>();
    private int startDayHour;
    private boolean showWeekends;

    private int mCurrentSelectedTab = -1;

    private class AutoCheckerActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AutoCheckerConstants.INTENT_ACTIVITY_RELOAD_REQUEST)) {
                invalidateOptionsMenu();
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private AutoCheckerActivityBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auto_checker_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter =
                new AutoCheckerLocationRecordPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        checkPermissions();

        mBroadcastReceiver = new AutoCheckerActivityBroadcastReceiver();
        registerReceiver(mBroadcastReceiver,
                new IntentFilter(AutoCheckerConstants.INTENT_ACTIVITY_RELOAD_REQUEST));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_SELECTED_TAB))
                mCurrentSelectedTab = savedInstanceState.getInt(CURRENT_SELECTED_TAB);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            outState.putInt(CURRENT_SELECTED_TAB, mTabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContents();
        selectTab();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void selectTab() {
        if (mTabLayout.getTabCount() > 0) {
            if (mCurrentSelectedTab >= 0 && mCurrentSelectedTab < mTabLayout.getTabCount())
                mTabLayout.getTabAt(mCurrentSelectedTab).select();
            else
                mTabLayout.getTabAt(mTabLayout.getTabCount() - 1).select();
        }
    }

    private void loadContents() {

        Log.d(getClass().getSimpleName(), "Loading contents");

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
        intervalTabDates =
                manager.getDateIntervals(location, DateUtils.INTERVAL_TYPE.WEEKS, startDayHour);
        showWeekends = prefs.getBoolean(AutoCheckerConstants.PREF_SHOW_WEEKENDS, false);

        setTitle(currentLocationName);

        invalidateOptionsMenu();

        mViewPager.getAdapter().notifyDataSetChanged();
    }

    private void checkFirstRun() {

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        long installTime =
                DateUtils.getApplicationInstallTime(this, getApplication().getPackageName());

        if (prefs.getLong(AutoCheckerConstants.INSTALL_TIME, 0) < installTime) {
            prefs.edit().putLong(AutoCheckerConstants.INSTALL_TIME, installTime).apply();
            requestRegisterGeofences();
        }
    }

    private void checkPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions
                    (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            checkFirstRun();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            requestRegisterGeofences();
    }

    private void requestRegisterGeofences() {
        sendBroadcast(new Intent(AutoCheckerConstants.INTENT_REQUEST_REGISTER_GEOFENCES));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auto_checker_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem insideLocationIndicator = menu.findItem(R.id.inside_location_action);

        if (location != null) {
            if (location.isInside())
                insideLocationIndicator.setIcon(R.drawable.ic_inside_indicator_action);
            else
                insideLocationIndicator.setIcon(R.drawable.ic_outside_indicator_action);
        }

        return super.onPrepareOptionsMenu(menu);
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
                    intervalTabDates.get(i).getStart().toLocalDate(), startDayHour, showWeekends);
        }

        @Override
        public int getCount() {
            return intervalTabDates.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return DateUtils.getDateIntervalString(intervalTabDates.get(position));
        }
    }
}
