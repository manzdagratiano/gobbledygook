/**
 * @file        Krunch.java
 * @summary     Source file for the Krunch class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.krunch;

// Libraries
import io.tengentoppa.yggdrasil.AboutFragment;
import io.tengentoppa.yggdrasil.PrefsFragment;
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.WorkhorseFragment;
import io.tengentoppa.yggdrasil.Yggdrasil;

// Android
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @summary The Krunch class
 *          This class extends the Yggdrasil class to provide
 *          an implementation of the main activity for the application.
 */
public class Krunch extends Yggdrasil {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Call the method from the base class.
        super.onCreate(savedInstanceState);

        // If there is no saved state, launch the "home" fragment.
        if (null == savedInstanceState) {
            this.onActivitySelection(R.id.drawerHome);
        }
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @summary Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

    /**
     * @summary A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     * @return  Does not return a value
     */
    protected void onActivitySelection(int itemId) {
        final String FUNC =  "onActivitySelection(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Selecting activtity id=" + itemId);

        switch(itemId) {
            case R.id.drawerHome:
                // Home fragment
                this.swapFragment(
                        new KrunchWorkhorseFragment(),
                        getString(R.string.tag_workhorseFragment));
                break;
            case R.id.drawerSettings:
                // Settings
                this.swapFragment(
                        new KrunchPrefsFragmentContainer(),
                        getString(R.string.tag_prefsFragmentContainer));
                break;
            case R.id.drawerAbout:
                // About
                this.swapFragment(
                        new KrunchAboutFragmentContainer(),
                        getString(R.string.tag_aboutFragmentContainer));
                break;
            default:
                // One of the other actions specified;
                // call the "super" method.
                super.onActivitySelection(itemId);
        }

        // Close the navigation drawer if it is open
        if (m_drawerLayout.isDrawerOpen(m_drawerView)) {
            m_drawerLayout.closeDrawer(m_drawerView);
        }
    }

}
