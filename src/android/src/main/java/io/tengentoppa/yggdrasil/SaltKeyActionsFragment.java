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

package io.tengentoppa.yggdrasil;

// Android
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.io.UnsupportedEncodingException;

/**
 * @brief   
 */
public abstract class SaltKeyActionsFragment extends DialogFragment {

    // ===================================================================
    // PUBLIC METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String FUNC = "onCreate()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating SaltKeyActions fragment...");
        super.onCreate(savedInstanceState);

        // Initialize private members
        this.m_showAsDialog = false;

        // Get the input arguments
        Bundle args = this.getArguments();
        if (null != args) {
            this.m_showAsDialog = args.getBoolean(PARAM_DIALOG);
        }
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
     * @brief   Called after onCreateView() to do final initializations.
     *          This is where the "Dialog" of the DialogFragment is created.
     *          Hence, the setShowsDialog() property must be set before
     *          this method in the lifecycle.
     * @return  Does not return a value.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        final String FUNC = "onResume()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring elements...");
        super.onResume();

        // Set the layout properties of the dialog for the fragment.
        // This MUST be done here, i.e, after onActivityCreated()
        // in the lifecycle, else will not take effect.
        if (this.m_showAsDialog) {
            Dialog dialog = this.getDialog();
            if (null != dialog) {
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                 ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                // This should not happen
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "getDialog() returned null!");
            }
        }

        this.configureElements();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onPause() {
        // Clean-up listeners and handlers
        final String FUNC = "onPause()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Deconfiguring elements...");

        this.deconfigureElements();

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

    // ===================================================================
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
        final String LOG_TAG = "SALTKEY";
        return LOG_TAG + "." + FUNC + ": ";
    }

    /**
     * @brief   Method to fill an argument bundle
     *          with the necessary parameters.
     * @return  Does not even.
     */
    protected static void fillBundle(Bundle args,
                                     final boolean showAsDialog) {
        args.putBoolean(PARAM_DIALOG, showAsDialog);
    }

    // ===================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    // Parameter names
    private static final String PARAM_DIALOG                        =
        "dialog";

    // Toast messages
    private static final String GENERATE_SALT_KEY_MESSAGE           =
        "Generate a new salt key. " +
        "(WARNING: This will irreversibly change all generated passwords!)";
    private static final String GENERATING_SALT_KEY_MESSAGE         =
        "Generating new salt key...";
    private static final String SALT_KEY_FAILURE_MESSAGE            =
        "Failed to generate salt key!";

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
            public void configureSaltKey() {
                final String FUNC = "configureSaltKey()";
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "Setting value...");

                EditText saltKeyBox =
                    (EditText)getView().findViewById(R.id.saltKey);

                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(
                                        getActivity().getApplicationContext());

                // Set the summary with the value
                String saltKey = sharedPreferences.getString(
                                    getString(R.string.pref_saltKey_key), "");
                if (saltKey.isEmpty()) {
                    Log.i(getLogCategory(), getLogPrefix(FUNC) +
                          "The salt key is empty");
                } else {
                    Log.i(getLogCategory(), getLogPrefix(FUNC) +
                          "Found non-empty saltKey='" + saltKey + "'");
                    saltKeyBox.setText(saltKey, TextView.BufferType.EDITABLE);
                }

                // Disable the saltKey for editing,
                // it is NOT safe to manually edit it.
                // The only two ways to edit it are to either generate a new key,
                // or import "settings" from a valid JSON configuration file.
                // it should be enabled only when editSaltKey is checked
                saltKeyBox.setEnabled(false);
            }

            /**
             * @brief   
             * @return  
             */
            public void configureEditSaltKey() {
                final String FUNC = "configureEditSaltKey()";
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "Unchecking checkbox...");

                CheckBox editSaltKeyBox =
                    (CheckBox)getView().findViewById(R.id.editSaltKey);
                editSaltKeyBox.setChecked(false);

                // Attach an onCheckedChangeListener
                // (as opposed to an onClickListener,
                // since we'll be unchecking the checkBox from code)
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "Attaching onCheckedChangeListener...");
                editSaltKeyBox.setOnCheckedChangeListener(
                                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view,
                                                boolean isChecked) {
                        EditText saltKeyBox =
                            (EditText)getView().findViewById(
                                                    R.id.saltKey);
                        Button generateSaltKeyButton =
                            (Button)getView().findViewById(
                                                    R.id.generateSaltKey);
                        if (isChecked) {
                            Toast.makeText(
                                    getActivity().getApplicationContext(),
                                    GENERATE_SALT_KEY_MESSAGE,
                                    Toast.LENGTH_SHORT).show();
                            generateSaltKeyButton.setEnabled(true);
                        } else {
                            generateSaltKeyButton.setEnabled(false);
                        }
                    }
                });
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

                // Disable the button until "editSaltKey" is checked
                generateSaltKeyButton.setEnabled(false);
            }

        };  // end class Configurator

        Configurator configurator = new Configurator();

        configurator.configureSaltKey();
        configurator.configureEditSaltKey();
        configurator.configureGenerateSaltKey();
    }

    /**
     * @brief   
     * @return  
     */
    private void generateSaltKey(final View view) {
        final String FUNC = "generateSaltKey()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) + ">>");
        Toast.makeText(getActivity().getApplicationContext(),
                       GENERATING_SALT_KEY_MESSAGE,
                       Toast.LENGTH_SHORT).show();

        // Generate the key
        String saltKey = null;
        try {
            saltKey = Crypto.generateSaltKey();
        } catch (UnsupportedEncodingException e) {
            Log.e(getLogCategory(), FUNC +
                  "ERROR: Caught " + e);
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(),
                           SALT_KEY_FAILURE_MESSAGE,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        // Set the view with the saltKey
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Setting the view with saltKey=" + saltKey);
        EditText saltKeyBox = (EditText)getView().findViewById(R.id.saltKey);
        saltKeyBox.setText(saltKey, TextView.BufferType.EDITABLE);

        // Save the saltKey into the preferences object
        saveSaltKeyToSharedPreferences(saltKey);

        // Uncheck the editSaltKey checkBox
        uncheckEditSaltKeyCheckBox();
    }

    /**
     * @brief   A method to "deconfigure elements", i.e.,
     *          clean up listeners and handlers
     * @return  Does not return a value
     */
    private void deconfigureElements() {
        final String FUNC = "deconfigureElements()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Cleaning up listeners/handlers");

        // "Edit Salt Key" checkbox
        CheckBox editSaltKeyBox =
            (CheckBox)getView().findViewById(R.id.editSaltKey);
        editSaltKeyBox.setOnCheckedChangeListener(null);

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
        final String FUNC = "saveSaltKeyToSharedPreferences()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Saving to SharedPreferences saltKey='" + saltKey + "'");
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        preferenceEditor.putString(getString(R.string.pref_saltKey_key),
                                   saltKey);
        preferenceEditor.apply();
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
    private void uncheckEditSaltKeyCheckBox() {
        final String FUNC = "uncheckEditSaltKeyCheckBox()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Unchecking the editSaltKey checkBox...");
        CheckBox editSaltKeyBox =
            (CheckBox)getView().findViewById(R.id.editSaltKey);
        editSaltKeyBox.setChecked(false);
        // The CheckBox listener would now be called,
        // disabling the "Load Salt Key" and "Generate Salt Key" buttons
    }

    // --------------------------------------------------------------------
    // UTILITIES

    protected boolean    m_showAsDialog;        /** @brief A parameter to
                                                  * indicate if this fragment
                                                  * should be shown
                                                  * as a dialog
                                                  */

}
