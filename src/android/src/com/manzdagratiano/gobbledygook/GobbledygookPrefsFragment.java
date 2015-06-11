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
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   
     * @return  Does not return a value.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "onCreate(): Creating activity...");

        super.onCreate(savedInstanceState);

        // Load the preferences from the xml resource
        addPreferencesFromResource(R.xml.preferences);
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStart() {
        super.onStart();

        Log.i(LOG_CATEGORY, "onStart(): Configuring elements...");
        configurePreferenceElements();

        // The OnSharedPreferenceChangedListener for all Preference changes
        Log.i(LOG_CATEGORY, "onStart(): "+
              "Registering onSharedPreferenceChangedListeners...");
        getPreferenceManager(
                ).getSharedPreferences(
                    ).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @brief   This lifecycle function is called after onStart(),
     *          and also when the activity comes
     *          back to the foreground. Don't want to be doing anything with
     *          handler registration here.
     * @return  
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * @brief   This lifecycle function is called when the activity is
     * sent to the background, such as when partially covered by another
     * activity, and also before onStop(). Don't want to be doing anything
     * with handler deregistration here.
     * @return  
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStop() {
        // Unregister the PreferenceChangeListener
        Log.i(LOG_CATEGORY, "onStop(): " +
              "Deregistering onSharedPreferenceChangedListeners...");
        getPreferenceManager(
                ).getSharedPreferences(
                    ).unregisterOnSharedPreferenceChangeListener(this);

        // Unregister the saltKey click listener
        Log.i(LOG_CATEGORY, "onStop(): " +
              "Cleaning up onPreferenceClickListeners...");
        Preference saltKeyPref = (Preference)findPreference(
                                    getString(R.string.pref_saltKey_key));
        saltKeyPref.setOnPreferenceClickListener(null);

        super.onStop();
    }

    // --------------------------------------------------------------------
    // HANDLERS

    /**
     * @brief   
     * @return  Does not return a value.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.i(LOG_CATEGORY,
              "onSharedPreferencesChanged() handler called. " +
              "key='" + key + "'");

        if (key.equals(
                    getString(R.string.pref_saltKey_key))) {
            String newSaltKey = sharedPreferences.getString(key, "");
            Log.i(LOG_CATEGORY, "onSharedPreferencesChanged(): " +
                  "New saltKey='" + newSaltKey + "'");

            Preference saltKeyPref = (Preference)findPreference(key);
            saltKeyPref.setSummary(
                    newSaltKey.isEmpty() ? EMPTY_SALT_KEY_INDICATOR 
                                         : newSaltKey);
        } else if (key.equals(
                    getString(R.string.pref_defaultIterations_key))) {
            Preference defaultIterationsPref = (Preference)findPreference(key);
            // Modify the summary to reflect the value selected by the user;
            // fetch the value of the default iterations as string.
            String newDefaultIterationsStr =
                sharedPreferences.getString(key, "");
            if (!newDefaultIterationsStr.isEmpty()) {
                Log.i(LOG_CATEGORY, "onSharedPreferencesChanged(): " +
                      "New defaultIterations=" + newDefaultIterationsStr);
                defaultIterationsPref.setSummary(newDefaultIterationsStr);
            }
        }
    }

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY        = "GOBBLEDYGOOK.PREFS";
    private static final int    DEFAULT_ITERATIONS  = 10000;
    private static final String EMPTY_SALT_KEY_INDICATOR
                                                    = "<null>";

    /**
     * @brief   
     * @return  
     */
    private void configurePreferenceElements() {

        class Configurator {

            /**
             * @brief   Function to configure the summary for the saltKey,
             *          and to set it read-only
             * @return  Does not return a value
             */
            public void configureSaltKey() {
                Log.i(LOG_CATEGORY, "configureSaltKey(): Setting value...");

                Preference saltKeyPref =
                    (Preference)findPreference(
                            getString(R.string.pref_saltKey_key));
                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                            ((GobbledygookPrefs)getActivity(
                                )).getApplicationContext());

                // Set the summary with the value
                String saltKey =
                    sharedPreferences.getString(
                            getString(R.string.pref_saltKey_key), "");
                if (saltKey.isEmpty()) {
                    Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                          "The salt key is empty");
                    saltKeyPref.setSummary(EMPTY_SALT_KEY_INDICATOR);
                } else {
                    Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                          "Found non-empty saltKey='" + saltKey + "'");
                    saltKeyPref.setSummary(saltKey);
                }

                // Disable clicking on the saltKey;
                // this is a read-only preference
                saltKeyPref.setEnabled(false);
            }

            /**
             * @brief   
             * @return  
             */
            public void configureDefaultIterations() {
                Log.i(LOG_CATEGORY,
                      "configureDefaultIterations(): Setting value...");

                Preference defaultIterationsPref =
                    (Preference)findPreference(
                            getString(R.string.pref_defaultIterations_key));
                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                            ((GobbledygookPrefs)getActivity(
                                )).getApplicationContext());

                // Set the summary with the value
                // defaultIterations needs to be retrieved as text
                String defaultIterationsStr =
                    sharedPreferences.getString(
                        getString(R.string.pref_defaultIterations_key), "");
                if (!defaultIterationsStr.isEmpty()) {
                    Log.i(LOG_CATEGORY, "configureDefaultIterations(): " +
                          "Found non-empty defaultIterations='" +
                          defaultIterationsStr + "'");
                    defaultIterationsPref.setSummary(defaultIterationsStr);
                }
            }

            /**
             * @brief   
             * @return  
             */
            public void configureCustomAttributes() {
            }

        };  // end class Configurator

        Configurator configurator = new Configurator();

        configurator.configureSaltKey();
        configurator.configureDefaultIterations();
        configurator.configureCustomAttributes();
    }
}
