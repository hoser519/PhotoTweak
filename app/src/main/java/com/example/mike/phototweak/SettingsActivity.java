package com.example.mike.phototweak;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


//import static android.R.attr.fragment;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
\\
 */
public class SettingsActivity extends PreferenceActivity {


    public static final String KEY_PREF_deviceIP = "deviceIP";
    public static final String KEY_PREF_devicePort = "devicePort";
    public static final String KEY_PREF_demoMode = "demoMode";

    // Regegular expression for a properly formatted IP  address
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");
    // Regular expresion for a properly formmated IP port
    private static final Pattern IP_PORT
            = Pattern.compile(
            "([0-9]"+"[0-9]"+"[0-9]"+"[0-9])");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment= new MyPreferenceFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment);
        ft.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
/**
 * A {@link PreferenceFragment} for the the settings .
 */
    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            // Verify the IP address and Port are formatted correctly before changing them.
            findPreference(KEY_PREF_deviceIP).setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Matcher ipAddressOK = IP_ADDRESS.matcher((String) newValue);
//                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                            builder.setTitle("Invalid Input");
//                            builder.setMessage("Something's gone wrong...");
//                            builder.setPositiveButton(android.R.string.ok, null);
//                            builder.show();
                            return ipAddressOK.matches();
                        }

                    });
            findPreference(KEY_PREF_devicePort).setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {

                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            Matcher ipPortOK = IP_PORT.matcher((String) newValue);
                            return ipPortOK.matches();
                        }

                    });
        }

    }

}
