/**
 * @file        PrefsFragment.java
 * @brief       Source file for the PrefsFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
public class PrefsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener{

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   
     * @return  Does not return a value.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "onCreate(): Creating PreferenceFragment...");

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
    }

    /**
     * @brief   This lifecycle function is called after onStart(),
     *          and also when the activity comes back to the foreground.
     *          Perform any configuration here,
     *          to pick up any changes made in other fragments if
     *          this fragment was added to the back stack.
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        Log.i(LOG_CATEGORY, "onResume(): Configuring elements...");

        super.onResume();

        this.configurePreferenceElements();

    }

    /**
     * @brief   This lifecycle function is called when the activity is
     *          sent to the background, such as when
     *          partially covered by another activity, and before onStop().
     *          Perform any cleanup here, symmetric with onResume().
     * @return  Does not return a value
     */
    @Override
    public void onPause() {
        Log.i(LOG_CATEGORY, "onPause(): Cleaning up...");
        this.deconfigurePreferenceElements();

        super.onPause();
    }

    /**
     * @brief   
     * @return  Does not return a value
     */
    @Override
    public void onStop() {
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
    private static final String EMPTY_CUSTOM_ATTRS_INDICATOR
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
            public void
            configureSaltKey(SharedPreferences sharedPreferences) {
                Log.i(LOG_CATEGORY, "configureSaltKey(): Setting value...");

                Preference saltKeyPref =
                    (Preference)findPreference(
                            getString(R.string.pref_saltKey_key));

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

                // Set an onClick listener to view/edit the salt key
                saltKeyPref.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                    // @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Display the SaltKeyFragment as a dialog
                        Log.i(LOG_CATEGORY,
                              "onClick(): Creating SaltKeyActions dialog...");

                        // Show the WorkhorseFragment as a Dialog
                        FragmentManager fragmentManager =
                            getActivity().getFragmentManager();
                        FragmentTransaction fragmentTx =
                            fragmentManager.beginTransaction();
                        Fragment prevInstance =
                            fragmentManager.findFragmentByTag(
                                getString(R.string.tag_saltKeyActionsFragment));
                        if (null != prevInstance) {
                            fragmentTx.remove(prevInstance);
                        }
                        // Provide proper "back" navigation
                        fragmentTx.addToBackStack(null);

                        // Instantiate the fragment
                        boolean showAsDialog = true;
                        DialogFragment saltKeyActionsDialog =
                            SaltKeyActionsFragment.newInstance(showAsDialog);

                        // "show" will commit the transaction as well
                        saltKeyActionsDialog.show(
                                fragmentTx,
                                getString(R.string.tag_saltKeyActionsFragment));

                        // The click was handled, so return true
                        return true;
                    }
                });
            }

            /**
             * @brief   
             * @return  
             */
            public void
            configureDefaultIterations(SharedPreferences sharedPreferences) {
                Log.i(LOG_CATEGORY,
                      "configureDefaultIterations(): Setting value...");

                Preference defaultIterationsPref =
                    (Preference)findPreference(
                            getString(R.string.pref_defaultIterations_key));

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
            public void
            configureCustomAttributes(SharedPreferences sharedPreferences) {
                Log.i(LOG_CATEGORY, "configureCustomAttributes(): " +
                      "Setting value...");

                Preference customAttributesPref =
                    (Preference)findPreference(
                            getString(R.string.pref_siteAttributesList_key));

                // Set the summary with the value
                String customAttributes =
                    sharedPreferences.getString(
                            getString(R.string.pref_siteAttributesList_key), "");
                if (customAttributes.isEmpty()) {
                    Log.i(LOG_CATEGORY, "configureCustomAttributes(): " +
                          "The list of custom attributes is empty");
                    customAttributesPref.setSummary(EMPTY_CUSTOM_ATTRS_INDICATOR);
                } else {
                    Log.i(LOG_CATEGORY, "configureCustomAttributes(): " +
                          "Found non-empty customAttributes='" +
                          customAttributes + "'");
                    customAttributesPref.setSummary(customAttributes);
                }
            }

        };  // end class Configurator

        // Get the SharedPreferences handle
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());

        Configurator configurator = new Configurator();

        configurator.configureSaltKey(sharedPreferences);
        configurator.configureDefaultIterations(sharedPreferences);
        configurator.configureCustomAttributes(sharedPreferences);

        // The OnSharedPreferenceChangedListener for all Preference changes
        Log.i(LOG_CATEGORY, "configurePreferenceElements(): "+
              "Registering onSharedPreferenceChangedListeners...");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @brief   Method to perform any cleanup, such as freeing handlers
     *          for garbage collection
     * @return  Does not return a value
     */
    private void deconfigurePreferenceElements() {
        // Unregister the PreferenceChangeListener
        Log.i(LOG_CATEGORY, "deconfigurePreferenceElements(): " +
              "Deregistering onSharedPreferenceChangedListeners...");
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
