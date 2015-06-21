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

package com.manzdagratiano.gobbledygook;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

// Standard Java
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @brief  The Gobbledygook class.
 *         This is the only "Activity" in the package.
 *         It implements a navigation drawer with a frame layout.
 *         All other "activities" in the app have been implemented
 *         as "Fragments"; navigating to a different activity within
 *         the app is equivalent to replacing the contents of
 *         the frame layout with the relevant fragment.
 */
public class Gobbledygook extends Activity {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   Called when the activity is created.
     *          *** NOTE ***
     *          A screen rotation is also an activity re-creation.
     *          The "state" is saved, and then reloaded on screen rotation.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "onCreate(): Creating main activity...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigation_drawer);

        // Create the navigation drawer
        this.createNavigationDrawer();

        // If there is no saved state,
        // launch the "main" activity (at position 0 in the drawer)
        if (null == savedInstanceState) {
            onActivitySelection(0);
        }
    }

    /**
     * @brief   ActionBarDrawerToggle must have its state synced in
     *          "onPostCreate"
     * @return  Does not return a value
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY,
              "onPostCreate(): Configuring ActionBarDrawerToggle...");
        super.onPostCreate(savedInstanceState);
        // Sync the drawer toggle state after onRestoreInstanceState
        // has been called
        m_drawerToggle.syncState();
    }

    /**
     * @brief   ActionBarDrawerToggle must be passed
     *          any configuration changes
     * @return  Does not return a value
     */
    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        m_drawerToggle.onConfigurationChanged(newConfiguration);
    }

    /**
     * @brief   
     * @return  Does not return a value
     */
    @Override
    public void setTitle(CharSequence title) {
        m_title = title;
        getActionBar().setTitle(title);
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
        super.onResume();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onPause() {
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

    // --------------------------------------------------------------------
    // ACTION BAR

    // Since this is the only activity, this is the only place where
    // the action bar handlers will be defined.
    // None of the fragments need to revisit these definitions.

    /**
     * @brief   Called to populate the action bar menu, if it is present
     * @return  Returns true on success and false on failure
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.gobbledygook_actions,
                                  menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @brief   Function called whenever "invalidateOptionsMenu" is invoked
     * @return  Returns true on success and false on failure
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isDrawerOpen = m_drawerLayout.isDrawerOpen(m_drawerView);
        // Hide the "salt key" action if the drawer is open,
        // since the drawer also provides navigation to that activity
        menu.findItem(R.id.saltKeyActions).setVisible(!isDrawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

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
            case R.id.saltKeyActions:
                this.onActivitySelection(1);
                return true;
            case R.id.settings:
                this.onActivitySelection(4);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY                              =
        "GOBBLEDYGOOK";

    // Fragment tags
    private static final String GOBBLEDYGOOK_MAIN_FRAGMENT_TAG            =
        "GobbledygookMainFragment";
    private static final String GOBBLEDYGOOK_SALTKEY_ACTIONS_FRAGMENT_TAG =
        "GobbledygookSaltKeyFragment";
    private static final String GOBBLEDYGOOK_PREFERENCES_FRAGMENT_TAG     =
        "GobbledygookPreferencesFragment";
    private static final String GOBBLEDYGOOK_ABOUT_FRAGMENT_TAG           =
        "GobbledygookAboutFragment";

    // An enum for the items in the navigation drawer
    private enum DrawerItem {
        HOME,
        SALTKEY_ACTIONS,
        IMPORT_SETTINGS,
        EXPORT_SETTINGS,
        SETTINGS,
        ABOUT
    }

    // Toast messages
    private static final String EXPORT_SETTINGS_MESSAGE                   =
        "Successfully exported settings to file...";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Routine to create the map (id, string) of items
     *          in the navigation drawer
     * @return  Does not return a value
     */
    private void createDrawerMap() {
        Log.i(LOG_CATEGORY, "createDrawerMap() :" +
              "Creating the (id, string) map of items in the drawer...");

        m_drawerMap = new TreeMap<Integer, String>();
        m_drawerMap.put(DrawerItem.HOME.ordinal(),
                        "Home");
        m_drawerMap.put(DrawerItem.SALTKEY_ACTIONS.ordinal(),
                        "Salt Key Actions");
        m_drawerMap.put(DrawerItem.IMPORT_SETTINGS.ordinal(),
                        "Import Settings...");
        m_drawerMap.put(DrawerItem.EXPORT_SETTINGS.ordinal(),
                        "Export Settings...");
        m_drawerMap.put(DrawerItem.SETTINGS.ordinal(),
                        "Settings");
        m_drawerMap.put(DrawerItem.ABOUT.ordinal(),
                        "About...");
    }

    /**
     * @brief   
     * @return  
     */
    private void createNavigationDrawer() {
        Log.i(LOG_CATEGORY, "createNavigationDrawer() :" +
              "Configuring the navigation drawer...");

        // Create the map of (id, name) of items in the drawer
        this.createDrawerMap();

        m_drawerLayout =
            (DrawerLayout)findViewById(R.id.navigationDrawerLayout);
        m_drawerView =
            (ListView)m_drawerLayout.findViewById(R.id.navigationDrawer);

        // Get the title
        m_title = getTitle();

        // Set the adapter for the list view
        ArrayList<String> drawerItemList =
            new ArrayList<String>(m_drawerMap.values());
        m_drawerView.setAdapter(new ArrayAdapter<String>(
                                        this,
                                        R.layout.navigation_drawer_item,
                                        drawerItemList));

        // Set the action bar app icon to behave as drawer toggler
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Set the onClickListener for the ListView
        m_drawerView.setOnItemClickListener(
                new NavigationItemClickListener());

        // Set drawer toggle interactions to play well with the action bar
        m_drawerToggle = new ActionBarDrawerToggle(
                                this,
                                m_drawerLayout,
                                R.drawable.ic_menu_white,
                                R.string.drawer_open,
                                R.string.drawer_closed) {

            /**
             * @brief   
             * @return  Does not return a value
             */
            public void onDrawerClosed(View drawerView) {
                getActionBar().setTitle(m_title);
                // Create call to onPrepareOptionsMenu
                invalidateOptionsMenu();
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(m_title);
                // Create call to onPrepareOptionsMenu
                invalidateOptionsMenu();
            }
        };
        m_drawerLayout.setDrawerListener(m_drawerToggle);
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     * @return  Does not return a value
     */
    private void onActivitySelection(int position) {
        Log.i(LOG_CATEGORY, "onActivitySelection() :" +
              "Selecting activtity at position=" + position);

        // Create a const copy of the enum values
        // for use in the switch-case statement
        final DrawerItem[] items = DrawerItem.values();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTx = null;
        switch(items[position]) {
            case HOME:
                // Main activity
                fragmentTx = fragmentManager.beginTransaction();
                fragmentTx.replace(R.id.contentFrame,
                                   new GobbledygookMainFragment(),
                                   GOBBLEDYGOOK_MAIN_FRAGMENT_TAG);
                fragmentTx.commit();
                break;
            case SALTKEY_ACTIONS:
                // Salt Key Actions
                fragmentTx = fragmentManager.beginTransaction();
                fragmentTx.replace(R.id.contentFrame,
                                   new GobbledygookSaltKeyActionsFragment(),
                                   GOBBLEDYGOOK_SALTKEY_ACTIONS_FRAGMENT_TAG);
                fragmentTx.commit();
                break;
            case IMPORT_SETTINGS:
                // Import Settings
                break;
            case EXPORT_SETTINGS:
                // Export settings
                Toast.makeText(getApplicationContext(),
                               EXPORT_SETTINGS_MESSAGE,
                               Toast.LENGTH_SHORT).show();
                break;
            case SETTINGS:
                // Settings
                fragmentTx = fragmentManager.beginTransaction();
                fragmentTx.replace(R.id.contentFrame,
                                   new GobbledygookPrefsFragment(),
                                   GOBBLEDYGOOK_PREFERENCES_FRAGMENT_TAG);
                fragmentTx.commit();
                break;
            case ABOUT:
                break;
            default:
                // Do nothing
        }

        // Close the navigation drawer if it is open
        if (m_drawerLayout.isDrawerOpen(m_drawerView)) {
            m_drawerLayout.closeDrawer(m_drawerView);
        }
    }

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @brief   Inner class NavigationItemClickListener.
     *          Being an inner class, it has direct access to the methods
     *          of the outer enclosing class.
     */
    private class NavigationItemClickListener
            implements ListView.OnItemClickListener {

        /**
         * @brief   
         * @return  
         */
        @Override
        public void onItemClick(AdapterView parent,
                                View view,
                                int position,
                                long id) {
            // Call method from outer class
            onActivitySelection(position);
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS

    private DrawerLayout            m_drawerLayout; /**
                                                     * @brief The drawer
                                                     * layout
                                                     */
    private ListView                m_drawerView;   /**
                                                     * @brief The drawer
                                                     * list view
                                                     */
    private TreeMap<Integer, String>m_drawerMap;    /**
                                                     * @brief A map of
                                                     * (id, names) of items
                                                     * in the drawer.
                                                     * Since it's a TreeMap,
                                                     * the items herein are
                                                     * sorted by the ids.
                                                     */
    private ActionBarDrawerToggle   m_drawerToggle; /**
                                                     * @brief The toggle
                                                     * action for the
                                                     * drawer
                                                     */
    private CharSequence            m_title;        /**
                                                     * @brief The activity
                                                     * title
                                                     */
}
