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

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY    = "GOBBLEDYGOOK.PREFS";
    private static final String FRAGMENT_TAG    = "GobbledygookPrefsFragment";

}
