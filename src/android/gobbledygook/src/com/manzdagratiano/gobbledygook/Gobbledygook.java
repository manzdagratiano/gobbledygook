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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.TreeMap;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

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
            onActivitySelection(DrawerItem.HOME.ordinal());
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
        // Hide the actions present in the navigation drawer
        // if the drawer is open
        menu.findItem(R.id.home).setVisible(!isDrawerOpen);
        menu.findItem(R.id.saltKeyActions).setVisible(!isDrawerOpen);
        menu.findItem(R.id.settings).setVisible(!isDrawerOpen);

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
            case R.id.home:
                this.onActivitySelection(
                        DrawerItem.HOME.ordinal());
                return true;
            case R.id.saltKeyActions:
                this.onActivitySelection(
                        DrawerItem.SALTKEY_ACTIONS.ordinal());
                return true;
            case R.id.settings:
                this.onActivitySelection(
                        DrawerItem.SETTINGS.ordinal());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @brief   Called when a spawned activity returns
     * @return  Does not return a value
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        Log.i(LOG_CATEGORY, "onActivityResult() handler called...");
        if (READ_PREFERENCES_FILE_CODE == requestCode &&
            Activity.RESULT_OK == resultCode) {
            Log.i(LOG_CATEGORY, "onActivityResult(): " +
                  "Calling onPreferencesFileSelection()...");
            this.onPreferencesFileSelection(resultData.getData());
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY                              =
        "GOBBLEDYGOOK";

    // Fragment tags
    private static final String GOBBLEDYGOOK_HOME_FRAGMENT_TAG            =
        "GobbledygookHomeFragment";
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

    // Output file constants
    private static final String OUTPUT_DIRECTORY_NAME                     =
        "gobbledygook";
    private static final String OUTPUT_PREFERENCES_FILENAME               =
        "gobbledygook.json";
    private static final int JSON_INDENT_FACTOR                           =
        8;

    // Toast messages
    private static final String DOPPELGANGER_FILE_ERROR                   =
        "ERROR exporting settings! " +
        "A file by the hijacked name 'gobbledygook' " +
        "already exists in the Documents folder :( " +
        "Please remove/rename it to continue";
    private static final String EXPORT_SETTINGS_MESSAGE                   =
        "Successfully exported settings to file " +
        OUTPUT_PREFERENCES_FILENAME + " in the Documents/" +
        OUTPUT_DIRECTORY_NAME + " folder";
    private static final String EXPORT_SETTINGS_ERROR                     =
        "ERROR exporting settings to file :(";
    private static final String EXTERNAL_STORAGE_ERROR                    =
        "ERROR exporting settings! The external storage is not mounted :(";
    private static final String FILE_MANAGER_MISSING_ERROR                =
        "ERROR importing file! Please install a file manager " +
        "to be able to browse to a file";
    private static final String IMPORT_SETTINGS_MESSAGE                   =
        "Successfully imported settings...";
    private static final String IMPORT_SETTINGS_ERROR                     =
        "ERROR! Found malformed file! Failed to import settings! :(";

    // Request codes for spawning activities
    private static final int    READ_PREFERENCES_FILE_CODE                =
        8086;

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Routine to create the map (id, string) of items
     *          in the navigation drawer
     * @return  Does not return a value
     */
    private void createDrawerMap() {
        Log.i(LOG_CATEGORY, "createDrawerMap() :" +
              "Creating the (id, NavigationDrawerItem) map...");

        m_drawerMap = new TreeMap<Integer, NavigationDrawerItem>();
        m_drawerMap.put(
                DrawerItem.HOME.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_menu_home,
                                         "Home"));
        m_drawerMap.put(
                DrawerItem.SALTKEY_ACTIONS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_menu_account_list,
                                         "Salt Key Actions"));
        m_drawerMap.put(
                DrawerItem.IMPORT_SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_download,
                                         "Import Settings..."));
        m_drawerMap.put(
                DrawerItem.EXPORT_SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_upload,
                                         "Export Settings..."));
        m_drawerMap.put(
                DrawerItem.SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_settings,
                                         "Settings"));
        m_drawerMap.put(
                DrawerItem.ABOUT.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_about,
                                         "About..."));
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

        // Create an Array of items from m_drawerMap
        ArrayList<NavigationDrawerItem> drawerItems =
            new ArrayList<NavigationDrawerItem>(m_drawerMap.values());

        // Set the adapter for the list view
        m_drawerView.setAdapter(new NavigationDrawerListAdapter(
                                            this.getApplicationContext(),
                                            drawerItems));

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
                                    R.drawable.ic_drawer,
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
        Log.i(LOG_CATEGORY, "onActivitySelection(): " +
              "Selecting activtity at position=" + position);

        // Create a const copy of the enum values
        // for use in the switch-case statement
        final DrawerItem[] items = DrawerItem.values();
        switch(items[position]) {
            case HOME:
                // Main activity
                this.swapFragment(new GobbledygookHomeFragment(),
                                  GOBBLEDYGOOK_HOME_FRAGMENT_TAG);
                break;
            case SALTKEY_ACTIONS:
                // Salt Key Actions
                this.swapFragment(new GobbledygookSaltKeyActionsFragment(),
                                  GOBBLEDYGOOK_SALTKEY_ACTIONS_FRAGMENT_TAG);
                break;
            case IMPORT_SETTINGS:
                this.importPreferences();
                break;
            case EXPORT_SETTINGS:
                // Export settings
                this.exportPreferences();
                break;
            case SETTINGS:
                // Settings
                this.swapFragment(new GobbledygookPrefsFragment(),
                                  GOBBLEDYGOOK_PREFERENCES_FRAGMENT_TAG);
                break;
            case ABOUT:
                // About
                this.swapFragment(new GobbledygookAboutFragment(),
                                  GOBBLEDYGOOK_ABOUT_FRAGMENT_TAG);
                break;
            default:
                // Do nothing
        }

        // Close the navigation drawer if it is open
        if (m_drawerLayout.isDrawerOpen(m_drawerView)) {
            m_drawerLayout.closeDrawer(m_drawerView);
        }
    }

    /**
     * @brief   Function to swap in a new fragment into
     *          the frameLayout of the Navigation Drawer
     * @return  Does not return a value
     */
    private void swapFragment(Fragment fragment,
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
     * @brief   Function to import preferences from a JSON file
     * @return  Does not return a value
     */
    private void importPreferences() {
        Log.i(LOG_CATEGORY, "importPreferences() handler called...");

        // Open the file picker dialog to select the key file.
        // This requires creating a new "Intent".
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to only show json files
        // TODO: This filter does not seem to have any effect with
        // the ACTION_GET_CONTENT Intent type as opposed to the
        // ACTION_OPEN_DOCUMENT intent
        intent.setType("application/json");

        // Start the activity
        Log.i(LOG_CATEGORY, "importPreferences(): Opening File Picker UI...");
        // Start the activity, but through a "chooser"
        // for available Content Providers instead of the intent directly,
        // since the user may prefer a different one each time.
        Intent fileChooser =
            intent.createChooser(intent,
                                 "Select the JSON preferences file...");
        // Check if the intent resolves to any activities,
        // and start it if it does.
        if (null != intent.resolveActivity(this.getPackageManager())) {
            startActivityForResult(fileChooser,
                                   READ_PREFERENCES_FILE_CODE);
        } else {
            Toast.makeText(this.getApplicationContext(),
                           FILE_MANAGER_MISSING_ERROR,
                           Toast.LENGTH_SHORT).show();
        }
        // The callback "onActivityResult" will be called
    }

    /**
     * @brief   
     * @return  
     */
    private void onPreferencesFileSelection(Uri uri) {
        Log.i(LOG_CATEGORY, "onPreferencesFileSelection(): " +
              "Parsing JSON file, uri='" + uri.toString() + "'");

        // Read the uri into a string
        InputStream inputStream = null;
        String line = null;
        BufferedReader bufferedFileReader = null;
        StringBuilder preferencesFileBuffer = new StringBuilder();
        try {
            inputStream =
                this.getContentResolver().openInputStream(uri);
            bufferedFileReader =
                new BufferedReader(new InputStreamReader(inputStream));
            while (null != (line = bufferedFileReader.readLine())) {
                preferencesFileBuffer.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           IMPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        } finally {
            if (null != bufferedFileReader) {
                try {
                    bufferedFileReader.close();
                } catch (IOException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Memory Leak! " +
                          "Couldn't close BufferedReader; " +
                          "uri='" + uri.toString() + "', Caught " + e);
                    e.printStackTrace();
                    // No need to return empty-handed here
                }
            }
        }

        // Parse the JSON string and set the preferences
        try {
            JSONObject inputPrefs =
                new JSONObject(preferencesFileBuffer.toString());

            // Get a handle to the default shared preferences
            // and the corresponding editor
            SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(
                                        this.getApplicationContext());
            SharedPreferences.Editor preferenceEditor = sharedPrefs.edit();

            preferenceEditor.putString(
                    getString(R.string.pref_saltKey_key),
                    inputPrefs.getString(
                        getString(R.string.pref_saltKey_key)));
            preferenceEditor.putString(
                    getString(R.string.pref_defaultIterations_key),
                    inputPrefs.getString(
                        getString(R.string.pref_defaultIterations_key)));
            preferenceEditor.putString(
                    getString(R.string.pref_siteAttributesList_key),
                    inputPrefs.getString(
                        getString(R.string.pref_siteAttributesList_key)));

            // Commit the changes
            preferenceEditor.commit();
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "ERROR: Malformed JSON! " +
                  "JSON='" + preferencesFileBuffer.toString() + "', " +
                  "Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           IMPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Toast.makeText(this.getApplicationContext(),
                       IMPORT_SETTINGS_MESSAGE,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * @brief   Function to export preferences to a JSON file
     * @return  Does not return a value
     */
    private void exportPreferences() {
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
     * @brief   Inner class NavigationDrawerItem.
     *          Models an item in the Navigation Drawer list.
     */
    private class NavigationDrawerItem {

        // ----------------------------------------------------------------
        // CREATORS

        /**
         * @brief   Constructor
         * @return  Does not have a return type
         */
        public NavigationDrawerItem(int iconId,
                                    String text) {
            this.m_iconId = iconId;
            this.m_text = text;
        }

        // ----------------------------------------------------------------
        // PUBLIC METHODS

        /**
         * @brief   
         * @return  {int} The ID associated with the icon in the "R" class
         */
        public int getIconId() {
            return this.m_iconId;
        }

        /**
         * @brief   
         * @return  {String} The text associated with the item
         */
        public String getText() {
            return this.m_text;
        }

        // ----------------------------------------------------------------
        // PRIVATE MEMBERS

        private int     m_iconId;       /** @brief The id of the icon
                                          * resource in the "R" class
                                          */
        private String  m_text;         /** @brief The display text
                                          * associated with the item
                                          */
    }

    /**
     * @brief   Inner class NavigationDrawerListAdpater.
     *          Provides a custom adapter for Navigation Drawer items
     *          (which are more complex types than String types) by
     *          extending the BaseAdapter class.
     */
    private class NavigationDrawerListAdapter extends BaseAdapter {

        // ----------------------------------------------------------------
        // CREATORS

        /**
         * @brief   Constructor
         * @return  Does not have a return type
         */
        public NavigationDrawerListAdapter(Context context,
                                           ArrayList<NavigationDrawerItem>
                                                navigationDrawerItems) {
            this.m_context = context;
            this.m_navigationDrawerItems = navigationDrawerItems;
        }

        // ----------------------------------------------------------------
        // PUBLIC METHODS

        /**
         * @brief   
         * @return  
         */
        @Override
        public int getCount() {
            return m_navigationDrawerItems.size();
        }

        /**
         * @brief   Method to return a reference to the object at
         *          a specific position.
         *          This would need to be typecast to the approriate type
         *          to use that object's methods.
         * @param   The position to look up
         * @return  A reference to the object at position "position"
         */
        @Override
        public Object getItem(int position) {
            return m_navigationDrawerItems.get(position);
        }

        /**
         * @brief   
         * @return  
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * @brief   
         * @return  
         */
        @Override
        public View getView(int position,
                            View view,
                            ViewGroup parentViewGroup) {
            if (null == view) {
                LayoutInflater inflater =
                    (LayoutInflater)m_context.getSystemService(
                            Activity.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.navigation_drawer_item,
                                        null);
            }

            ImageView itemIcon =
                (ImageView)view.findViewById(R.id.navigationItemIcon);
            TextView itemText =
                (TextView)view.findViewById(R.id.navigationItemText);

            // Obtain a reference to the NavigationItem
            // at position "position", and use it to set resources.
            NavigationDrawerItem navigationItem =
                (NavigationDrawerItem)this.getItem(position);
            itemIcon.setImageResource(navigationItem.getIconId());
            itemText.setText(navigationItem.getText());

            return view;
        }

        // ----------------------------------------------------------------
        // PRIVATE MEMBERS

        /**
         * @brief   A reference to the Application context
         */
        private Context                         m_context;

        /**
         * @brief   A reference to the list of items in the Navigation Drawer
         */
        private ArrayList<NavigationDrawerItem> m_navigationDrawerItems;
    }

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
    private TreeMap<Integer, NavigationDrawerItem>
                                    m_drawerMap;    /**
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
