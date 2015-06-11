/**
 * @file        GobbledygookSaltKeyActions.java
 * @brief       Source file for the GobbledygookSaltKey class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 08, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package com.manzdagratiano.gobbledygook;

// Android
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.Security;

// SpongyCastle
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;

public class GobbledygookSaltKeyActions extends Activity {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ===================================================================
    // PUBLIC METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "Creating SaltKeyActions activity...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.saltkeyactions);

        // Enable the app icon as an "up" button
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStart() {
        Log.i(LOG_CATEGORY, "onStart(): Configuring elements...");
        super.onResume();

        this.configureElements();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        Log.i(LOG_CATEGORY, "onActivityResult() handler called...");

        // Handle the result from the file picker
        // for the loadSaltKey() handler, if this is how we got here
        if (requestCode == READ_SALT_KEY_FILE &&
            resultCode == Activity.RESULT_OK) {
            Log.i(LOG_CATEGORY, "onActivityResult(): " +
                    "Calling onSaltKeyFileSelection()...");
            this.onSaltKeyFileSelection(resultData.getData());
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    // --------------------------------------------------------------------
    // EVENT HANDLERS

    /**
     * @brief   
     * @return  
     */
    public void loadSaltKey(final View view) {
        Log.i(LOG_CATEGORY, "loadSaltKey() handler called...");

        // Open the file picker dialog to select the key file.
        // This requires creating a new "Intent".
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to only show text files
        intent.setType("text/*");

        // Start the activity
        Log.i(LOG_CATEGORY, "loadSaltKey(): Opening File Picker UI...");
        // Start the activity, but through a "chooser"
        // for available Content Providers instead of the intent directly,
        // since the user may prefer a different one each time.
        Intent fileChooser =
            intent.createChooser(intent,
                                 "Select a plaintext file...");
        // Check if the intent resolves to any activities,
        // and start it if it does.
        if (null != intent.resolveActivity(this.getPackageManager())) {
            startActivityForResult(fileChooser,
                                   READ_SALT_KEY_FILE);
        }
        // The callback "onActivityResult" will be called
    }

    /**
     * @brief   
     * @return  
     */
    public void generateSaltKey(final View view) {
        Log.i(LOG_CATEGORY, "generateSaltKey() handler called...");
        Toast.makeText(getApplicationContext(),
                       "Generating new salt key...",
                       Toast.LENGTH_SHORT).show();
        SecureRandom csprng = new SecureRandom();
        byte[] saltKeyBytes = new byte[SALT_KEY_LENGTH];
        csprng.nextBytes(saltKeyBytes);
        String saltKey = null;
        try {
            saltKey = new String(Base64.encode(saltKeyBytes), UTF8);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }
        Log.i(LOG_CATEGORY, "generateSaltKey(): " +
              "Generated saltKey='" + saltKey + "'");

        // Set the view with the saltKey
        Log.i(LOG_CATEGORY, "generateSaltKey(): " +
              "Setting the view with saltKey...");
        EditText saltKeyBox = (EditText)findViewById(R.id.saltKey);
        saltKeyBox.setText(saltKey, TextView.BufferType.EDITABLE);

        // Save the saltKey into the preferences object
        saveSaltKeyToSharedPreferences(saltKey);

        // Uncheck the unlockSaltKey checkBox
        uncheckLoadSaltKeyCheckBox();
    }

    // ===================================================================
    // PRIVATE METHODS

    private static final String LOG_CATEGORY        = "GOBBLEDYGOOK.SALTKEY";
    private static final int    READ_SALT_KEY_FILE  = 666;
    private static final int    SALT_KEY_LENGTH     = 512;
    private static final String UTF8                = "UTF-8";

    /**
     * @brief   
     * @return  
     */
    private void configureElements() {

        class Configurator {

            /**
             * @brief   
             * @return  
             */
            public void configureUnlockSaltKey() {
                Log.i(LOG_CATEGORY, "configureUnlockSaltKey(): " +
                      "Unchecking checkbox...");

                CheckBox unlockSaltKeyBox =
                    (CheckBox)findViewById(R.id.unlockSaltKey);
                unlockSaltKeyBox.setChecked(false);

                // Attach an onCheckedChangeListener
                // (as opposed to an onClickListener,
                // since we'll be unchecking the checkBox from code)
                Log.i(LOG_CATEGORY, "configureUnlockSaltKey(): " +
                      "Attaching onCheckedChangeListener...");
                unlockSaltKeyBox.setOnCheckedChangeListener(
                                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view,
                                                boolean isChecked) {
                        EditText saltKeyBox =
                            (EditText)findViewById(R.id.saltKey);
                        Button loadSaltKeyButton =
                            (Button)findViewById(R.id.loadSaltKey);
                        Button generateSaltKeyButton =
                            (Button)findViewById(R.id.generateSaltKey);
                        if (isChecked) {
                            Toast.makeText(getApplicationContext(),
                                           "Load or Generate a new Salt Key",
                                           Toast.LENGTH_SHORT).show();
                            saltKeyBox.setEnabled(true);
                            loadSaltKeyButton.setEnabled(true);
                            generateSaltKeyButton.setEnabled(true);
                        } else {
                            saltKeyBox.setEnabled(false);
                            loadSaltKeyButton.setEnabled(false);
                            generateSaltKeyButton.setEnabled(false);
                        }
                    }
                });
            }

            /**
             * @brief   
             * @return  
             */
            public void configureSaltKey() {
                Log.i(LOG_CATEGORY, "configureSaltKey(): Setting value...");

                EditText saltKeyBox = (EditText)findViewById(R.id.saltKey);

                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                                                getApplicationContext());

                // Set the summary with the value
                String saltKey = sharedPreferences.getString(
                                    getString(R.string.pref_saltKey_key), "");
                if (saltKey.isEmpty()) {
                    Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                          "The salt key is empty");
                } else {
                    Log.i(LOG_CATEGORY, "configureSaltKey(): " +
                          "Found non-empty saltKey='" + saltKey + "'");
                    saltKeyBox.setText(saltKey, TextView.BufferType.EDITABLE);
                }

                // Disable the saltKey by default for editing,
                // it should be enabled only when unlockSaltKey is checked
                saltKeyBox.setEnabled(false);
            }

            /**
             * @brief   
             * @return  
             */
            public void configureLoadSaltKey() {
                Button loadSaltKeyButton = (Button)findViewById(R.id.loadSaltKey);
                loadSaltKeyButton.setEnabled(false);
            }

            /**
             * @brief   
             * @return  
             */
            public void configureGenerateSaltKey() {
                Button generateSaltKeyButton = (Button)findViewById(R.id.generateSaltKey);
                generateSaltKeyButton.setEnabled(false);
            }

        };  // end class Configurator

        Configurator configurator = new Configurator();

        configurator.configureUnlockSaltKey();
        configurator.configureSaltKey();
        configurator.configureLoadSaltKey();
        configurator.configureGenerateSaltKey();
    }

    /**
     * @brief   
     * @return  
     */
    private void onSaltKeyFileSelection(Uri uri) {
        Log.i(LOG_CATEGORY, "onSaltKeyFileSelection() called..., " +
              "uri='" + uri.toString() + "'");

        InputStream inputStream = null;
        BufferedReader bufferedFileReader = null;
        String saltKey = null;
        try {
            inputStream = this.getContentResolver().openInputStream(uri);
            bufferedFileReader =
                new BufferedReader(new InputStreamReader(inputStream));
            // The "key" will be assumed to be the first whole line of the file.
            // Therefore, a StringBuilder is not needed.
            // Read the key, and trim any leading/trailing whitespace.
            saltKey = bufferedFileReader.readLine().trim();
            // Sanity check
            if (null != bufferedFileReader.readLine()) {
                Log.e(LOG_CATEGORY, "onSaltKeyFileSelection(), " +
                      "ERROR: Malformed file with extraneous data");
            }
            Log.i(LOG_CATEGORY, "onSaltKeyFileSelection(), " +
                  "saltKey='" + saltKey + "'");
        } catch (IOException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        } finally {
            if (null != bufferedFileReader) {
                try {
                    bufferedFileReader.close();
                } catch (IOException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Memory Leak! " +
                          "Couldn't close BufferedReader; " +
                          "uri = '" + uri.toString() + "', " +
                          "Caught " + e);
                    e.printStackTrace();
                }
            }
        }

        // Modify the "saltKey"
        // Allow an empty salt key;
        // either someone really tried to do that if a blank got here,
        // or they have a malformed file and they'll come to know.
        if (null != saltKey) {
            saveSaltKeyToSharedPreferences(saltKey);
        }

        // Uncheck the unlockSaltKey checkBox
        uncheckLoadSaltKeyCheckBox();
    }

    // --------------------------------------------------------------------
    // UTILITIES

    /**
     * @brief   
     * @return  Does not return a value
     */
    private void saveSaltKeyToSharedPreferences(String saltKey) {
        Log.i(LOG_CATEGORY, "saveSaltKeyToSharedPreferences(): " +
                "Saving to SharedPreferences saltKey='" + saltKey + "'");
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(
                                        this.getApplicationContext());
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        preferenceEditor.putString(getString(R.string.pref_saltKey_key),
                                   saltKey);
        preferenceEditor.commit();
        // On this commit,
        // the onSharedPreferenceChanged handler will be called
        // This may happen AFTER onStart() and onResume(),
        // if the Activity is currently stopped
        // (say shadowed by the file selection activity).
    }

    /**
     * @brief   
     * @return  Does not return a value
     */
    private void uncheckLoadSaltKeyCheckBox() {
        Log.i(LOG_CATEGORY, "Unchecking the unlockSaltKey checkBox...");
        CheckBox unlockSaltKeyBox = (CheckBox)findViewById(R.id.unlockSaltKey);
        unlockSaltKeyBox.setChecked(false);
        // The CheckBox listener would now be called,
        // disabling the "Load Salt Key" and "Generate Salt Key" buttons
    }

}
