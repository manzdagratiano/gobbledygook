/**
 * @file        PrefsFragment.java
 * @summary     Source file for the PrefsFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

// Standard Java
import java.lang.RuntimeException;

// ==========================================================================

/**
 * @summary 
 */
public abstract class PrefsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener{

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @summary Called when the fragment is created.
     * @return  Does not return a value.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a handle to the SharedPreferences
        this.getSharedPreferencesHandle();
    }

    /**
     * @summary Called when the preference hierarchy is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {
        final String FUNC = "onCreatePreferences()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating PrefsFragment...");

        // Load the preferences from the xml resource
        setPreferencesFromResource(R.xml.preferences,
                                   rootKey);
    }

    /**
     * @summary This lifecycle function is called after onStart(),
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
     * @summary This lifecycle function is called when the activity is
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

    // --------------------------------------------------------------------
    // HANDLERS

    /**
     * @summary Handler called when any of the SharedPreference elements
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
     * @summary An method to obtain the log category,
     *          suitably overridden in the concrete implementation.
     * @return  {String} The log category.
     */
    protected abstract String getLogCategory();

    /**
     * @summary A method to get a prefix for the log.
     * @return  {String} The log prefix
     */
    protected String getLogPrefix(String FUNC) {
        final String LOG_TAG = "PREFS";
        return LOG_TAG + "." + FUNC + ": ";
    }

    /**
     * @summary Method to configure listeners for the preference elements.
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
     * @summary A method to return the implementation of the DialogFragment.
     *          (must be overridden in derived classes).
     * @return  {DialogFragment} The instance of the concrete implementation
     *          of the DialogFragment
     */
    protected abstract DialogFragment
    getSaltKeyActionsFragment(final boolean showAsDialog);

    /**
     * @summary Method to perform any cleanup, such as freeing handlers
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
     * @summary Function to configure the summary for the saltKey,
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

                // Instantiate the fragment
                boolean showAsDialog = true;
                DialogFragment saltKeyActionsDialog =
                    getSaltKeyActionsFragment(showAsDialog);

                // Show the SaltKeyActionsFragment as a Dialog
                final String fragmentTag =
                          getString(R.string.tag_saltKeyActionsFragment);
                FragmentManager fragmentManager =
                    ((AppCompatActivity)getActivity())
                        .getSupportFragmentManager();
                FragmentTransaction fragmentTx =
                    fragmentManager.beginTransaction();
                Fragment prevInstance =
                    fragmentManager.findFragmentByTag(fragmentTag);
                if (null != prevInstance) {
                    fragmentTx.remove(prevInstance);
                }
                fragmentTx.addToBackStack(null);

                // "show" will commit the transaction as well
                saltKeyActionsDialog.show(fragmentTx,
                                          fragmentTag);

                // The click was handled, so return true
                return true;
            }
        });
    }

    /**
     * @summary 
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
     * @summary 
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

    /**
     * @brief A handle to the SharedPreferences for the application.
     */
    private SharedPreferences   m_sharedPreferences;

}
