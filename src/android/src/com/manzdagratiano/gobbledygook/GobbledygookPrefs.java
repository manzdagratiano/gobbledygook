/**
 * @file        GobbledygookPrefs.java
 * @brief       Source file for the GobbledygookPrefs Activity
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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

// ------------------------------------------------------------------------

/**
 * @brief   
 */
public class GobbledygookPrefs extends Activity {

    // ====================================================================
    // PUBLIC MEMBERS

    // --------------------------------------------------------------------
    // CONSTANTS

    public static final int READ_SALT_KEY_FILE  = 666;

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the PrefsFragment as the main content
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTx = fragmentManager.beginTransaction();
        fragmentTx.replace(android.R.id.content,
                           new GobbledygookPrefsFragment(),
                           FRAGMENT_TAG);
        fragmentTx.commit();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        Log.i(LOG_CATEGORY, "onActivityResult() handler called...");

        // Handle the result from the file picker
        // for the Salt Key preference, if this was called due to that
        if (requestCode == READ_SALT_KEY_FILE &&
            resultCode == Activity.RESULT_OK) {
            FragmentManager fragmentManager = getFragmentManager();
            GobbledygookPrefsFragment preferenceFragment =
                (GobbledygookPrefsFragment)fragmentManager.findFragmentByTag(
                        FRAGMENT_TAG);
            if (null == preferenceFragment) {
                Log.e(LOG_CATEGORY, "onActivityResult(): " +
                        "FATAL: Could not retrieve " + FRAGMENT_TAG);
            }

            Log.i(LOG_CATEGORY, "onActivityResult(): " +
                    "Calling PrefsFragment fileSelectionHandler...");
            preferenceFragment.handleFileSelection(resultData.getData());
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY    = "GOBBLEDYGOOK.PREFS";
    private static final String FRAGMENT_TAG    = "GobbledygookPrefsFragment";

}
