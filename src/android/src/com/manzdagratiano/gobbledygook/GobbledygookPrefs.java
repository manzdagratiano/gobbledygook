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
import android.content.SharedPreferences;
import android.os.Bundle;

// --------------------------------------------------------------------------

/**
 * @brief   
 */
public class GobbledygookPrefs extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the PrefsFragment as the main content
        getFragmentManager(
                ).beginTransaction(
                    ).replace(android.R.id.content,
                              new GobbledygookPrefsFragment()).commit();
    }

}
