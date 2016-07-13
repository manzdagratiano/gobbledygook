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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
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
import org.json.JSONArray;
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

        // Set the Toolbar as the App bar
        // (as well as the Action bar through the "Support" framework)
        Toolbar appBar = (Toolbar)findViewById(R.id.appBar);
        setSupportActionBar(appBar);

        // Create the navigation drawer
        this.createNavigationDrawer();

        // Set the navigation drawer header
        this.setNavigationDrawerHeader();

        // If this is the first launch, create a "Salt Key"
        this.checkAndCreateSaltKey();

        // If there is no saved state,
        // launch the "home" fragment
        // (guaranteed to be at position 0 in the drawer)
        if (null == savedInstanceState) {
            this.onActivitySelection(R.id.drawerHome);
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
     * @return  Does not return a value
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        Log.i(LOG_CATEGORY, "onActivityResult() handler called...");
        if (READ_SETTINGS_FILE_CODE == requestCode &&
            Activity.RESULT_OK == resultCode) {
            Log.i(LOG_CATEGORY, "onActivityResult(): " +
                  "Calling onSettingsFileSelection()...");
            this.onSettingsFileSelection(resultData.getData());
        }

        super.onActivityResult(requestCode, resultCode, resultData);
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
    protected static final String FILE_MANAGER_MISSING_ERROR              =
        "ERROR importing file! Please install a file manager " +
        "to be able to browse to a file";
    protected static final String IMPORT_SETTINGS_MESSAGE                 =
        "Successfully imported settings...";
    protected static final String IMPORT_SETTINGS_ERROR                   =
        "ERROR! Found malformed file! Failed to import settings! :(";
    protected static final String INIT_MESSAGE                            =
        "Initializing...";

    // Request codes for spawning activities
    protected static final int    READ_SETTINGS_FILE_CODE                 =
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
        Log.i(LOG_CATEGORY, "createNavigationDrawer(): " +
              "Configuring the navigation drawer...");

        m_drawerLayout = (DrawerLayout)findViewById(
                R.id.navigationDrawerLayout);
        m_drawerView = (NavigationView)m_drawerLayout.findViewById(
                    R.id.navigationDrawer);
        m_drawerView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean
                    onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        onActivitySelection(menuItem.getItemId());
                        return true;
                    }
                });

        // Get the title
        m_title = getTitle();

        // Set the action bar app icon to behave as drawer toggler
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set drawer toggle interactions to play well with the action bar
        m_drawerToggle = new ActionBarDrawerToggle(
                                    this,
                                    m_drawerLayout,
                                    R.drawable.ic_drawer,
                                    R.string.drawer_action_open,
                                    R.string.drawer_action_closed) {

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
     * @brief   A method to set the text in the navigation drawer header.
     *          This will be appropriately overridden in each flavor.
     * @return  Does not return a value
     */
    protected void setNavigationDrawerHeader() {
        // Do nothing
    }

    /**
     * @brief   Method to check if a salt key exists, and if not,
     *          to create a new one and save it to SharedPreferences.
     * @return  Does not return a value
     */
    protected void checkAndCreateSaltKey() {
        Log.i(LOG_CATEGORY, "checkAndCreateSaltKey(): " +
              "Checking for salt key...");

        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    this.getApplicationContext());
        String saltKey = "";
        // Catch all exceptions when reading from SharedPreferences
        try {
            saltKey = sharedPrefs.getString(
                    getString(R.string.pref_saltKey_key),
                              "");
        } catch (Exception e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        if (saltKey.isEmpty()) {
            Log.i(LOG_CATEGORY, "checkAndCreateSaltKey(): " +
                    "saltKey=null, Creating new key...");
            // Since this may take a few seconds,
            // inform the user
            Toast.makeText(this.getApplicationContext(),
                           INIT_MESSAGE,
                           Toast.LENGTH_SHORT).show();
            saltKey = Crypto.generateSaltKey();
            SharedPreferences.Editor preferenceEditor = sharedPrefs.edit();
            preferenceEditor.putString(getString(R.string.pref_saltKey_key),
                                       saltKey);
            preferenceEditor.commit();
        }
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     * @return  Does not return a value
     */
    protected void onActivitySelection(int itemId) {
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
     * @brief   Function to export settings to a JSON file
     * @return  Does not return a value
     */
    protected void exportSettings() {
        Log.i(LOG_CATEGORY, "exportSettings() handler called...");

        // Create a JSON object from the SharedPreferences
        JSONObject outputPrefs = constructSchema();
        if (null == outputPrefs) {
            Log.e(LOG_CATEGORY, "exportSettings(): " +
                  "ERROR: Schema.Construction.FAILURE");
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(LOG_CATEGORY, "exportSettings(): " +
              "outputPrefs='" + outputPrefs.toString() + "'");

        // Obtain a file handle for output in the external storage;
        // this is necessary since the user must be able to access
        // the output file
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
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
                Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
                      "Directory.Creation.Failure");
                Toast.makeText(this.getApplicationContext(),
                               EXPORT_SETTINGS_ERROR,
                               Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (!outputDir.isDirectory()) {
            Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
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
            Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
                  "Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        } catch (IOException e) {
            Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
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
                Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
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

    /**
     * @brief   Function to import settings from a JSON file
     * @return  Does not return a value
     */
    protected void importSettings() {
        Log.i(LOG_CATEGORY, "importSettings() handler called...");

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
        Log.i(LOG_CATEGORY, "importSettings(): Opening File Picker UI...");
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
                                   READ_SETTINGS_FILE_CODE);
        } else {
            Toast.makeText(this.getApplicationContext(),
                           FILE_MANAGER_MISSING_ERROR,
                           Toast.LENGTH_SHORT).show();
        }
        // The callback "onActivityResult" will be called
    }

    // ====================================================================
    // PRIVATE METHODS

    /**
     * @brief   A method to construct a schema object for exporting
     *          to a file.
     * @return  {JSONObject} The constructed schema.
     */
    private JSONObject constructSchema() {
        JSONObject outputSchema = new JSONObject();
        // Obtain a handle to the Shared Preferences in the system.
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    this.getApplicationContext());

        try {
            // Construct the preferences for the profile
            JSONObject profileSettings = new JSONObject();
            // Salt key; blank on empty retrieval
            profileSettings.put(
                    getString(R.string.pref_saltKey_key),
                    sharedPrefs.getString(
                        getString(R.string.pref_saltKey_key),
                        ""));
            // Default iterations; the iterations "hint"
            // (which was the default value) on empty retrieval
            profileSettings.put(
                    getString(R.string.pref_defaultIterations_key),
                    sharedPrefs.getString(
                        getString(R.string.pref_defaultIterations_key),
                        getString(R.string.hint_iterations)));
            // Custom website attribute list; blank on empty retrieval
            profileSettings.put(
                    getString(R.string.pref_customOverrides_key),
                    sharedPrefs.getString(
                        getString(R.string.pref_customOverrides_key),
                        ""));

            // Construct the profile itself.
            JSONObject defaultProfile = new JSONObject();
            defaultProfile.put(
                    getString(R.string.schema_profile_name_key),
                    getString(R.string.schema_default_profile_name));
            defaultProfile.put(
                    getString(R.string.schema_profile_settings_key),
                    profileSettings);

            // Put the profile into an array
            JSONArray profiles = new JSONArray();
            profiles.put(defaultProfile);

            // Put the array into the profiles
            outputSchema.put(
                    getString(R.string.schema_profiles_key),
                    profiles);

            return outputSchema;
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "exportSettings() ERROR: " +
                  "Caught " + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @brief   
     * @return  
     */
    private void onSettingsFileSelection(Uri uri) {
        Log.i(LOG_CATEGORY, "onSettingsFileSelection(): " +
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
            JSONObject schema =
                new JSONObject(preferencesFileBuffer.toString());
            JSONObject inputPrefs =
                this.validateAndReturnPreferences(schema);

            if (null != inputPrefs) {
                Toast.makeText(this.getApplicationContext(),
                               IMPORT_SETTINGS_ERROR,
                               Toast.LENGTH_SHORT).show();
                return;
            }

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
                    getString(R.string.pref_customOverrides_key),
                    inputPrefs.getString(
                        getString(R.string.pref_customOverrides_key)));

            // Commit the changes
            preferenceEditor.commit();
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "ERROR: JSON.Malformed, " +
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
     * @brief   A method to validate if an input schema object
     *          is valid.
     * @param   {JSONObject} A schema object.
     * @return  {JSONObject} The preferences section of the schema
     */
    private JSONObject validateAndReturnPreferences(final JSONObject schema) {
        try {
            JSONArray profiles = schema.getJSONArray(
                    getString(R.string.schema_profiles_key));
            if (1 != profiles.length()) {
                Log.e(LOG_CATEGORY, "ERROR: JSON.Malformed, " +
                      "Too.Many.Profiles, Time.Travel.Anomaly, " +
                      "JSON='" + schema);
                return null;
            }
            JSONObject defaultProfile = (JSONObject)(profiles.get(0));

            if (!(defaultProfile.has(
                            getString(R.string.schema_profile_name_key)) &&
                  defaultProfile.has(
                            getString(R.string.schema_profile_settings_key)) &&
                  (2 == defaultProfile.length()))) {
                Log.e(LOG_CATEGORY, "ERROR: JSON.Malformed, " +
                      "Bad.Profile, " +
                      "JSON='" + schema);
                return null;
            }
            JSONObject profileSettings = (JSONObject)defaultProfile.get(
                    getString(R.string.schema_profile_settings_key));

            if (!(profileSettings.has(
                        getString(R.string.pref_saltKey_key)) &&
                  profileSettings.has(
                        getString(R.string.pref_defaultIterations_key)) &&
                  profileSettings.has(
                        getString(R.string.pref_customOverrides_key)) &&
                  (3 == profileSettings.length()))) {
                Log.e(LOG_CATEGORY, "ERROR: JSON.Malformed, " +
                      "Bad.Profile.Settings, " +
                      "JSON='" + schema);
                return null;
            }

            return profileSettings;
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "ERROR: JSON.Malformed, " +
                  "JSON='" + schema);
            e.printStackTrace();
            return null;
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS

    protected DrawerLayout          m_drawerLayout; /**
                                                     * @brief The drawer
                                                     * layout
                                                     */
    protected NavigationView        m_drawerView;   /**
                                                     * @brief The drawer
                                                     * list view
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
