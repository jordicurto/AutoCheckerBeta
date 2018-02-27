package com.github.jordicurto.autochecker.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.jordicurto.autochecker.R;
import com.github.jordicurto.autochecker.constants.AutoCheckerConstants;
import com.github.jordicurto.autochecker.data.model.WatchedLocation;
import com.github.jordicurto.autochecker.fragment.AutoCheckerTotalWeekFragment;
import com.github.jordicurto.autochecker.fragment.AutoCheckerWeekRecordsFragment;
import com.github.jordicurto.autochecker.manager.AutoCheckerBusinessManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerNotificationManager;
import com.github.jordicurto.autochecker.manager.AutoCheckerPreferencesManager;
import com.github.jordicurto.autochecker.util.DateUtils;
import com.polyak.iconswitch.IconSwitch;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class AutoCheckerMainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        NavigationView.OnNavigationItemSelectedListener,
        AutoCheckerWeekRecordsFragment.OnTotalDurationUpdateListener {

    public final String TAG = getClass().getSimpleName();

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

    private AutoCheckerTotalWeekFragment mTotalWeekFragment;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private IconSwitch mIconSwitch;

    private WatchedLocation location;

    private List<Interval> intervalTabDates = new ArrayList<>();

    private AutoCheckerPreferencesManager preferencesManager = new AutoCheckerPreferencesManager();

    private int mCurrentSelectedTab = -1;

    private class AutoCheckerActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Intent received: " + intent.getAction());

            if (intent.getAction() != null &&
                    intent.getAction().equals(AutoCheckerConstants.INTENT_ACTIVITY_RELOAD_REQUEST)) {
                invalidateOptionsMenu();
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private AutoCheckerActivityBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.autochecker_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter =
                new AutoCheckerLocationRecordPageAdapter(getSupportFragmentManager());

        mTotalWeekFragment = (AutoCheckerTotalWeekFragment)
                getSupportFragmentManager().findFragmentById(R.id.total_week_fragment);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTotalWeekFragment.showDurationText(mTabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mTotalWeekFragment.showDurationText(mTabLayout.getSelectedTabPosition());
            }
        });

        mIconSwitch = (IconSwitch) findViewById(R.id.absolute_relative_switch);
        mIconSwitch.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                changeRelativeAbsoluteDuration((current == IconSwitch.Checked.RIGHT));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            mTotalWeekFragment = (AutoCheckerTotalWeekFragment)
                    getSupportFragmentManager().getFragment(savedInstanceState, "totalWeekFragment");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putInt(CURRENT_SELECTED_TAB, mTabLayout.getSelectedTabPosition());
            getSupportFragmentManager().putFragment(outState, "totalWeekFragment", mTotalWeekFragment);
        }
        super.onSaveInstanceState(outState);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void selectTab() {
        if (mTabLayout.getTabCount() > 0) {
            int selectedTab;
            if (mCurrentSelectedTab >= 0 && mCurrentSelectedTab < mTabLayout.getTabCount())
                selectedTab = mCurrentSelectedTab;
            else
                selectedTab = mTabLayout.getTabCount() - 1;
            mTabLayout.getTabAt(selectedTab).select();
        }
    }

    private void loadContents() {

        Log.d(TAG, "Loading contents");

        AutoCheckerBusinessManager manager = AutoCheckerBusinessManager.getManager(this);
        manager.cleanOldWatchedLocationRecords(AutoCheckerConstants.RECORDS_HOLD_DURATION);

        preferencesManager.updatePreferences(this);

        String currentLocationName = preferencesManager.getCurrentLocationName();

        // TODO: GTD Version to remove
        if (!manager.existsWatchedLocation(currentLocationName)) {
            manager.insertWatchedLocation(AutoCheckerConstants.WL_GTD);
        }

        location = manager.getWatchedLocation(currentLocationName);
        int startDayHour = preferencesManager.getStartDayHour();
        int weeksToShow = preferencesManager.getWeeksToShow();
        List<Interval> intervals =
                manager.getDateIntervals(location, DateUtils.INTERVAL_TYPE.WEEKS, startDayHour);
        if (intervals.size() > weeksToShow) {
            intervalTabDates = intervals.subList(intervals.size() - weeksToShow, intervals.size());
        } else {
            intervalTabDates = intervals;
        }

        invalidateOptionsMenu();

        mIconSwitch.setChecked(preferencesManager.isRelativeDurations() ?
                IconSwitch.Checked.RIGHT : IconSwitch.Checked.LEFT);

        mViewPager.getAdapter().notifyDataSetChanged();
    }

    private void checkFirstRun() {

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

        long installTime =
                DateUtils.getApplicationInstallTime(this, getApplication().getPackageName());

        if (prefs.getLong(AutoCheckerConstants.INSTALL_TIME, 0) < installTime) {
            prefs.edit().putLong(AutoCheckerConstants.INSTALL_TIME, installTime).apply();
            checkLocationSettings();
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
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            AutoCheckerNotificationManager.getInstance(this).cancelNotification(
                    AutoCheckerConstants.NOTIFICATION_PERMISSION_REQUIRED);
            checkLocationSettings();
        } else {
            AutoCheckerNotificationManager.getInstance(this).notifyPermissionRequired(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    private void checkLocationSettings() {
        sendBroadcast(new Intent(AutoCheckerConstants.INTENT_REQUEST_CHECK_LOCATION));
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

        if (id == R.id.action_settings) {
            Intent launchNewIntent = new Intent(AutoCheckerMainActivity.this,
                    AutoCheckerSettingsActivity.class);
            startActivityForResult(launchNewIntent, 0);
            return true;
        }

        if (id == R.id.inside_location_action) {
            Toast.makeText(this, getString(location.isInside() ?
                    R.string.inside_location_text : R.string.outside_location_text,
                    location.getName()), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateTotalDuration(String durationText, DateTime start) {

        int pos = 0;
        for (Interval interval : intervalTabDates) {
            if (interval.getStart().equals(start)) {
                break;
            }
            pos++;
        }
        mTotalWeekFragment.updateTotalDuration(durationText, pos);
        if (mTabLayout.getSelectedTabPosition() == pos) {
            mTotalWeekFragment.showDurationText(pos);
        }
    }

    private void changeRelativeAbsoluteDuration(boolean relative) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().
                putBoolean(AutoCheckerConstants.PREF_SHOW_RELATIVE_DURATIONS, relative).apply();
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class AutoCheckerLocationRecordPageAdapter extends FragmentStatePagerAdapter {

        private AutoCheckerLocationRecordPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return AutoCheckerWeekRecordsFragment.newInstance(
                    intervalTabDates.get(i).getStart().toLocalDate());
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
