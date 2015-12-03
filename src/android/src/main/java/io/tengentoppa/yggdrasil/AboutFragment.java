/**
 * @file        AboutFragment.java
 * @brief       Source file for the AboutFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 23, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @brief   The AboutFragment class.
 *          This class is derived from a PreferenceFragment
 *          with each of the preferences unmodifiable.
 */
public class AboutFragment extends PreferenceFragment {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   Called when the fragment is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "onCreate(): Creating AboutFragment...");

        super.onCreate(savedInstanceState);

        // Load the preferences from the xml resource
        addPreferencesFromResource(R.xml.about);
    }

    /**
     * @brief   Called after onCreate()
     * @return  Does not return a value
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * @brief   Called after onCreate() (and onStart(),
     *          when the activity begins interacting with the user)
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        Log.i(LOG_CATEGORY, "onResume(): Configuring elements...");

        super.onResume();

        // Configure elements
        this.configureElements();
    }

    /**
     * @brief   Called when the activity is partially covered by another.
     *          Perform any cleanup here - symmetric to onResume
     * @return  Does not return a value
     */
    @Override
    public void onPause() {
        // Perform any cleanup here
        Log.i(LOG_CATEGORY, "onPause(): Deconfiguring elements...");

        this.deconfigureElements();

        super.onPause();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY            = "YGGDRASIL.ABOUT";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   
     * @return  Does not return a value
     */
    private void configureElements() {

        /**
         * @brief   A private class to configure individual view elements
         */
        class Configurator {

            /**
             * @brief   Method to configure the "Home Page" preference.
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
             * @brief   Method to configure the "Build Version" preference
             * @return  Does not return a value
             */
            public void configureVersionPreference() {
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
                    Log.e(LOG_CATEGORY, "configureElements(): Caught " + e);
                    e.printStackTrace();
                }
                versionPref.setSummary(versionName);
            }

        };  // end class Configurator

        Configurator configurator = new Configurator();
        configurator.configureHomepagePreference();
        configurator.configureVersionPreference();

    }


    /**
     * @brief   Method to do clean up
     * @return  Does not return a value
     */
    private void deconfigureElements() {
        // Nothing to do
    }

}
