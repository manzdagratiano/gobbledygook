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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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

    // --------------------------------------------------------------------
    // CONSTANTS

    public static final String LOG_CATEGORY     = "GOBBLEDYGOOK.PREFS";
    public static final int DEFAULT_ITERATIONS  = 10000;

    // --------------------------------------------------------------------
    // PUBLIC MEMBERS

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
        } else if (key.equals(
                    getString(R.string.pref_unlockSaltKey_key))) {
            Preference unlockSaltKeyPref = (Preference)findPreference(key);
            // If the checkbox was checked, enable the Salt Key preference,
            // else disable it
            boolean unlockSaltKey = sharedPreferences.getBoolean(key, false);
            Preference saltKeyPref = (Preference)findPreference(
                                        getString(R.string.pref_saltKey_key));
            if (unlockSaltKey) {
                Log.i(LOG_CATEGORY, "onSharedPreferencesChanged(): "+
                      "Unlocking salt key...");
                saltKeyPref.setEnabled(true);
            } else {
                Log.i(LOG_CATEGORY, "onSharedPreferencesChanged(): "+
                      "Locking salt key...");
                saltKeyPref.setEnabled(false);
            }
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

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStart() {
        super.onStart();

        Log.i(LOG_CATEGORY, "onStart(): Configuring elements...");
        configurePreferenceElements();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onResume() {
        super.onResume();

        Log.i(LOG_CATEGORY, "onResume(): Registering changedListeners...");
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

        Log.i(LOG_CATEGORY, "onPause(): Deregistering changedListeners...");
        // Unregister the PreferenceChangeListener
        getPreferenceManager(
                ).getSharedPreferences(
                    ).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStop() {
        super.onStop();

        Log.i(LOG_CATEGORY, "onStop(): Cleaning up clickHandlers...");
        // Unregister the saltKey click listener
        Preference saltKeyPref = (Preference)findPreference(
                                    getString(R.string.pref_saltKey_key));
        saltKeyPref.setOnPreferenceClickListener(null);
    }

    /**
     * @brief   
     * @return  
     */
    public void handleFileSelection(Uri uri) {
        Log.i(LOG_CATEGORY, "handleFileSelection() called...");

        // Finally, uncheck the "unlockSaltkey" checkbox and
        // disable the "Salt Key" preference
    }

    // --------------------------------------------------------------------
    // PRIVATE MEMBERS

    /**
     * @brief   
     * @return  
     */
    private void configurePreferenceElements() {

        class Configurator {

            /**
             * @brief   Function to configure the summary for the saltKey,
             * and to attach a "click" listener to the saltKey
             * preference "button"
             * @return  Does not return a value
             */
            public void configureSaltKey() {
                Log.i(LOG_CATEGORY, "configureSaltKey(): Setting value...");

                Preference saltKeyPref =
                    (Preference)findPreference(
                            getString(R.string.pref_saltKey_key));
                SharedPreferences sharedPreferences =
                    getPreferenceManager().getSharedPreferences();

                // Set the summary with the value
                String saltKey =
                    sharedPreferences.getString(
                            getString(R.string.pref_saltKey_key), "");
                if (!saltKey.isEmpty()) {
                    Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                          "Found non-empty saltKey='" + saltKey + "'");
                    saltKeyPref.setSummary(saltKey);
                }

                // Attach a listener to the "Load Salt Key..." Preference
                Log.i(LOG_CATEGORY,
                      "configureSaltKey(): Attaching clickListener...");
                saltKeyPref.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean
                            onPreferenceClick(Preference preference) {
                                Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                                      "onPreferenceClick() handler called");
                                // Open the file picker dialog
                                // to select the key file.
                                // Create a new "Intent" to do so.
                                Intent intent =
                                    new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                // Filter to only show results that
                                // can be "opened"
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                // Filter to only show text files
                                // with the ".key" extension
                                intent.setType("text/*");
                                // Start the activity
                                Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                                      "Opening File Picker UI...");
                                startActivityForResult(
                                        intent,
                                        GobbledygookPrefs.READ_SALT_KEY_FILE);
                                // The callback "onActivityResult" will be called

                                // Satisfy the compiler
                                return true;
                            }
                        }
                );

                // Disable clicking on the saltKey by default for editing,
                // it should be enabled only when unlockSaltKey is checked
                saltKeyPref.setEnabled(false);
            }

            /**
             * @brief   
             * @return  
             */
            public void configureUnlockSaltKey() {
                Log.i(LOG_CATEGORY, "configureUnlockSaltKey(): " +
                      "Unchecking checkbox...");

                CheckBoxPreference unlockSaltKeyPref =
                    (CheckBoxPreference)(Preference)findPreference(
                            getString(R.string.pref_unlockSaltKey_key));
                unlockSaltKeyPref.setChecked(false);
                // The OnSharedPreferencesChangedHandler() will be called
                // when the unlockSaltKey checkbox is checked/unchecked,
                // so no extra handlers need be attached.
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
                    getPreferenceManager().getSharedPreferences();

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

        };  // end class Configurator

        Configurator configurator = new Configurator();

        configurator.configureSaltKey();
        configurator.configureUnlockSaltKey();
        configurator.configureDefaultIterations();
    }
}
