package com.example.mike.phototweak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.util.Iterator;
import java.util.List;

/**
 * The Top level {@link AppCompatActivity} activity. Contains AppBar/Toolbar and tabs to select
 * which Fragment is displayed. Manages all fragments. Implements all callbacks related to  Network,
 * Prefrences, and TabLayout changes. A ViewPager is not used with TabLayout because it doesn't play
 * nicely with the VerticalSeekBar implentation being used.
 */
public class TopLevelActivity extends AppCompatActivity
        implements
        NetworkIOFragment.NetworkIOCallback,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TabLayout.OnTabSelectedListener {

    // Help/Info screen
    WebView mHelpView;
    // A reference to the NetworkFragment which that is used to execute network ops.
    private NetworkIOFragment mNetwork;

    // Global preferences
    SharedPreferences prefs;

    // Progress bar is displayed when there's a network operation
    private ProgressBar mProgressSpinner;

    private boolean demoMode;
    /**
     * Signal all active fragments that the network connection to the device is active,
     * (i.e. call their respective .deviceConnectionActive() functions).
     *
     * @param isActive Is the network connection active?
     */

    private void deviceConnectionIsActive(boolean isActive) {

        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        Iterator<Fragment> allFragmentsIterator = allFragments.iterator();
        // Iterate through all active ContentFragments and invoke their respective deviceConnectionActive() functions
        while (allFragmentsIterator.hasNext()) {
            Fragment aFragment = allFragmentsIterator.next();

            if (aFragment instanceof ContentFragment) {
                ((ContentFragment) aFragment).deviceConnectionActive(isActive);

            }
        }
    }

    // Call back implementations:

    // Call back implementation for NetworkIOFragment.NetworkIOCallback

    @Override
    public void updateFromDownload(String result) {
        if (result != null) {
            // What we received from server (nothing)
        } else {
            //
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch (progressCode) {
            case Progress.ERROR:
                // Connection to device was not successfull.
                // Display error message Snackbar with option to retry network connection
                Snackbar s = Snackbar.make(findViewById(android.R.id.content),
                        R.string.networkio_error_message,
                        Snackbar.LENGTH_INDEFINITE).setAction(
                        R.string.networkio_error_retry,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mNetwork != null) {
                                    if (!mNetwork.isConnected()) {
                                        // connect to device
                                        mNetwork.startNetworkIO();
                                        mNetwork.stopNetworkIO();
                                    }
                                }
                            }
                        });
                s.show();
                // Signal network is disabled for all fragments
                if (!demoMode) deviceConnectionIsActive(false);
                mProgressSpinner.setVisibility(View.GONE);
                break;
            case Progress.CONNECT_SUCCESS:
                // Connection attempt in progress..
                if (percentComplete == 0) {
                    // Attempting to connect ...
                    // Show the progressSpinner progress bar and Snackbar message while waiting to establish connection
                    mProgressSpinner.setVisibility(View.VISIBLE);
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.networkio_connecting_message) +
                            prefs.getString(SettingsActivity.KEY_PREF_deviceIP, ""),
                            Snackbar.LENGTH_LONG)
                            .setAction("", null).show();
                    deviceConnectionIsActive(false);
                } else if (percentComplete == 100) {
                    // Connection to device was successfull, disable progressSpinner and display
                    // Snackbar message
                    mProgressSpinner.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.networkio_connected_message) +
                            prefs.getString(SettingsActivity.KEY_PREF_deviceIP, ""),
                            Snackbar.LENGTH_LONG)
                            .setAction("", null).show();
                    // Signal network is enabled to fragments
                    deviceConnectionIsActive(true);
                }
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void cancelNetworkIO() {
        mProgressSpinner.setVisibility(View.GONE);
        if (mNetwork != null) {
            mNetwork.stopNetworkIO();
        }
    }

    // Call back implementation SharedPreferences.OnSharedPreferenceChangeListener
    //
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        demoMode = prefs.getBoolean(SettingsActivity.KEY_PREF_demoMode,false);
        // Change the network configuration of our network fragment
        if (mNetwork!=null) {
            mNetwork.setConfig(prefs.getString(SettingsActivity.KEY_PREF_deviceIP, ""),
                    Integer.parseInt(prefs.getString(SettingsActivity.KEY_PREF_devicePort, "")),
                    demoMode);

            if (!prefs.getBoolean(SettingsActivity.KEY_PREF_demoMode,false))
                    deviceConnectionIsActive(true);

        }
    }


    // Call back implementation TabLayout.OnTabSelectedListener

    public void onTabReselected(TabLayout.Tab tab) {

        // start network connection if it's ready to go
        if (mNetwork != null) {
            if (!mNetwork.isConnected()) {
                // connect to device
                mNetwork.startNetworkIO();
                mNetwork.stopNetworkIO();
            }
        }
    }

    public void onTabSelected(TabLayout.Tab tab) {

        ContentFragment fragment = null;
        int fragmentId = tab.getPosition();

        // start network connection of it's ready to go
        if (mNetwork != null) {
            if (!mNetwork.isConnected()) {
                // connect to device
                mNetwork.startNetworkIO();
                mNetwork.stopNetworkIO();
            }
        }
        // Get the appropriate fragment for this tab
        if (fragmentId == 0) {
            fragment = LightingLevelFragment.getInstance(getSupportFragmentManager());
        } else {
           // fragment = (ContentFragment) PlaceholderFragment.newInstance(fragmentId);
        }
        // show the fragment and add to backstack
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, fragment, fragment.getFragmentTag());
            ft.addToBackStack(null);
            ft.commit();

        }
    }

    public void onTabUnselected(TabLayout.Tab tab) {
    }

    // Boilerplate Activity lifecycle callbacks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_top_level);

        // Get the ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        // get global preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        demoMode = prefs.getBoolean(SettingsActivity.KEY_PREF_demoMode,false);
        // Get the network fragment
        mNetwork = NetworkIOFragment.getInstance(getSupportFragmentManager(),
                prefs.getString(SettingsActivity.KEY_PREF_deviceIP, "192.168.0.65"),
                Integer.parseInt(prefs.getString(SettingsActivity.KEY_PREF_devicePort, "9000")),
                demoMode);

        // setup the network delay progress spinner

        mProgressSpinner = (ProgressBar) findViewById(R.id.connectProgressBar);
        mProgressSpinner.setVisibility(View.GONE);
        mProgressSpinner.bringToFront();

        // Are we currently waiting for a network operation?, if so make sure "busy" overlay is visible
        if (mNetwork.getIsNetworkBusy())
            mProgressSpinner.setVisibility(View.VISIBLE);

        // Add tabs
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setOnTabSelectedListener(this);
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.ic_wb_sunny_black_48dp));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.ic_hourglass_full_black_48dp));

        mHelpView = (WebView) findViewById(R.id.webview);
        String infoPage = getString(R.string.infopage);
        mHelpView.loadData(infoPage,"text/html","utf-8");
        // mHelpView.setVisibility(View.VISIBLE);


    }

    @Override

    public  void onResume() {

        // if DemoMode is on we want controls to work
        if (prefs.getBoolean(SettingsActivity.KEY_PREF_demoMode,false)) {
            Log.v(TopLevelActivity.class.getSimpleName(), "demoMode = " + prefs.getBoolean(SettingsActivity.KEY_PREF_demoMode,false));
            deviceConnectionIsActive(true);
        }
        super.onResume();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_level, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Layunch settings fragment
            Intent launchSettings = new Intent(this, SettingsActivity.class);
            startActivity(launchSettings);
            // SettingsActivity class must exist for us to register the PreferenceChangeListener
            prefs.registerOnSharedPreferenceChangeListener(this);
            return true;
        }
        if (id == R.id.action_info) {
            // Launch App info fragment
            DialogFragment fragment = new InfoScreenFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            fragment.show(ft, InfoScreenFragment.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Disable the back button for this Activity
        //super.onBackPressed();
    }


}
