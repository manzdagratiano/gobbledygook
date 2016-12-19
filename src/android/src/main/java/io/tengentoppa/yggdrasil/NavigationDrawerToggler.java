/**
 * @file        NavigationDrawerToggler.java
 * @summary     Source file for the NavigationDrawerToggler class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * @summary The NavigationDrawerToggler class.
 */
public class NavigationDrawerToggler {

    /**
     * @summary Method to configure the toggle behavior for the
     *          navigation drawer in the activity for the Toolbar
     *          in the specific fragment this is invoked from.
     * @return  Does not even.
     */
    public static void configureToggler(final AppCompatActivity activity,
                                        final Toolbar appBar) {
        // Set the Toolbar as the App Bar
        // (as well as the ActionBar through the "Support" framework).
        activity.setSupportActionBar(appBar);
        // Now that the action bar has been set,
        // set the action bar app icon to behave as drawer toggler.
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setIcon(R.drawable.ic_launcher);

        // Set navigation drawer toggle interactions
        // to play well with the action bar.
        DrawerLayout drawerLayout =
            (DrawerLayout)activity.findViewById(R.id.navigationDrawerLayout);
        ActionBarDrawerToggle drawerToggle =
            new ActionBarDrawerToggle(activity,
                                      drawerLayout,
                                      appBar,
                                      R.string.drawer_action_open,
                                      R.string.drawer_action_closed) {

            /**
             * @summary The drawer closed handler.
             * @return  Does not even.
             */
            public void onDrawerClosed(View drawerView) {
                activity.getSupportActionBar().setTitle(activity.getTitle());
                // Create call to onPrepareOptionsMenu
                activity.supportInvalidateOptionsMenu();
            }

            /**
             * @summary The drawer opened handler.
             * @return  Does not even.
             */
            public void onDrawerOpened(View drawerView) {
                activity.getSupportActionBar().setTitle(activity.getTitle());
                // Create call to onPrepareOptionsMenu
                activity.supportInvalidateOptionsMenu();
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set the handle to the drawerToggle in the activity.
        if (activity instanceof Yggdrasil) {
            ((Yggdrasil)activity).setDrawerToggler(drawerToggle);
        }
    }

}
