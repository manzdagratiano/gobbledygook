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

// Standard Java
import java.lang.RuntimeException;

// ==========================================================================

/**
 * @brief   
 */
public abstract class PrefsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener{

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   
     * @return  Does not return a value.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String FUNC = "onCreate()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating PreferenceFragment...");

        super.onCreate(savedInstanceState);

        // Load the preferences from the xml resource
        addPreferencesFromResource(R.xml.preferences);

        // Get a handle to the SharedPreferences
        this.getSharedPreferencesHandle();
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
        final String FUNC = "onResume()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring elements...");

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
        final String FUNC = "onPause()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Cleaning up...");
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
     * @brief   Handler called when any of the SharedPreference elements
     *          are changed.
     * @return  Does not return a value.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        final String FUNC = "onSharedPreferencesChanged()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "key='" + key + "'");

        if (key.equals(
                    getString(R.string.pref_saltKey_key))) {
            String newSaltKey = sharedPreferences.getString(key, "");
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "New saltKey='" + newSaltKey + "'");

            Preference saltKeyPref = (Preference)findPreference(key);
            saltKeyPref.setSummary(newSaltKey);
        } else if (key.equals(
                    getString(R.string.pref_defaultIterations_key))) {
            Preference defaultIterationsPref = (Preference)findPreference(key);
            // Modify the summary to reflect the value selected by the user;
            // fetch the value of the default iterations as string.
            String newDefaultIterationsStr =
                sharedPreferences.getString(key, "");
            if (!newDefaultIterationsStr.isEmpty()) {
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "New defaultIterations=" + newDefaultIterationsStr);
                defaultIterationsPref.setSummary(newDefaultIterationsStr);
            }
        }
    }

    // ====================================================================
    // PROTECTED METHODS

    /**
     * @brief   An method to obtain the log category,
     *          suitably overridden in the concrete implementation.
     * @return  {String} The log category.
     */
    protected abstract String getLogCategory();

    /**
     * @brief   A method to get a prefix for the log.
     * @return  {String} The log prefix
     */
    protected String getLogPrefix(String FUNC) {
        final String LOG_TAG = "PREFS";
        return LOG_TAG + "." + FUNC + ": ";
    }

    /**
     * @brief   Method to configure listeners for the preference elements.
     * @return  Does not even.
     */
    protected void configurePreferenceElements() {
        final String FUNC = "configurePreferenceElements()";

        this.configureSaltKey();
        this.configureDefaultIterations();
        this.configureCustomAttributes();

        // The OnSharedPreferenceChangedListener for all Preference changes
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Registering onSharedPreferenceChangedListeners...");
        m_sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @brief   A method to return the implementation of the DialogFragment.
     *          (must be overridden in derived classes).
     * @return  {DialogFragment} The instance of the concrete implementation
     *          of the DialogFragment
     */
    protected abstract DialogFragment
    getSaltKeyActionsFragment(final boolean showAsDialog);

    /**
     * @brief   Method to perform any cleanup, such as freeing handlers
     *          for garbage collection
     * @return  Does not return a value
     */
    protected void deconfigurePreferenceElements() {
        // Unregister the PreferenceChangeListener
        final String FUNC = "deconfigurePreferenceElements()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Deregistering onSharedPreferenceChangedListeners...");
        m_sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        // Deregister the saltKey OnClick listener
        Preference saltKeyPref =
            (Preference)findPreference(
                    getString(R.string.pref_saltKey_key));
        saltKeyPref.setOnPreferenceClickListener(null);
    }

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final int    DEFAULT_ITERATIONS  = 10000;
    private static final String EMPTY_CUSTOM_ATTRS_INDICATOR
                                                    = "<null>";

    // --------------------------------------------------------------------
    // METHODS

    private void getSharedPreferencesHandle() {
        m_sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        if (null == m_sharedPreferences) {
            throw new RuntimeException("SharedPreferences.Null!");
        }
    }

    /**
     * @brief   Function to configure the summary for the saltKey,
     *          and to set it read-only
     * @return  Does not return a value
     */
    private void configureSaltKey() {
        final String FUNC = "configureSaltKey()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Setting value...");

        Preference saltKeyPref =
            (Preference)findPreference(
                    getString(R.string.pref_saltKey_key));

        // Set the summary with the value
        String saltKey =
            m_sharedPreferences.getString(
                    getString(R.string.pref_saltKey_key), "");
        // The saltKey should never be empty,
        // since it is generated on first start when it does not exist
        saltKeyPref.setSummary(saltKey);

        // Set an onClick listener to view/edit the salt key
        saltKeyPref.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            // @Override
            public boolean onPreferenceClick(Preference preference) {
                final String FUNC = "onPreferenceClick()";
                // Display the SaltKeyFragment as a dialog
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "Creating SaltKeyActions dialog...");

                // Show the SaltKeyActionsFragment as a Dialog
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
                    getSaltKeyActionsFragment(showAsDialog);

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
     * @return  Does not even
     */
    private void configureDefaultIterations() {
        final String FUNC = "configureDefaultIterations()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Setting value...");

        Preference defaultIterationsPref =
            (Preference)findPreference(
                    getString(R.string.pref_defaultIterations_key));

        // Set the summary with the value
        // defaultIterations needs to be retrieved as text
        String defaultIterationsStr =
            m_sharedPreferences.getString(
                getString(R.string.pref_defaultIterations_key), "");
        if (!defaultIterationsStr.isEmpty()) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "Found non-empty defaultIterations='" +
                  defaultIterationsStr + "'");
            defaultIterationsPref.setSummary(defaultIterationsStr);
        }
    }

    /**
     * @brief   
     * @return  Does not even
     */
    private void configureCustomAttributes() {
        final String FUNC = "configureCustomAttributes()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Setting value...");

        Preference customOverridesPref =
            (Preference)findPreference(
                    getString(R.string.pref_customOverrides_key));

        // Set the summary with the value
        String customOverrides =
            m_sharedPreferences.getString(
                    getString(R.string.pref_customOverrides_key), "");
        if (customOverrides.isEmpty()) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "The list of custom attributes is empty");
            customOverridesPref.setSummary(EMPTY_CUSTOM_ATTRS_INDICATOR);
        } else {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "Found non-empty customOverrides='" +
                  customOverrides + "'");
            customOverridesPref.setSummary(customOverrides);
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS

    /** @brief A handle to the SharedPreferences for the application.
     */
    protected SharedPreferences m_sharedPreferences;

}
