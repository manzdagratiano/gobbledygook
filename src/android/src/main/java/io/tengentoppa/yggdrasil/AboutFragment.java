/**
 * @file        AboutFragment.java
 * @summary     Source file for the AboutFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 23, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.InputType;
import android.util.Log;

/**
 * @summary The AboutFragment class.
 *          This class is derived from a PreferenceFragment
 *          with each of the preferences unmodifiable.
 */
public abstract class AboutFragment extends PreferenceFragmentCompat {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @summary Called when the preference hierarchy is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {
        final String FUNC = "onCreatePreferences()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating AboutFragment...");

        // Load the preferences from the xml resource
        setPreferencesFromResource(R.xml.list_about,
                                   rootKey);
    }

    /**
     * @summary Called after onCreate() (and onStart(),
     *          when the activity begins interacting with the user)
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        final String FUNC = "onResume()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring elements...");

        super.onResume();

        // Configure elements
        this.configureElements();
    }

    /**
     * @summary Called when the activity is partially covered by another.
     *          Perform any cleanup here - symmetric to onResume
     * @return  Does not return a value
     */
    @Override
    public void onPause() {
        // Perform any cleanup here
        final String FUNC = "onPause()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Deconfiguring elements...");

        this.deconfigureElements();

        super.onPause();
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
        final String LOG_TAG = "ABOUT";
        return LOG_TAG + "." + FUNC + ": ";
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @summary 
     * @return  Does not return a value
     */
    private void configureElements() {
        final String FUNC = "configureElements(): ";

        /**
         * @summary A private class to configure individual view elements
         */
        class Configurator {

            /**
             * @summary Method to configure the "License" preference.
             *          Sets an Intent to open the License URL.
             * @return  Does not return a value
             */
            public void configureLicensePreference() {
                Preference licensePref =
                    (Preference)findPreference(
                            getString(R.string.about_license_key));
                // Set an intent to open the home page in a browser
                licensePref.setIntent(
                        new Intent(Intent.ACTION_VIEW,
                                   Uri.parse(getString(
                                        R.string.about_license_page))));
            }

            /**
             * @summary Method to configure the "Home Page" preference.
             *          Sets an Intent to open the Home page URL.
             * @return  Does not return a value
             */
            public void configureHomepagePreference() {
                Preference homepagePref =
                    (Preference)findPreference(
                            getString(R.string.about_homepage_key));
                // Set an intent to open the home page in a browser
                homepagePref.setIntent(
                        new Intent(Intent.ACTION_VIEW,
                                   Uri.parse(getString(
                                        R.string.about_homepage_summary))));
            }

            /**
             * @summary Method to configure the "Build Version" preference
             * @return  Does not return a value
             */
            public void configureVersionPreference() {
                final String FUNC = "configureVersionPreference()";
                Preference versionPref =
                    (Preference)findPreference(
                            getString(R.string.about_version_key));

                String versionName = "";
                try {
                    Context appContext = getActivity().getApplicationContext();
                    PackageInfo pkgInfo =
                        appContext.getPackageManager(
                                ).getPackageInfo(appContext.getPackageName(),
                                                 0);
                    versionName = pkgInfo.versionName;
                } catch(PackageManager.NameNotFoundException e) {
                    Log.i(getLogCategory(), getLogPrefix(FUNC) +
                          "Caught " + e);
                    e.printStackTrace();
                }
                versionPref.setSummary(versionName);
            }

        };  // end class Configurator

        Configurator configurator = new Configurator();
        configurator.configureLicensePreference();
        configurator.configureHomepagePreference();
        configurator.configureVersionPreference();

    }


    /**
     * @summary Method to do clean up
     * @return  Does not return a value
     */
    private void deconfigureElements() {
        // Nothing to do
    }

}
