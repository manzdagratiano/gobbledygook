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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import java.lang.RuntimeException;
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
    implements ActivityCompat.OnRequestPermissionsResultCallback
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

        // Selecting a default state should be left
        // to the concrete implementations.
    }

    /**
     * @brief   ActionBarDrawerToggle must have its state synced in
     *          "onPostCreate"
     * @return  Does not return a value
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        final String FUNC = "onPostCreate(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring ActionBarDrawerToggle...");
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
        getMenuInflater().inflate(R.menu.action_menu,
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
        menu.findItem(R.id.homePage).setVisible(!isDrawerOpen);
        menu.findItem(R.id.settingsPage).setVisible(!isDrawerOpen);

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

        switch(item.getItemId()) {
            case R.id.homePage:
                this.onActivitySelection(R.id.drawerHome);
                return true;
            case R.id.settingsPage:
                this.onActivitySelection(R.id.drawerSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        final String FUNC = "onActivityResult()";
        Log.i(getLogCategory(), FUNC);
        if (READ_SETTINGS_FILE_CODE == requestCode &&
            Activity.RESULT_OK == resultCode) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "Calling onSettingsFileSelection()...");
            this.onSettingsFileSelection(resultData.getData());
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * @brief   Called when a permission is requested.
     * @return  Does not even.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                                         permissions,
                                         grantResults);
        final String FUNC = "onRequestPermissionsResult()";
        switch(requestCode) {
            case WRITE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 &&
                    (PackageManager.PERMISSION_GRANTED == grantResults[0])) {
                    // Permission granted; go back to exporting the file.
                    this.exportSettings();
                } else {
                    Log.e(getLogCategory(), getLogPrefix(FUNC) +
                          "Write.Permission.Denied!");
                    Toast.makeText(this,
                                   NO_WRITE_PERMISSION_MESSAGE,
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "Unknown requestCode=" + Integer.toString(requestCode));
        }
    }

    // ====================================================================
    // PROTECTED METHODS

    /**
     * @brief   An method to obtain the log category,
     *          suitably overridden in the concrete implementation.
     * @return  {String} The log category.
     */
    protected abstract String getLogCategory();

    /**
     * @brief   A method to get a prefix for the log.
     * @return  {String} The log prefix
     */
    protected String getLogPrefix(String FUNC) {
        return FUNC + ": ";
    }

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String FRAGMENT_DIALOG                         =
        "dialog";

    // Output file constants
    private static final String OUTPUT_DIRECTORY_NAME                   =
        "gobbledygook";
    private static final String OUTPUT_PREFERENCES_FILENAME             =
        "gobbledygook.json";
    private static final int JSON_INDENT_FACTOR                         =
        8;

    // Toast messages
    private static final String DOPPELGANGER_FILE_ERROR                 =
        "ERROR exporting settings! " +
        "A file by the hijacked name 'gobbledygook' " +
        "already exists in the Documents folder :( " +
        "Please remove/rename it to continue";
    private static final String EXPORT_SETTINGS_MESSAGE                 =
        "Successfully exported settings to file " +
        OUTPUT_PREFERENCES_FILENAME + " in the Documents/" +
        OUTPUT_DIRECTORY_NAME + " folder";
    private static final String EXPORT_SETTINGS_ERROR                   =
        "ERROR exporting settings to file :(";
    private static final String EXTERNAL_STORAGE_ERROR                  =
        "ERROR exporting settings! External storage not mounted :(";
    private static final String FILE_MANAGER_MISSING_ERROR              =
        "ERROR importing file! Please install a file manager " +
        "to be able to browse to a file";
    private static final String IMPORT_SETTINGS_MESSAGE                 =
        "Successfully imported settings...";
    private static final String IMPORT_SETTINGS_ERROR                   =
        "ERROR! Found malformed file! Failed to import settings! :(";
    private static final String INIT_MESSAGE                            =
        "Initializing...";
    private static final String NO_WRITE_PERMISSION_MESSAGE             =
        "Exporting will not work until write permission is granted :(";

    // Request codes for spawning activities
    private static final int    READ_SETTINGS_FILE_CODE                 =
        8086;
    private static final int    WRITE_PERMISSION_REQUEST_CODE         =
        1337;

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
     * @brief   Method to configure the navigation drawer.
     * @return  Does not even.
     */
    protected void createNavigationDrawer() {
        final String FUNC = "createNavigationDrawer(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
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
     * @brief   Method to check if a salt key exists, and if not,
     *          to create a new one and save it to SharedPreferences.
     * @return  Does not return a value
     */
    protected void checkAndCreateSaltKey() {
        final String FUNC = "checkAndCreateSaltKey(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
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
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Caught " + e);
            e.printStackTrace();
        }

        if (saltKey.isEmpty()) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                    "saltKey=null, Creating new key...");
            // Since this may take a few seconds,
            // inform the user
            Toast.makeText(this.getApplicationContext(),
                           INIT_MESSAGE,
                           Toast.LENGTH_SHORT).show();

            saltKey = null;
            try {
                saltKey = Crypto.generateSaltKey();
            } catch (UnsupportedEncodingException e) {
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: Caught " + e);
                e.printStackTrace();
                // We cannot proceed.
                throw new RuntimeException("SaltKey.Generation.Failure");
            }
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "Generated saltKey='" + saltKey + "'");

            SharedPreferences.Editor preferenceEditor = sharedPrefs.edit();
            preferenceEditor.putString(getString(R.string.pref_saltKey_key),
                                       saltKey);
            preferenceEditor.apply();
        }
    }

    /**
     * @brief   A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     *          This method may be overridden in the concrete derived classes.
     * @return  Does not return a value.
     */
    protected void onActivitySelection(int itemId) {
        final String FUNC =  "onActivitySelection(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Selecting activity id=" + itemId);

        switch(itemId) {
            case R.id.drawerSettingsExport:
                // Export settings
                this.exportSettings();
                break;
            case R.id.drawerSettingsImport:
                // Import settings
                this.importSettings();
                break;
            default:
                // Do nothing
        }
    }

    /**
     * @brief   Function to swap in a new fragment into
     *          the frameLayout of the Navigation Drawer
     * @return  Does not return a value
     */
    protected void swapFragment(Fragment fragment,
                                final String fragmentTag) {
        assert (null != fragment) : "Asked to swap in null fragment!!!";
        getFragmentManager().beginTransaction()
            .replace(R.id.contentFrame,
                     fragment,
                     fragmentTag)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit();
        // We will NOT add the fragment to the back stack,
        // fragmentTx.addToBackStack(null)
        // since after having wandered across fragments,
        // the "back" behavior does not make sense anymore,
        // nor does it allow consistent highlighting in the navigation drawer.
        // The only sensible place for the "back" behavior is the WebView.
        // We will train the user to navigate using the navigation drawer,
        // and the back button will exit the app.
    }

    /**
     * @brief   Function to export settings to a JSON file
     * @return  Does not return a value
     */
    protected void exportSettings() {
        final String FUNC = "exportSettings(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) + ">>");

        // If running on Marshmallow or higher (API Level 23),
        // we need to ask the user explicitly for write permission.
        if ((android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) &&
            (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
             != PackageManager.PERMISSION_GRANTED)) {
            // Request permission
            new ConfirmationDialog().show(this.getFragmentManager(),
                                          FRAGMENT_DIALOG);
            // If permission is granted, the permission handler will
            // trigger exportSettings() again.
            return;
        }

        // Create a JSON object from the SharedPreferences
        JSONObject outputPrefs = constructSchema();
        if (null == outputPrefs) {
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Schema.Construction.FAILURE");
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        Log.e(getLogCategory(), getLogPrefix(FUNC) +
              "outputPrefs='" + outputPrefs.toString() + "'");

        // Obtain a file handle for output in the external storage;
        // this is necessary since the user must be able to access
        // the output file
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: External.Storage.NOT_MOUNTED");
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
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: Directory.Creation.Failure");
                Toast.makeText(this.getApplicationContext(),
                               EXPORT_SETTINGS_ERROR,
                               Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (!outputDir.isDirectory()) {
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: File.Exists.InPlaceOf.Directory");
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
        } catch (IOException | JSONException e) {
            // IOException is a superclass of FileNotFoundException,
            // and will catch that as well.
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Caught " + e);
            Toast.makeText(this.getApplicationContext(),
                           EXPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: Memory Leak! Could not close FileOutputStream." +
                      " Caught " + e);
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
        final String FUNC = "importSettings(): ";
        Log.e(getLogCategory(), getLogPrefix(FUNC) + ">>");

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
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Opening File Picker UI...");
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
        final String FUNC = "constructSchema(): ";

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
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Caught " + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @brief   
     * @return  
     */
    private void onSettingsFileSelection(Uri uri) {
        final String FUNC = "onSettingsFileSelection(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
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
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Caught " + e);
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
                    Log.i(getLogCategory(), getLogPrefix(FUNC) +
                          "ERROR: Memory Leak! " +
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

            if (null == inputPrefs) {
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
            preferenceEditor.apply();
        } catch (JSONException e) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Malformed JSON! " +
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
        final String FUNC = "validateAndReturnPreferences(): ";

        try {
            JSONArray profiles = schema.getJSONArray(
                    getString(R.string.schema_profiles_key));
            if (1 != profiles.length()) {
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: JSON.Malformed, " +
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
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: JSON.Malformed, " +
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
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: JSON.Malformed, " +
                      "Bad.Profile.Settings, " +
                      "JSON='" + schema);
                return null;
            }

            return profileSettings;
        } catch (JSONException e) {
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: JSON.Malformed, " +
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

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @brief   A class to encapsulate a confirmation dialog for
     *          external storage permission checks.
     *          This class, being a fragment, needs to be public so that
     *          its constructor can be accessed while restoring the state
     *          of the activity.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity().getApplicationContext();
            return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.permissionRequest)
                .setPositiveButton(android.R.string.ok,
                                   new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            final String permission =
                            Manifest.permission.WRITE_EXTERNAL_STORAGE;
                            ActivityCompat.requestPermissions(
                                            getActivity(),
                                            new String[]{permission},
                                            WRITE_PERMISSION_REQUEST_CODE);
                        }
                    })
                .setNegativeButton(android.R.string.cancel,
                                   new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Toast.makeText(context,
                                           NO_WRITE_PERMISSION_MESSAGE,
                                           Toast.LENGTH_SHORT).show();
                        }
                    })
                .create();
        }

    }

}
