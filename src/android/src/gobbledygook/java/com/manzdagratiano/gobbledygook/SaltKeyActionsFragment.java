/**
 * @file        SaltKeyActionsFragment.java
 * @brief       Source file for the SaltKeyActionsFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 08, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package com.manzdagratiano.gobbledygook;

// Libraries
import com.manzdagratiano.yggdrasil.R;

// Android
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * @brief   
 */
public class SaltKeyActionsFragment extends Fragment {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ===================================================================
    // PUBLIC METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "Creating SaltKeyActions fragment...");
        super.onCreate(savedInstanceState);
    }

    /**
     * @brief   Called when the fragment is ready to display its UI
     * @return  The View representing the root of the fragment layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main layout
        return inflater.inflate(R.layout.saltkey_actions,
                                container,
                                false);
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onResume() {
        Log.i(LOG_CATEGORY, "onResume(): Configuring elements...");
        super.onResume();

        this.configureElements();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onPause() {
        // Clean-up listeners and handlers
        Log.i(LOG_CATEGORY, "onPause(): Deconfiguring elements...");

        this.deconfigureElements();

        super.onStop();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStop() {
        super.onStop();
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
        if (READ_SALT_KEY_FILE_CODE == requestCode &&
            Activity.RESULT_OK == resultCode) {
            Log.i(LOG_CATEGORY, "onActivityResult(): " +
                    "Calling onSaltKeyFileSelection()...");
            this.onSaltKeyFileSelection(resultData.getData());
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    // ===================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY                        =
        "GOBBLEDYGOOK.SALTKEY";
    private static final int    READ_SALT_KEY_FILE_CODE             =
        666;
    private static final int    SALT_KEY_LENGTH                     =
        512;
    private static final String UTF8                                =
        "UTF-8";

    // Toast messages
    private static final String FILE_MANAGER_MISSING_ERROR          =
        "ERROR importing file! Please install a file manager " +
        "to be able to browse to a file";
    private static final String GENERATE_SALT_KEY_MESSAGE           =
        "Load or Generate a new Salt Key";
    private static final String GENERATING_SALT_KEY_MESSAGE         =
        "Generating new salt key...";

    // --------------------------------------------------------------------
    // METHODS

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
                    (CheckBox)getView().findViewById(R.id.unlockSaltKey);
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
                            (EditText)getView().findViewById(
                                                    R.id.saltKey);
                        Button loadSaltKeyButton =
                            (Button)getView().findViewById(
                                                    R.id.loadSaltKey);
                        Button generateSaltKeyButton =
                            (Button)getView().findViewById(
                                                    R.id.generateSaltKey);
                        if (isChecked) {
                            Toast.makeText(
                                    getActivity().getApplicationContext(),
                                    GENERATE_SALT_KEY_MESSAGE,
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

                EditText saltKeyBox =
                    (EditText)getView().findViewById(R.id.saltKey);

                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                                        getActivity().getApplicationContext());

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
                Button loadSaltKeyButton =
                    (Button)getView().findViewById(R.id.loadSaltKey);
                loadSaltKeyButton.setOnClickListener(
                                        new View.OnClickListener() {
                    /**
                     * @brief   The "onClick" callback for
                     *          the "Load Salt Key" button
                     * @return  Does not return a value
                     */
                    @Override
                    public void onClick(View view) {
                        loadSaltKey(view);
                    }
                });

                // Disable the button until "unlockSaltKey" is checked
                loadSaltKeyButton.setEnabled(false);
            }

            /**
             * @brief   
             * @return  
             */
            public void configureGenerateSaltKey() {
                Button generateSaltKeyButton =
                    (Button)getView().findViewById(R.id.generateSaltKey);
                generateSaltKeyButton.setOnClickListener(
                                        new View.OnClickListener() {
                    /**
                     * @brief   The "onClick" callback for
                     *          the "Generate Salt Key" button
                     * @return  Does not return a value
                     */
                    @Override
                    public void onClick(View view) {
                        generateSaltKey(view);
                    }
                });

                // Disable the button until "unlockSaltKey" is checked
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
    private void loadSaltKey(final View view) {
        Log.i(LOG_CATEGORY, "loadSaltKey() handler called...");

        // Open the file picker dialog to select the key file.
        // This requires creating a new "Intent".
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to only show text files
        // TODO: This filter does not seem to have any effect
        // when the ACTION_GET_CONTENT intent type is used
        // as opposed to ACTION_OPEN_DOCUMENT
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
        if (null != intent.resolveActivity(
                                getActivity().getPackageManager())) {
            startActivityForResult(fileChooser,
                                   READ_SALT_KEY_FILE_CODE);
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                           FILE_MANAGER_MISSING_ERROR,
                           Toast.LENGTH_SHORT).show();
        }
        // The callback "onActivityResult" will be called
    }

    /**
     * @brief   
     * @return  
     */
    private void generateSaltKey(final View view) {
        Log.i(LOG_CATEGORY, "generateSaltKey() handler called...");
        Toast.makeText(getActivity().getApplicationContext(),
                       GENERATING_SALT_KEY_MESSAGE,
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
        EditText saltKeyBox = (EditText)getView().findViewById(R.id.saltKey);
        saltKeyBox.setText(saltKey, TextView.BufferType.EDITABLE);

        // Save the saltKey into the preferences object
        saveSaltKeyToSharedPreferences(saltKey);

        // Uncheck the unlockSaltKey checkBox
        uncheckLoadSaltKeyCheckBox();
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
            inputStream =
                getActivity().getContentResolver().openInputStream(uri);
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
                          "uri='" + uri.toString() + "', Caught " + e);
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

    /**
     * @brief   A method to "deconfigure elements", i.e.,
     *          clean up listeners and handlers
     * @return  Does not return a value
     */
    private void deconfigureElements() {
        Log.i(LOG_CATEGORY, "deconfigureElements(): " +
              "Cleaning up listeners/handlers");

        // "Unlock Salt Key" checkbox
        CheckBox unlockSaltKeyBox =
            (CheckBox)getView().findViewById(R.id.unlockSaltKey);
        unlockSaltKeyBox.setOnCheckedChangeListener(null);

        // "Load Salt Key" button
        Button loadSaltKeyButton =
            (Button)getView().findViewById(R.id.loadSaltKey);
        loadSaltKeyButton.setOnClickListener(null);

        // "Generate Salt Key" button
        Button generateSaltKeyButton =
            (Button)getView().findViewById(R.id.generateSaltKey);
        generateSaltKeyButton.setOnClickListener(null);
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
                                    getActivity().getApplicationContext());
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
        CheckBox unlockSaltKeyBox =
            (CheckBox)getView().findViewById(R.id.unlockSaltKey);
        unlockSaltKeyBox.setChecked(false);
        // The CheckBox listener would now be called,
        // disabling the "Load Salt Key" and "Generate Salt Key" buttons
    }

}
