/**
 * @file        Gobbledygook.java
 * @summary     Source file for the Gobbledygook class
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
 * @summary The Gobbledygook class
 *          This class extends the Yggdrasil class to provide
 *          an implementation of the main activity for the application.
 */
public class Gobbledygook extends Yggdrasil {

    // ====================================================================
    // PRIVATE METHODS

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
     *          This method may be overridden in the concrete derived classes.
     * @return  Does not return a value.
     */
    @Override
    protected void onActivitySelection(final int itemId,
                                       final String intentData) {
        final String FUNC =  "onActivitySelection(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Selecting activity id=" + itemId);

        switch(itemId) {
            case R.id.drawerHome:
                // Home fragment
                this.swapFragment(
                        new GobbledygookHomeFragment(),
                        getString(R.string.tag_homeFragment));
                break;
            case R.id.drawerSettings:
                // Settings
                this.swapFragment(
                        new GobbledygookPrefsFragmentContainer(),
                        getString(R.string.tag_prefsFragmentContainer));
                break;
            case R.id.drawerAbout:
                // About
                this.swapFragment(
                        new GobbledygookAboutFragmentContainer(),
                        getString(R.string.tag_aboutFragmentContainer));
                break;
            case R.id.workhorseFragment:
                // Workhorse.
                // If launched directly, this is due to
                // an intent from a browser.
                // The Workhorse fragment should be swapped in as a
                // "regular" fragment (i.e., not as a dialog).
                // For this selection, the "intentData",
                // which will be the "url" should not be null.
                assert (null != intentData) : "Null url supplied from intent!";
                this.swapFragment(
                        GobbledygookWorkhorseFragment.newInstance(intentData,
                                                                  false),
                        getString(R.string.tag_workhorseFragment));
                break;
            default:
                // One of the other actions specified;
                // call the "super" method.
                super.onActivitySelection(itemId,
                                          intentData);
        }

        // Close the navigation drawer if it is open
        if (m_drawerLayout.isDrawerOpen(m_drawerView)) {
            m_drawerLayout.closeDrawer(m_drawerView);
        }
    }

}
