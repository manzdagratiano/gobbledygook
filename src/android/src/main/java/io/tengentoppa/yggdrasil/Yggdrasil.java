/**
 * @file        Yggdrasil.java
 * @brief       Source file for the Yggdrasil class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @brief  The Yggdrasil class.
 *         This is the only "Activity" in the package.
 *         It implements a navigation drawer with a frame layout.
 *         All other "activities" in the app have been implemented
 *         as "Fragments"; navigating to a different activity within
 *         the app is equivalent to replacing the contents of
 *         the frame layout with the relevant fragment.
 *         This class extends "AppCompatActivity" from support-v7
 *         as opposed to "Activity", so that it may use the "Toolbar"
 *         class as the AppBar as opposed to the "ActionBar",
 *         guaranteeing a consistent performance across devices.
 *         This class is abstract;
 *         this so that it can provide a generic implementation for the
 *         navigation drawer without reference to specific items.
 *         The appropriate subclasses will implement item-list-specific
 *         behavior.
 */
public abstract class Yggdrasil extends AppCompatActivity
{
    // ====================================================================
    // PUBLIC METHODS

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // Set the Toolbar as the ActionBar
        Toolbar appBar = (Toolbar)findViewById(R.id.appBar);
        setSupportActionBar(appBar);

        // Create the navigation drawer
        this.createNavigationDrawer();

        // If there is no saved state,
        // launch the "home" fragment
        // (guaranteed to be at position 0 in the drawer)
        if (null == savedInstanceState) {
            this.onActivitySelection(0);
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
        getSupportActionBar().setTitle(title);
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
        // Hide the actions present in the navigation drawer
        // if the drawer is open
        menu.findItem(R.id.home).setVisible(!isDrawerOpen);
        menu.findItem(R.id.settings).setVisible(!isDrawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * @brief   Called when an action bar menu item is selected
     *          This method will be appropriately overridden by the subclass.
     *          It needs to exist here for the proper "super" handling
     *          of this override through the Activity inheritance tree.
     *          (The subclass calls its "super" method, and this class
     *          calls its own in turn, calling the one from "Activity")
     * @return  Returns true on success and false on failure
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // --------------------------------------------------------------------
    // HANDLERS

    /**
     * @brief   Called when a spawned activity returns
     *          This method will be appropriately overridden by the subclass.
     *          It needs to exist here for the proper "super" handling
     *          of this override through the Activity inheritance tree.
     * @return  Does not return a value
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    // ====================================================================
    // INTERFACES

    // An interface for the items in the navigation drawer
    // It will be implemented by an enum in the subclass
    protected interface DrawerItem {
    }

    // ====================================================================
    // PROTECTED METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    protected static final String LOG_CATEGORY                            =
        "YGGDRASIL";

    // Output file constants
    private static final String OUTPUT_DIRECTORY_NAME                     =
        "gobbledygook";
    private static final String OUTPUT_PREFERENCES_FILENAME               =
        "gobbledygook.json";
    private static final int JSON_INDENT_FACTOR                           =
        8;

    // Toast messages
    protected static final String DOPPELGANGER_FILE_ERROR                 =
        "ERROR exporting settings! " +
        "A file by the hijacked name 'gobbledygook' " +
        "already exists in the Documents folder :( " +
        "Please remove/rename it to continue";
    protected static final String EXPORT_SETTINGS_MESSAGE                 =
        "Successfully exported settings to file " +
        OUTPUT_PREFERENCES_FILENAME + " in the Documents/" +
        OUTPUT_DIRECTORY_NAME + " folder";
    protected static final String EXPORT_SETTINGS_ERROR                   =
        "ERROR exporting settings to file :(";
    protected static final String EXTERNAL_STORAGE_ERROR                  =
        "ERROR exporting settings! The external storage is not mounted :(";

    // Request codes for spawning activities
    protected static final int    READ_PREFERENCES_FILE_CODE              =
        8086;

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Routine to create the map (id, string) of items
     *          in the navigation drawer
     * @return  Does not return a value
     */
    protected void createDrawerMap() {
        // Do nothing;
        // The subclass will override this method appropriately
    }

    /**
     * @brief   
     * @return  
     */
    protected void createNavigationDrawer() {
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

        // Create an Array of items from m_drawerMap
        ArrayList<NavigationDrawerItem> drawerItems =
            new ArrayList<NavigationDrawerItem>(m_drawerMap.values());

        // Set the adapter for the list view
        m_drawerView.setAdapter(new NavigationDrawerListAdapter(
                                            this.getApplicationContext(),
                                            drawerItems));

        // Set the action bar app icon to behave as drawer toggler
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set the onClickListener for the ListView
        m_drawerView.setOnItemClickListener(
                new NavigationItemClickListener());

        // Set drawer toggle interactions to play well with the action bar
        m_drawerToggle = new ActionBarDrawerToggle(
                                    this,
                                    m_drawerLayout,
                                    R.drawable.ic_drawer,
                                    R.string.drawer_open,
                                    R.string.drawer_closed) {

            /**
             * @brief   
             * @return  Does not return a value
             */
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(m_title);
                // Create call to onPrepareOptionsMenu
                invalidateOptionsMenu();
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(m_title);
                // Create call to onPrepareOptionsMenu
                invalidateOptionsMenu();
            }
        };
        m_drawerLayout.setDrawerListener(m_drawerToggle);
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     * @return  Does not return a value
     */
    protected void onActivitySelection(int position) {
        // Do nothing
        // The subclass will override this method appropriately
    }

    /**
     * @brief   Function to swap in a new fragment into
     *          the frameLayout of the Navigation Drawer
     * @return  Does not return a value
     */
    protected void swapFragment(Fragment fragment,
                                final String fragmentTag) {
        assert (null != fragment) : "Asked to swap in null fragment!!!";
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTx = fragmentManager.beginTransaction();
        fragmentTx.replace(R.id.contentFrame,
                           fragment,
                           fragmentTag);
        // Provide proper "back" navigation
        fragmentTx.addToBackStack(null);
        fragmentTx.setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTx.commit();
    }

    /**
     * @brief   Function to export preferences to a JSON file
     * @return  Does not return a value
     */
    protected void exportPreferences() {
        Log.i(LOG_CATEGORY, "exportPreferences() handler called...");

        // Create a JSON object from the SharedPreferences
        JSONObject outputPrefs = new JSONObject();
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    this.getApplicationContext());

        try {
            // Salt key; blank on empty retrieval
            outputPrefs.put(
                    getString(R.string.pref_saltKey_key),
                    sharedPrefs.getString(
                        getString(R.string.pref_saltKey_key),
                        ""));
            // Default iterations; the iterations "hint"
            // (which was the default value) on empty retrieval
            outputPrefs.put(
                    getString(R.string.pref_defaultIterations_key),
                    sharedPrefs.getString(
                        getString(R.string.pref_defaultIterations_key),
                        getString(R.string.hint_iterations)));
            // Custom website attribute list; blank on empty retrieval
            outputPrefs.put(
                    getString(R.string.pref_siteAttributesList_key),
                    sharedPrefs.getString(
                        getString(R.string.pref_siteAttributesList_key),
                        ""));
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                  "Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Log.i(LOG_CATEGORY, "exportPreferences(): " +
              "outputPrefs='" + outputPrefs.toString() + "'");

        // Obtain a file handle for output in the external storage;
        // this is necessary since the user must be able to access
        // the output file
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                  "External.Storage.NOT_MOUNTED");
            Toast.makeText(this.getApplicationContext(),
                           EXTERNAL_STORAGE_ERROR,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the directory for output
        File outputDir =
            new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS),
                     OUTPUT_DIRECTORY_NAME);

