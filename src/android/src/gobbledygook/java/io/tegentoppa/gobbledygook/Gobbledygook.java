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
import io.tengentoppa.yggdrasil.AboutFragment;
import io.tengentoppa.yggdrasil.HomeFragment;
import io.tengentoppa.yggdrasil.PrefsFragment;
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.WorkhorseFragment;
import io.tengentoppa.yggdrasil.Yggdrasil;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * @brief  The Gobbledygook class
 *         This class extends the Yggdrasil class to provide
 *         an implementation of the main activity for the application.
 */
public class Gobbledygook extends Yggdrasil {

    // ====================================================================
    // PUBLIC METHODS

    // --------------------------------------------------------------------
    // ACTION BAR

    /**
     * @brief   Called when an action bar menu item is selected
     * @return  Returns true on success and false on failure
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The Action Bar home/up button should open/close the drawer;
        // ActionBarDrawerToggle handles that
        if (m_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()) {
            case R.id.home:
                this.onActivitySelection(R.id.drawerHome);
                return true;
            case R.id.settings:
                this.onActivitySelection(R.id.drawerSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Set the text in the navigation drawer header.
     * @return  Does not return a value
     */
    protected void setNavigationDrawerHeader() {
        View drawerHeaderView = m_drawerView.getHeaderView(0);
        TextView drawerHeader = (TextView)drawerHeaderView.findViewById(
                R.id.drawerHeaderText);
        drawerHeader.setText(R.string.drawer_header_text,
                             TextView.BufferType.NORMAL);
        drawerHeader.setTypeface(null, Typeface.BOLD);
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     * @return  Does not return a value
     */
    protected void onActivitySelection(int itemId) {
        Log.i(LOG_CATEGORY, "onActivitySelection(): " +
              "Selecting activity id=" + itemId);

        switch(itemId) {
            case R.id.drawerHome:
                // Home fragment
                this.swapFragment(new GobbledygookHomeFragment(),
                                  getString(R.string.tag_homeFragment));
                break;
            case R.id.drawerSettings:
                // Settings
                this.swapFragment(new PrefsFragment(),
                                  getString(R.string.tag_prefsFragment));
                break;
            case R.id.drawerSettingsExport:
                // Export settings
                this.exportSettings();
                break;
            case R.id.drawerSettingsImport:
                // Import settings
                this.importSettings();
                break;
            case R.id.drawerAbout:
                // About
                this.swapFragment(new AboutFragment(),
                                  getString(R.string.tag_aboutFragment));
                break;
            default:
                // Do nothing
        }

        // Close the navigation drawer if it is open
        if (m_drawerLayout.isDrawerOpen(m_drawerView)) {
            m_drawerLayout.closeDrawer(m_drawerView);
        }
    }
}