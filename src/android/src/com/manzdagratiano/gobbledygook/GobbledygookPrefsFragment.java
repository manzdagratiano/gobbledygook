/**
 * @file        GobbledygookPrefsFragment.java
 * @brief       Source file for the GobbledygookPrefsFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package com.manzdagratiano.gobbledygook;

// Android
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

// ==========================================================================

/**
 * @brief   
 */
public class GobbledygookPrefsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener{

    /**
     * @brief   Namespace "Env" for constants used throughout the class
     */
    public class Env {
        public static final String LOG_CATEGORY = "GOBBLEDYGOOK.PREFS";
        public static final int DEFAULT_ITERATIONS = 10000;
    }

    /**
     * @brief   
     * @return  Does not return a value.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(Env.LOG_CATEGORY, "onCreate(): Creating activity...");

        super.onCreate(savedInstanceState);

        // Load the preferences from the xml resource
        addPreferencesFromResource(R.xml.preferences);

        // Attach a listener to the "Load Salt Key..." Preference
        configureLoadSaltKeyButton();
    }

    /**
     * @brief   
     * @return  Does not return a value.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.i(Env.LOG_CATEGORY,
              "onSharedPreferencesChanged() handler called. " +
              "key='" + key + "'");
        if (key.equals(getString(R.string.pref_defaultIterations_key))) {
            Preference defaultIterationsPref = (Preference)findPreference(key);
            // Modify the summary to reflect the value selected by the user;
            // fetch the value of the default iterations as string.
            String newDefaultIterationsStr =
                sharedPreferences.getString(key, "");
            if (!newDefaultIterationsStr.isEmpty()) {
                Log.i(Env.LOG_CATEGORY,
                      "New defaultIterations=" + newDefaultIterationsStr);
                defaultIterationsPref.setSummary(newDefaultIterationsStr);
            }
        }
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.i(Env.LOG_CATEGORY, "onResume(): Registering changedListeners...");

        // The OnSharedPreferenceChangedListener for all Preference changes
        getPreferenceManager(
                ).getSharedPreferences(
                    ).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.i(Env.LOG_CATEGORY, "onPause(): Deregistering changedListeners...");

        // Unregister the PreferenceChangeListener
        getPreferenceManager(
                ).getSharedPreferences(
                    ).unregisterOnSharedPreferenceChangeListener(this);
    }

    // --------------------------------------------------------------------
    // PRIVATE MEMBERS

    /**
     * @brief   Function to attach a "click" listener to the loadSaltKey
     * preference "button"
     * @return  Does not return a value
     */
    private void configureLoadSaltKeyButton() {
        Log.i(Env.LOG_CATEGORY,
              "configureLoadSaltKeyButton(): Attaching clickListener...");

        Preference loadSaltKeyButton =
            (Preference)findPreference(
                    getString(
                        R.string.pref_loadSaltKey_key));
        loadSaltKeyButton.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Open the file picker dialog to select the key file
                        // @TODO
                        return true;
                    }
                }
        );
    }

}