        // Check if it already exists, and if it does, is a directory;
        // create it if it does not exist
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                      "Directory.Creation.Failure");
                Toast.makeText(this.getApplicationContext(),
                               EXPORT_SETTINGS_ERROR,
                               Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (!outputDir.isDirectory()) {
            Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                  "File.Exists.InPlaceOf.Directory");
            Toast.makeText(this.getApplicationContext(),
                           DOPPELGANGER_FILE_ERROR,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        // Write the JSON object to the file
        FileOutputStream outputStream = null;
        try {
            File outputFile = new File(outputDir,
                                       OUTPUT_PREFERENCES_FILENAME);
            outputStream = new FileOutputStream(outputFile);
            outputStream.write(
                    outputPrefs.toString(JSON_INDENT_FACTOR).getBytes());
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                  "Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        } catch (IOException e) {
            Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                  "Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(LOG_CATEGORY, "exportPreferences() ERROR: " +
                      "Memory Leak! Could not close FileOutputStream. " +
                      "Caught " + e);
                e.printStackTrace();
                // No need to return
            }
        }

        Toast.makeText(this.getApplicationContext(),
                       EXPORT_SETTINGS_MESSAGE,
                       Toast.LENGTH_SHORT).show();
    }

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @brief   Inner class NavigationItemClickListener.
     *          Being an inner class, it has direct access to the methods
     *          of the outer enclosing class.
     */
    protected class NavigationItemClickListener
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

    protected DrawerLayout          m_drawerLayout; /**
                                                     * @brief The drawer
                                                     * layout
                                                     */
    protected ListView              m_drawerView;   /**
                                                     * @brief The drawer
                                                     * list view
                                                     */
    protected TreeMap<Integer, NavigationDrawerItem>
                                    m_drawerMap;    /**
                                                     * @brief A map of
                                                     * (id, names) of items
                                                     * in the drawer.
                                                     * Since it's a TreeMap,
                                                     * the items herein are
                                                     * sorted by the ids.
                                                     */
    protected ActionBarDrawerToggle m_drawerToggle; /**
                                                     * @brief The toggle
                                                     * action for the
                                                     * drawer
                                                     */
    protected CharSequence          m_title;        /**
                                                     * @brief The activity
                                                     * title
                                                     */
}
