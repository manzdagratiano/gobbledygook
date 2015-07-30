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

// Libraries
import com.manzdagratiano.yggdrasil.AboutFragment;
import com.manzdagratiano.yggdrasil.HomeFragment;
import com.manzdagratiano.yggdrasil.NavigationDrawerItem;
import com.manzdagratiano.yggdrasil.PrefsFragment;
import com.manzdagratiano.yggdrasil.R;
import com.manzdagratiano.yggdrasil.Yggdrasil;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

// Standard Java
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.StringBuilder;
import java.util.TreeMap;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

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

        Intent intent = null;
        switch(item.getItemId()) {
            case R.id.home:
                this.onActivitySelection(
                        GobbledygookDrawerItem.HOME.ordinal());
                return true;
            case R.id.saltKeyActions:
                this.onActivitySelection(
                        GobbledygookDrawerItem.SALTKEY_ACTIONS.ordinal());
                return true;
            case R.id.settings:
                this.onActivitySelection(
                        GobbledygookDrawerItem.SETTINGS.ordinal());
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

    // An enum for the items in the navigation drawer
    protected enum GobbledygookDrawerItem implements DrawerItem {
        HOME,
        SALTKEY_ACTIONS,
        IMPORT_SETTINGS,
        EXPORT_SETTINGS,
        SETTINGS,
        ABOUT
    }

    // Toast messages
    private static final String FILE_MANAGER_MISSING_ERROR                =
        "ERROR importing file! Please install a file manager " +
        "to be able to browse to a file";
    private static final String IMPORT_SETTINGS_MESSAGE                   =
        "Successfully imported settings...";
    private static final String IMPORT_SETTINGS_ERROR                     =
        "ERROR! Found malformed file! Failed to import settings! :(";

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
                GobbledygookDrawerItem.HOME.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_menu_home,
                                         "Home"));
        m_drawerMap.put(
                GobbledygookDrawerItem.SALTKEY_ACTIONS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_menu_account_list,
                                         "Salt Key Actions"));
        m_drawerMap.put(
                GobbledygookDrawerItem.IMPORT_SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_download,
                                         "Import Settings..."));
        m_drawerMap.put(
                GobbledygookDrawerItem.EXPORT_SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_action_upload,
                                         "Export Settings..."));
        m_drawerMap.put(
                GobbledygookDrawerItem.SETTINGS.ordinal(),
                new NavigationDrawerItem(R.drawable.ic_settings,
                                         "Settings"));
        m_drawerMap.put(
                GobbledygookDrawerItem.ABOUT.ordinal(),
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
        final GobbledygookDrawerItem[] items =
            GobbledygookDrawerItem.values();
        switch(items[position]) {
            case HOME:
                // Main activity
                this.swapFragment(new GobbledygookHomeFragment(),
                                  getString(R.string.tag_homeFragment));
                break;
            case SALTKEY_ACTIONS:
                // Salt Key Actions
                this.swapFragment(new SaltKeyActionsFragment(),
                                  getString(
                                      R.string.tag_saltKeyActionsFragment));
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
}
