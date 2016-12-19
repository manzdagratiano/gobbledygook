/**
 * @file        Yggdrasil.java
 * @summary     Source file for the Yggdrasil class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

// JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrefsHandler implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    // --------------------------------------------------------------------
    // CONSTRUCTOR

    PrefsHandler(final AppCompatActivity activity,
                 final String logCategory) {
        m_activity = activity;
        m_logCategory = logCategory;
    }

    // --------------------------------------------------------------------
    // PUBLIC METHODS

    /**
     * @summary Function to export settings to a JSON file
     * @return  Does not return a value
     */
    public void exportSettings() {
        final String FUNC = "exportSettings(): ";
        Log.i(getLogCategory(), getLogPrefix(FUNC) + ">>");

        // If running on Marshmallow or higher (API Level 23),
        // we need to ask the user explicitly for write permission.
        if ((android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) &&
            (ContextCompat.checkSelfPermission(
                                m_activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
             != PackageManager.PERMISSION_GRANTED)) {
            // Request permission
            new ConfirmationDialog().show(m_activity.getFragmentManager(),
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
            Toast.makeText(m_activity.getApplicationContext(),
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
            Toast.makeText(m_activity.getApplicationContext(),
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
                Toast.makeText(m_activity.getApplicationContext(),
                               EXPORT_SETTINGS_ERROR,
                               Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (!outputDir.isDirectory()) {
            Log.e(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: File.Exists.InPlaceOf.Directory");
            Toast.makeText(m_activity.getApplicationContext(),
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
            Toast.makeText(m_activity.getApplicationContext(),
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

        Toast.makeText(m_activity.getApplicationContext(),
                       EXPORT_SETTINGS_MESSAGE,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * @summary Function to import settings from a JSON file
     * @return  Does not return a value
     */
    public void importSettings() {
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
        if (null != intent.resolveActivity(m_activity.getPackageManager())) {
            m_activity.startActivityForResult(fileChooser,
                                              READ_SETTINGS_FILE_CODE);
        } else {
            Toast.makeText(m_activity.getApplicationContext(),
                           FILE_MANAGER_MISSING_ERROR,
                           Toast.LENGTH_SHORT).show();
        }
        // The callback "onActivityResult" will be called
    }

    // --------------------------------------------------------------------
    // HANDLERS

    /**
     * @summary Called when a spawned activity returns
     * @return  Does not return a value
     */
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        final String FUNC = "onActivityResult()";
        Log.i(getLogCategory(), FUNC);
        if (READ_SETTINGS_FILE_CODE == requestCode &&
            AppCompatActivity.RESULT_OK == resultCode) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "Calling onSettingsFileSelection()...");
            this.onSettingsFileSelection(resultData.getData());
        }
    }

    /**
     * @summary Called when a permission is requested.
     * @return  Does not even.
     */
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
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
                    Toast.makeText(m_activity.getApplicationContext(),
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
    // PRIVATE METHODS

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
     * @summary An method to obtain the log category,
     *          suitably overridden in the concrete implementation.
     * @return  {String} The log category.
     */
    private String getLogCategory() {
        return m_logCategory;
    }

    /**
     * @summary A method to get a prefix for the log.
     * @return  {String} The log prefix
     */
    private String getLogPrefix(String FUNC) {
        return FUNC + ": ";
    }

    /**
     * @summary Method to wrap "getString" from Activity,
     *          so that moving code around does not require
     *          changing millions of lines of code.
     * @return  {String} The string returned.
     */
    private String getString(final int id) {
        return m_activity.getString(id);
    }

    /**
     * @summary 
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
                m_activity.getContentResolver().openInputStream(uri);
            bufferedFileReader =
                new BufferedReader(new InputStreamReader(inputStream));
            while (null != (line = bufferedFileReader.readLine())) {
                preferencesFileBuffer.append(line + "\n");
            }
        } catch (IOException e) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Caught " + e);
            Toast.makeText(m_activity.getApplicationContext(),
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
                Toast.makeText(m_activity.getApplicationContext(),
                               IMPORT_SETTINGS_ERROR,
                               Toast.LENGTH_SHORT).show();
                return;
            }

            // Get a handle to the default shared preferences
            // and the corresponding editor
            SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(
                                        m_activity.getApplicationContext());
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
            Toast.makeText(m_activity.getApplicationContext(),
                           IMPORT_SETTINGS_ERROR,
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Toast.makeText(m_activity.getApplicationContext(),
                       IMPORT_SETTINGS_MESSAGE,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * @summary A method to construct a schema object for exporting
     *          to a file.
     * @return  {JSONObject} The constructed schema.
     */
    private JSONObject constructSchema() {
        final String FUNC = "constructSchema(): ";

        JSONObject outputSchema = new JSONObject();
        // Obtain a handle to the Shared Preferences in the system.
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    m_activity.getApplicationContext());

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
     * @summary A method to validate if an input schema object
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

    private AppCompatActivity   m_activity;     /**
                                                  * @brief A handle to the
                                                  *        calling activity.
                                                  */
    private String              m_logCategory;  /**
                                                  * @brief The log category.
                                                  */

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @summary A class to encapsulate a confirmation dialog for
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
