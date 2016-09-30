/**
 * @file        WorkhorseFragment.java
 * @brief       Source file for the WorkhorseFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.io.UnsupportedEncodingException;
import java.lang.ClassCastException;
import java.lang.Exception;
import java.lang.Runnable;
import java.lang.Thread;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

// Spongycastle (Bouncycastle)
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.encoders.Base64;

/**
 * @brief   The WorkhorseFragment class.
 *          This is the "main" fragment of the application,
 *          where all the magic happens.
 */
public abstract class WorkhorseFragment extends DialogFragment {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   Called when the fragment is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "onCreate(): Creating workhorse fragment...");
        super.onCreate(savedInstanceState);

        // Initialize the private data members
        this.m_showAsDialog = false;
        this.m_url = "";
        this.m_savedOverrides = null;
        this.m_proposedAttributes = null;
        this.m_customOverrides = null;

        // Get the input arguments
        Bundle args = this.getArguments();
        if (null != args) {
            this.m_showAsDialog = args.getBoolean(PARAM_DIALOG);
            this.m_url = args.getString(PARAM_URL);
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
        return inflater.inflate(R.layout.workhorse,
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
        Log.i(LOG_CATEGORY, "onResume(): Configuring elements...");

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
                Log.e(LOG_CATEGORY, "getDialog() returned null!");
            }
        }

        // Configure elements here;
        // this is to ensure we pick up changes made when
        // the user navigated to a different fragment
        // and changed any essential settings
        // and this fragment was added to the back stack
        this.configureElements();
    }

    /**
     * @brief   Called when the activity is partially covered by another.
     *          Perform any cleanup here - symmetric to onResume
     * @return  Does not return a value
     */
    @Override
    public void onPause() {
        // Perform any cleanup here
        Log.i(LOG_CATEGORY, "onPause(): Deconfiguring elements...");

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

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    protected static final String LOG_CATEGORY      = "YGGDRASIL.WORKHORSE";
    protected static final String SHA256            = "SHA-256";
    protected static final String UTF8              = "UTF-8";

    // Parameter names
    protected static final String PARAM_DIALOG      = "dialog";
    protected static final String PARAM_URL         = "url";

    // Toast messages
    protected static final String ATTRIBUTES_OVERRIDE_SPARINGLY_MESSAGE
                                                    =
        "Please use the custom attributes option sparingly! " +
        "It should only be a last resort.";
    protected static final String ATTRIBUTES_SAVE_FAILURE_MESSAGE
                                                    =
        "Alas! Failed to save custom attributes!";
    protected static final String ATTRIBUTES_SAVE_SUCCESS_MESSAGE
                                                    =
        "Successfully saved custom attributes!";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   
     * @return  
     */
    protected void configureElements() {

        // ----------------------------------------------------------------
        // The Configurator class

        /**
         * @brief   The Configurator class, which reads the saved preferences
         * and configures the UI elements upon load.
         */
        class Configurator {

            /**
             * @brief   
             * @return  
             */
            public String extractUrlFromClipboard() {
                String url = "";
                ClipboardManager clipboard =
                    (ClipboardManager)getActivity().getSystemService(
                            Context.CLIPBOARD_SERVICE);
                if (clipboard.hasPrimaryClip()) {
                    ClipData.Item clipItem =
                        clipboard.getPrimaryClip().getItemAt(0);
                    CharSequence clipText = clipItem.getText();
                    if (null != clipText) {
                        url = clipText.toString();
                    }
                }

                return url;
            }

            /**
             * @brief   
             * @return  
             */
            public String extractDomain(String url) {
                String domain = url;
                if (null == url) {
                    return "";
                }

                // Chop off the beginning "http://" or"https://"
                // or "ftp://" or whatever, if it exists.
                if (url.contains("://")) {
                    domain = url.split("://")[1];
                }

                // Obtain the root of the URL - the domain-subdomain
                domain = domain.split("/")[0];

                // Chop off the begin "www." or "m."
                // to uniquely identify across web or mobile.
                if (domain.startsWith("www.")) {
                    domain = domain.substring("www.".length(), domain.length());
                } else if (domain.startsWith("m.")) {
                    domain = domain.substring("m.".length(), domain.length());
                }

                return domain;
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public String configureDomain(String domain,
                                          String savedDomain) {
                EditText domainField =
                    (EditText)getView().findViewById(R.id.domain);
                String theDomain = (null == savedDomain) ? domain : savedDomain;
                domainField.setText(theDomain,
                                    TextView.BufferType.EDITABLE);
                return theDomain;
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public Integer configureIterations(Integer defaultIterations,
                                               Integer savedIterations) {
                EditText iterationsField =
                    (EditText)getView().findViewById(R.id.iterations);
                Integer theIterations =
                    ((null == savedIterations) ? defaultIterations : savedIterations);
                iterationsField.setText(theIterations.toString(),
                                        TextView.BufferType.EDITABLE);
                return theIterations;
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public Integer configureTruncation(Integer truncation) {
                EditText truncationField =
                    (EditText)getView().findViewById(R.id.truncation);
                // The truncation is never null
                truncationField.setText(truncation.toString(),
                                        TextView.BufferType.EDITABLE);
                CheckBox truncateBox =
                    (CheckBox)getView().findViewById(R.id.truncate);

                // If the truncation value is NO_TRUNCATION,
                // then this option is as good as inactive
                if (truncation.equals(Attributes.NO_TRUNCATION)) {
                    truncationField.setEnabled(false);
                    truncateBox.setChecked(false);
                }

                // Attach a listener to the "Truncate" checkbox
                truncateBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText truncationField = 
                            (EditText)getView().findViewById(R.id.truncation);
                        CheckBox truncateBox = (CheckBox)view; 
                        truncationField.setEnabled(truncateBox.isChecked());
                    }
                });

                return truncation;
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public void configureHash() {
                // Disable the "proxy password" field
                EditText hashField =
                    (EditText)getView().findViewById(R.id.hash);
                hashField.setEnabled(false);

                // Attach a listener to the "Show Hash" checkbox
                CheckBox showHashBox =
                    (CheckBox)getView().findViewById(R.id.showHash);
                showHashBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText hashField = 
                            (EditText)getView().findViewById(R.id.hash);
                        CheckBox showHashBox = (CheckBox)view; 
                        if (showHashBox.isChecked()) {
                            hashField.setInputType(
                                        InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            hashField.setEnabled(true);
                        } else {
                            hashField.setInputType(
                                        InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            hashField.setEnabled(false);
                        }
                    }
                });
            }

            /**
             * @brief   
             * @return  Does not return a value
             */
            public void configureSaveCustomOverridesCheckBox() {
                CheckBox saveOverridesBox =
                    (CheckBox)getView().findViewById(R.id.saveOverrides);
                saveOverridesBox.setOnClickListener(new
                                                     View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox saveOverridesBox = (CheckBox)view; 
                        if (saveOverridesBox.isChecked()) {
                            Toast.makeText(
                                    getActivity().getApplicationContext(),
                                    ATTRIBUTES_OVERRIDE_SPARINGLY_MESSAGE,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            /**
             * @brief   Routine to configure the "Generate" button
             *          by attaching an "onClick" listener to it
             * @return  Does not return a value
             */
            public void configureGenerateButton() {
                Button generateButton =
                    (Button)getView().findViewById(R.id.generate);
                generateButton.setOnClickListener(
                                    new View.OnClickListener() {
                    /**
                     * @brief   The "onClick" callback for
                     *          the "Generate" button
                     * @return  Does not return a value
                     */
                    @Override
                    public void onClick(View view) {
                        generate(view);
                    }
                });
            }

        }   // end class Configurator

        // ----------------------------------------------------------------
        // Retrieve the "ingredients"
        Ingredients ingredients = this.retrieveIngredients();
        Log.d(LOG_CATEGORY, "ingredients=" + ingredients.toString());

        // ----------------------------------------------------------------
        // Create the "actors"

        Configurator configurator = new Configurator();

        // ----------------------------------------------------------------
        // Read the url from the clipboard

        // Log.i(LOG_CATEGORY, "Reading url from clipboard...");
        // String url = configurator.extractUrlFromClipboard();
        Log.i(LOG_CATEGORY, "url='" + m_url + "'");

        // ----------------------------------------------------------------
        // Extract the domain from the url

        String domain = configurator.extractDomain(m_url);
        Log.i(LOG_CATEGORY, "domain='" + domain + "'");

        // ----------------------------------------------------------------
        // Saved and Proposed Attributes

        // Obtain the decoded encodedOverridesMap.
        JSONObject encodedOverridesMap =
            AttributesCodec.getEncodedOverridesMap(
                                ingredients.encodedOverrides());

        // Obtain the saved attributes for this domain, if any
        Attributes savedOverrides =
            AttributesCodec.getDomainOverrides(domain,
                                               encodedOverridesMap);
        Log.i(LOG_CATEGORY,
              "savedOverrides='" +
              AttributesCodec.encode(savedOverrides) + "'");

        // The "proposed" attributes,
        // which would be used to generate the proxy password,
        // unless overridden
        Attributes proposedAttributes =
            new Attributes(
                configurator.configureDomain(
                                    domain,
                                    savedOverrides.domain()),
                configurator.configureIterations(
                                    ingredients.defaultIterations(),
                                    savedOverrides.iterations()),
                configurator.configureTruncation(
                                    savedOverrides.truncation()));

        configurator.configureHash();
        configurator.configureSaveCustomOverridesCheckBox();
        configurator.configureGenerateButton();

        // Save the saved and proposed attributes,
        // as well as the customOverrides list,
        // in the class for recalling later;
        // need to save state here since the next call will be
        // the invocation of a handler via user interaction
        this.m_saltKey = ingredients.saltKey();
        this.m_savedOverrides = savedOverrides;
        this.m_proposedAttributes = proposedAttributes;
        this.m_customOverrides = encodedOverridesMap;
    }

    /**
     * @brief   Method to retrieve the saved "ingredients"
     *          for the recipe.
     *          This method will be suitably overridden in
     *          the respective flavor implementation.
     * @return  Does not return a value
     */
    protected Ingredients retrieveIngredients() {
        // Do nothing; this must be overridden in the appropriate flavor.
        // Return null to cause an intentional crash if not overridden.
        return null;
    }

    /**
     * @brief   A method to retrieve ingredients from SharedPreferences
     *          This can either be the default method of retrieving
     *          ingredients, or the fallback.
     * @return  Does not return a value
     */
    protected Ingredients retrieveIngredientsFromSharedPreferences() {
        // The SharedPreferences handle
        Log.i(LOG_CATEGORY, "Reading saved preferences...");
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        // The salt key 
        String saltKey = "";
        Integer defaultIterations = Attributes.DEFAULT_ITERATIONS;
        String encodedOverrides = "";
        // Catch all exceptions when reading from SharedPreferences
        try {
            saltKey = sharedPrefs.getString(
                    getString(R.string.pref_saltKey_key),
                              "");
            // The default number of PBKDF2 iterations.
            // Since the preference is an EditTextPreference,
            // even though it has the numeric attribute,
            // and is entered as an integer, it is saved as a string.
            // Therefore, it must be retrieved as a string and then cast.
            String defaultIterationsStr = sharedPrefs.getString(
                    getString(R.string.pref_defaultIterations_key),
                              "");
            // If non-empty, parse as an Integer.
            // The empty case will leave defaultIterations at
            // Attributes.DEFAULT_ITERATIONS.
            if (!defaultIterationsStr.isEmpty()) {
                defaultIterations = Integer.parseInt(defaultIterationsStr);
            }
            // The encoded customOverrides
            encodedOverrides = sharedPrefs.getString(
                    getString(R.string.pref_customOverrides_key),
                              "");
        } catch (Exception e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        Ingredients ingredients = new Ingredients(saltKey,
                                                  defaultIterations,
                                                  encodedOverrides);
        return ingredients;
    }

    /**
     * @brief   Routine to generate the "proxy" password
     *          from the domain name using the user password
     *          and the salt key
     * @return  Does not return a value
     */
    protected void generate(final View view) {
        Log.i(LOG_CATEGORY, "Generating proxy password...");
        final Attributes attributes = getAttributes(view);
        Log.i(LOG_CATEGORY,
              "attributes='" + AttributesCodec.encode(attributes) + "'");

        final byte[] seedSHA =
            Crypto.getSeedSHA(
                ((EditText)view.findViewById(R.id.password))
                                    .getText().toString());
        if (null == seedSHA) {
            Log.e(LOG_CATEGORY, "ERROR: seedSHA.generation.failure");
            return;
        }
        try {
            Log.d(LOG_CATEGORY,
                  "seedSHA=" + (new String(Hex.encode(seedSHA),
                                           UTF8)));
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        // The salt key
        final String saltKey = m_saltKey;
        // Sanity check
        if (saltKey.isEmpty()) {
            Log.e(LOG_CATEGORY, "FATAL: saltKey=null");
            // Continue, but the results will be bogus.
        }

        // Do the heavy lifting in a separate thread.
        // This involves:
        // a) generate the salt from the saltKey and the domain,
        // b) generate the proxy password from the salt and the password
        // Create the new Thread object and start it.
        new Thread(new Runnable () {
            public void run() {
                // Generate the salt
                byte[] salt = Crypto.generateSalt(attributes.domain(),
                                                  saltKey,
                                                  attributes.iterations());
                if (null == salt) {
                    String errMsg = "Salt.Generation.Failure";
                    Log.e(LOG_CATEGORY, errMsg);
                    setPassword(view, errMsg);
                    return;
                }
                try {
                    Log.i(LOG_CATEGORY,
                          "salt=" + (new String(Base64.encode(salt),
                                                UTF8)));
                } catch (UnsupportedEncodingException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                }

                // Generate the hash (the "proxy password")
                String b64Hash =
                    Crypto.generateHash(seedSHA,
                                        salt,
                                        attributes.iterations(),
                                        attributes.specialCharsFlag());
                if (null == b64Hash) {
                    String errMsg = "Hash.Generation.Failure";
                    Log.e(LOG_CATEGORY, errMsg);
                    setPassword(view, errMsg);
                    return;
                }
                Log.i(LOG_CATEGORY, "hash=" + b64Hash);
                final String password =
                    Crypto.getPasswdStr(b64Hash,
                                        attributes.truncation(),
                                        attributes.specialCharsFlag());
                Log.i(LOG_CATEGORY, "password=" + password);

                // Post the results to the UI thread for manipulation
                // (using "runOnUiThread" from the "Activity" class)
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setPassword(view, password);
                        checkAndSaveOverrides(attributes);
                    }
                });
            }
        }).start();
    }

    /**
     * @brief   A method to get an Attributes object created from
     *          the elements of the view after the user has potentially
     *          changed any elements and hit "Generate!" to generate the
     *          proxy password.
     * @return  An Attributes object initialized from elements of the view
     */
    protected Attributes getAttributes(View view) {
        EditText domainField     =
            (EditText)getView().findViewById(R.id.domain);
        EditText iterationsField =
            (EditText)getView().findViewById(R.id.iterations);
        EditText truncationField =
            (EditText)getView().findViewById(R.id.truncation);

        String domain = domainField.getText().toString();
        Integer iterations = null;
        try {
            iterations =
                ((0 == iterationsField.getText().toString().trim().length()) ?
                 Attributes.DEFAULT_ITERATIONS :
                 Integer.parseInt(iterationsField.getText().toString()));
        } catch (ClassCastException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
            iterations = Attributes.DEFAULT_ITERATIONS;
        }

        Integer truncation = null;
        try {
            truncation =
                ((0 == truncationField.getText().toString().trim().length()) ?
                 Attributes.NO_TRUNCATION :
                 Integer.parseInt(truncationField.getText().toString()));
        } catch (ClassCastException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
            truncation = Attributes.NO_TRUNCATION;
        }

        Attributes attributes = new Attributes(domain,
                                               iterations,
                                               truncation);

        return attributes;
    }

    /**
     * @brief   
     * @return  
     */
    protected void setPassword(View view,
                               String password) {
        EditText hashField = (EditText)getView().findViewById(R.id.hash);
        hashField.setEnabled(true);
        hashField.setText(password, TextView.BufferType.EDITABLE);
    }

    /**
     * @brief   
     * @return  
     */
    protected void checkAndSaveOverrides(final Attributes attributes) {
        CheckBox saveOverridesBox =
            (CheckBox)getView().findViewById(R.id.saveOverrides);
        if (!saveOverridesBox.isChecked()) {
            Log.i(LOG_CATEGORY, "checkAndSaveOverrides(): " +
                  "Not asked to save overrides. Nothing to do...");
            return;
        }

        Log.i(LOG_CATEGORY, "checkAndSaveOverrides(): " +
              "Checking if modified attributes exist...");
        Attributes overridesToSave =
            AttributesCodec.getOverridesToSave(attributes,
                                               this.m_savedOverrides,
                                               this.m_proposedAttributes);
        String encodedOverrides = AttributesCodec.encode(overridesToSave);
        Log.i(LOG_CATEGORY, "overridesToSave='" + encodedOverrides + "'");

        if (!overridesToSave.attributesExist()) {
            Log.i(LOG_CATEGORY, "No custom changes to save...");
            return;
        }

        // Add this encoded string to the customOverrides
        // which, upto this point, could have been null
        if (null == m_customOverrides) {
            m_customOverrides = new JSONObject();
        }

        // Add new or update existing
        try {
            m_customOverrides.put(attributes.domain(),
                                  encodedOverrides);
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            Toast.makeText(getActivity().getApplicationContext(),
                           ATTRIBUTES_SAVE_FAILURE_MESSAGE,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(LOG_CATEGORY, "checkAndSaveOverrides(): " +
              "new customOverrides=" + m_customOverrides.toString());

        // Stringify the JSON for saving in the default SharedPreferences
        String encodedOverridesList = m_customOverrides.toString();

        // Save the stringified JSON to SharedPreferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        preferenceEditor.putString(
                getString(R.string.pref_customOverrides_key),
                encodedOverridesList);
        preferenceEditor.apply();
        // The OnSharedPreferenceChangedHandler will be called

        Toast.makeText(getActivity().getApplicationContext(),
                       ATTRIBUTES_SAVE_SUCCESS_MESSAGE,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * @brief   A method to "deconfigure elements", i.e.,
     *          clean up listeners and handlers
     * @return  Does not return a value
     */
    protected void deconfigureElements() {
        Log.i(LOG_CATEGORY, "deconfigureElements(): " +
              "Cleaning up listeners/handlers");

        // "Truncate" checkbox
        CheckBox truncateBox = 
            (CheckBox)getView().findViewById(R.id.truncate);
        truncateBox.setOnClickListener(null);

        // "Show Hash" checkbox
        CheckBox showHashBox = 
            (CheckBox)getView().findViewById(R.id.showHash);
        showHashBox.setOnClickListener(null);

        // "Save custom overrides" checkbox
        CheckBox saveOverridesBox =
            (CheckBox)getView().findViewById(R.id.saveOverrides);
        saveOverridesBox.setOnClickListener(null);

        // "Generate" button
        Button generateButton =
            (Button)getView().findViewById(R.id.generate);
        generateButton.setOnClickListener(null);
    }

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @brief   The Ingredients class
     *          This class is used to retrieve/pass around the
     *          "ingredients" that go into the "recipe", i.e.,
     *          the necessary input to generate passwords.
     */
    protected class Ingredients {

        // ================================================================
        // Creators

        /**
         * @brief   The constructor.
         * @return  Does not return a value.
         */
        public Ingredients() {
            m_saltKey = "";
            m_defaultIterations = Attributes.DEFAULT_ITERATIONS;
            m_encodedOverrides = "";
        }

        public Ingredients(String saltKey,
                           Integer defaultIterations,
                           String encodedOverrides) {
            m_saltKey = saltKey;
            m_defaultIterations = defaultIterations;
            m_encodedOverrides = encodedOverrides;
        }

        // ================================================================
        // Accessors

        public final String saltKey() {
            return m_saltKey;
        }

        public final Integer defaultIterations() {
            return m_defaultIterations;
        }

        public final String encodedOverrides() {
            return m_encodedOverrides;
        }

        /**
         * @brief   Method to return a textual representation of the
         *          Ingredients object.
         *          This method cannot return a "final" type, since it
         *          needs to override the toString() method in the
         *          Object class.
         * @return  {String} Returns a string with the representation.
         */
        public String toString() {
            return ("[ " + 
                    "saltKey='" +
                    m_saltKey + "', " +
                    "defaultIterations=" +
                    m_defaultIterations.toString() + ", " +
                    "customOverrides='" +
                    m_encodedOverrides + "' ]");
        }

        // ================================================================
        // Modifiers

        public void setSaltKey(String saltKey) {
            m_saltKey = saltKey;
        }

        public void setDefaultIterations(Integer defaultIterations) {
            m_defaultIterations = defaultIterations;
        }

        public void setEncodedOverrides(String encodedOverrides) {
            m_encodedOverrides = encodedOverrides;
        }

        // ================================================================
        // Private members

        private String  m_saltKey;
        private Integer m_defaultIterations;
        private String  m_encodedOverrides;
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS

    protected boolean    m_showAsDialog;        /** @brief A parameter to
                                                  * indicate if this fragment
                                                  * should be shown
                                                  * as a dialog
                                                  */
    protected String     m_url;                 /** @brief The url
                                                  * being operated upon
                                                  */
    protected String     m_saltKey;             /** @brief The salt key
                                                  * retrieved from the
                                                  * default
                                                  * shared preferences/
                                                  * server
                                                  */
    protected Attributes m_savedOverrides;      /** @brief Attributes saved
                                                  * in the preferences system
                                                  */
    protected Attributes m_proposedAttributes;  /** @brief Proposed
                                                  * attributes,
                                                  * after reading from the
                                                  * saved preferences
                                                  * and what would be used if
                                                  * further unmodified
                                                  * by the user
                                                  */
    protected JSONObject m_customOverrides;     /** @brief The saved JSON
                                                  * of custom
                                                  * website overrides
                                                  */
}
