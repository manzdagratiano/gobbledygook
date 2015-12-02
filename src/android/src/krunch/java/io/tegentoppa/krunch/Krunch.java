/**
 * @file        Krunch.java
 * @brief       Source file for the Krunch class
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
import io.tengentoppa.yggdrasil.NavigationDrawerItem;
import io.tengentoppa.yggdrasil.PrefsFragment;
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.WorkhorseFragment;
import io.tengentoppa.yggdrasil.Yggdrasil;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

// Standard Java
import java.util.TreeMap;

/**
 * @brief  The Krunch class
 *         This class extends the Yggdrasil class to provide
 *         an implementation of the main activity for the application.
 */
public class Krunch extends Yggdrasil {

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

        Intent intent = null;
        switch(item.getItemId()) {
            case R.id.home:
                this.onActivitySelection(
                        KrunchDrawerItem.HOME.ordinal());
                return true;
            case R.id.settings:
                this.onActivitySelection(
                        KrunchDrawerItem.SETTINGS.ordinal());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    // Drawer Items
    protected enum KrunchDrawerItem implements DrawerItem {
        HOME,
        EXPORT_SETTINGS,
        SETTINGS,
        ABOUT
    }

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Routine to create the map (id, string) of items
     *          in the navigation drawer
     * @return  Does not return a value
     */
    protected void createDrawerMap() {
        Log.i(LOG_CATEGORY, "createDrawerMap() :" +
              "Creating the (id, NavigationDrawerItem) map...");

        m_drawerMap = new TreeMap<Integer, NavigationDrawerItem>();
        m_drawerMap.put(
                KrunchDrawerItem.HOME.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_menu_home,
                                         "Home"));
        m_drawerMap.put(
                KrunchDrawerItem.EXPORT_SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_upload,
                                         "Export Settings..."));
        m_drawerMap.put(
                KrunchDrawerItem.SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_settings,
                                         "Settings"));
        m_drawerMap.put(
                KrunchDrawerItem.ABOUT.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_about,
                                         "About..."));
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     * @return  Does not return a value
     */
    protected void onActivitySelection(int position) {
        Log.i(LOG_CATEGORY, "onActivitySelection(): " +
              "Selecting activtity at position=" + position);

        // Create a const copy of the enum values
        // for use in the switch-case statement
        final KrunchDrawerItem[] items =
            KrunchDrawerItem.values();
        switch(items[position]) {
            case HOME:
                // Main activity
                this.swapFragment(new KrunchWorkhorseFragment(),
                                  getString(R.string.tag_workhorseFragment));
                break;
            case EXPORT_SETTINGS:
                // Export settings
                this.exportPreferences();
                break;
            case SETTINGS:
                // Settings
                this.swapFragment(new PrefsFragment(),
                                  getString(R.string.tag_prefsFragment));
                break;
            case ABOUT:
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
