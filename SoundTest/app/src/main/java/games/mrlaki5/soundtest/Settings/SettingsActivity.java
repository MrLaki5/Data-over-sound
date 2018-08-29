package games.mrlaki5.soundtest.Settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import games.mrlaki5.soundtest.R;

public class SettingsActivity extends AppCompatPreferenceActivity {

    //Preference keys
    public static String KEY_START_FREQUENCY="StFrequency";
    public static String KEY_END_FREQUENCY="EndFrequency";
    public static String KEY_BIT_PER_TONE="BitPerTone";
    public static String KEY_ENCODING="Encoding";
    public static String KEY_ERROR_DETECTION="ErrDetection";
    public static String KEY_ERROR_BYTE_NUM="ErrByteNum";

    //Preference default values
    public static String DEF_START_FREQUENCY= "17500";
    public static String DEF_END_FREQUENCY= "20000";
    public static String DEF_BIT_PER_TONE="4";
    public static boolean DEF_ENCODING=false;
    public static boolean DEF_ERROR_DETECTION=false;
    public static String DEF_ERROR_BYTE_NUM="4";

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        if(ab!=null){
            ab.setTitle("Settings");
        }
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            //Adding change listeners
            bindPreferenceSummaryToValue(findPreference(KEY_START_FREQUENCY));
            bindPreferenceSummaryToValue(findPreference(KEY_END_FREQUENCY));
            bindPreferenceSummaryToValue(findPreference(KEY_BIT_PER_TONE));
            bindPreferenceSummaryToValue(findPreference(KEY_ERROR_BYTE_NUM));
        }
    }

    //Listener for menu in settings activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        else{
            //On default settings chosen, set all preferences to default values and refresh view
            if(item.getItemId() == R.id.settings_menu_default_settings){
                SharedPreferences preferences =PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString(SettingsActivity.KEY_START_FREQUENCY,
                        SettingsActivity.DEF_START_FREQUENCY);
                editor.putString(SettingsActivity.KEY_END_FREQUENCY,
                        SettingsActivity.DEF_END_FREQUENCY);
                editor.putString(SettingsActivity.KEY_BIT_PER_TONE,
                        SettingsActivity.DEF_BIT_PER_TONE);
                editor.putBoolean(SettingsActivity.KEY_ENCODING,
                        SettingsActivity.DEF_ENCODING);
                editor.putBoolean(SettingsActivity.KEY_ERROR_DETECTION,
                        SettingsActivity.DEF_ERROR_DETECTION);
                editor.putString(SettingsActivity.KEY_ERROR_BYTE_NUM,
                        SettingsActivity.DEF_ERROR_BYTE_NUM);
                editor.commit();
                getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //Create settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    //Bind preferences to view preferences
    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    //A preference value change listener that updates the preference's summary to reflect its new value.
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference) {
                //For list preferences, look up the correct display value in
                //the preference's entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }
            else{
                if (preference instanceof EditTextPreference) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };
}
