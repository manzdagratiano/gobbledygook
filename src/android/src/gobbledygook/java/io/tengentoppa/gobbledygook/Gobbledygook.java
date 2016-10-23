/**
 * @file        Gobbledygook.java
 * @brief       Source file for the Gobbledygook class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.gobbledygook;

// Libraries
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.Yggdrasil;

// Android
import android.os.Bundle;
import android.util.Log;

/**
 * @brief  The Gobbledygook class
 *         This class extends the Yggdrasil class to provide
 *         an implementation of the main activity for the application.
 */
public class Gobbledygook extends Yggdrasil {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Select the default state
        // If there is no saved state, launch the "home" fragment.
        if (null == savedInstanceState) {
                this.onActivitySelection(R.id.drawerHome);
        }
    }

    // ====================================================================
    // PRIVATE METHODS

    /**
     * @brief   Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     *          This method may be overridden in the concrete derived classes.
     * @return  Does not return a value.
     */
    @Override
    protected void onActivitySelection(int itemId) {
        final String FUNC =  "onActivitySelection(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Selecting activity id=" + itemId);

        switch(itemId) {
            case R.id.drawerHome:
                // Home fragment
                this.swapFragment(new GobbledygookHomeFragment(),
                                  getString(R.string.tag_homeFragment));
                break;
            case R.id.drawerSettings:
                // Settings
                this.swapFragment(new GobbledygookPrefsFragment(),
                                  getString(R.string.tag_prefsFragment));
                break;
            case R.id.drawerAbout:
                // About
                this.swapFragment(new GobbledygookAboutFragment(),
                                  getString(R.string.tag_aboutFragment));
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
