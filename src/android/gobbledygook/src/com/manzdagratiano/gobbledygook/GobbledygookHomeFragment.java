/**
 * @file        GobbledygookHomeFragment.java
 * @brief       Source file for the GobbledygookHomeFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package com.manzdagratiano.gobbledygook;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

// Spongycastle (Bouncycastle)
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.encoders.Base64;

/**
 * @brief   The GobbledygookHomeFragment class.
 *          This is the "main" fragment of the application,
 *          where all the magic happens.
 */
public class GobbledygookHomeFragment extends Fragment {

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
        Log.i(LOG_CATEGORY, "onCreate(): Creating home activity...");
        super.onCreate(savedInstanceState);

        // Nullify the private data members
        this.m_savedAttributes = null;
        this.m_proposedAttributes = null;
        this.m_siteAttributesList = null;
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
        return inflater.inflate(R.layout.main,
                                container,
                                false);
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

    private static final String LOG_CATEGORY            = "GOBBLEDYGOOK";
    private static final String SHA256                  = "SHA-256";
    private static final String UTF8                    = "UTF-8";

    // Toast messages
    private static final String ATTRIBUTES_OVERRIDE_SPARINGLY_MESSAGE
                                                        = "Please use the " +
        "custom attributes option sparingly! " +
        "It should only be a last resort.";
    private static final String ATTRIBUTES_SAVE_FAILURE_MESSAGE
                                                        = "Alas! Failed to " +
        "save custom attributes!";
    private static final String ATTRIBUTES_SAVE_SUCCESS_MESSAGE
                                                        = "Successfully " +
        "saved custom attributes!";
    private static final String EMPTY_SALT_KEY_MESSAGE  = "The salt key " +
        "is empty, therefore the generated password is meaningless. " +
        "Please generate/load a salt key first.";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   
     * @return  
     */
    private void configureElements() {

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
                if (Attributes.NO_TRUNCATION == truncation) {
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
            public void configureSaveCustomAttributesCheckBox() {
                CheckBox saveAttributesBox =
                    (CheckBox)getView().findViewById(R.id.saveAttributes);
                saveAttributesBox.setOnClickListener(new
                                                     View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox saveAttributesBox = (CheckBox)view; 
                        if (saveAttributesBox.isChecked()) {
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
        // Retrieve saved preferences

        // The SharedPreferences handle
        Log.i(LOG_CATEGORY, "Reading saved preferences...");
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        // The salt key 
        String saltKey = "";
        Integer defaultIterations = Attributes.DEFAULT_ITERATIONS;
        String encodedAttributesList = "";
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
            // The encoded siteAttributesList
            encodedAttributesList = sharedPrefs.getString(
                    getString(R.string.pref_siteAttributesList_key),
                              "");
        } catch (Exception e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        Log.d(LOG_CATEGORY,
              "savedPreferences=[ " +
              "saltKey='" + saltKey + "', " +
              "defaultIterations=" + defaultIterations.toString() + ", " +
              "siteAttributesList='" + encodedAttributesList + "' ]");

        // ----------------------------------------------------------------
        // Create the "actors"

        AttributesCodec codec = new AttributesCodec();
        Configurator configurator = new Configurator();

        // ----------------------------------------------------------------
        // Read the url from the clipboard

        Log.i(LOG_CATEGORY, "Reading url from clipboard...");
        String url = configurator.extractUrlFromClipboard();
        Log.i(LOG_CATEGORY, "url='" + url + "'");

        // ----------------------------------------------------------------
        // Extract the domain from the url

        String domain = configurator.extractDomain(url);
        Log.i(LOG_CATEGORY, "domain='" + domain + "'");

        // ----------------------------------------------------------------
        // Saved and Proposed Attributes

        // Obtain the decoded siteAttributesList
        // We'll need it later on
        JSONObject siteAttributesList =
            codec.getDecodedAttributesList(encodedAttributesList);

        // Obtain the saved attributes for this domain, if any
        Attributes savedAttributes =
            codec.getSavedAttributes(domain,
                                     siteAttributesList);
        Log.i(LOG_CATEGORY,
              "savedAttributes='" + codec.encode(savedAttributes) + "'");

        // The "proposed" attributes,
        // which would be used to generate the proxy password,
        // unless overridden
        Attributes proposedAttributes =
            new Attributes(
                configurator.configureDomain(
                                    domain,
                                    savedAttributes.domain()),
                configurator.configureIterations(
                                    defaultIterations,
                                    savedAttributes.iterations()),
                configurator.configureTruncation(
                                    savedAttributes.truncation()));

        configurator.configureHash();
        configurator.configureSaveCustomAttributesCheckBox();
        configurator.configureGenerateButton();

        // Save the saved and proposed attributes,
        // as well as the siteAttributes list,
        // in the class for recalling later;
        // need to save state here since the next call will be
        // the invocation of a handler via user interaction
        this.m_saltKey = saltKey;
        this.m_savedAttributes = savedAttributes;
        this.m_proposedAttributes = proposedAttributes;
        this.m_siteAttributesList = siteAttributesList;
    }

    /**
     * @brief   Routine to generate the "proxy" password
     *          from the domain name using the user password
     *          and the salt key
     * @return  Does not return a value
     */
    private void generate(final View view) {
        Log.i(LOG_CATEGORY, "Generating proxy password...");
        AttributesCodec codec = new AttributesCodec();
        final Attributes attributes = getAttributes(view);
        Log.i(LOG_CATEGORY,
              "attributes='" + codec.encode(attributes) + "'");

        final byte[] seedSHA = getSeedSHA(view);
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
        if (saltKey.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(),
                           EMPTY_SALT_KEY_MESSAGE,
                           Toast.LENGTH_SHORT).show();
        }

        // Do the heavy lifting in a separate thread.
        // This involves:
        // a) generate the salt from the saltKey and the domain,
        // b) generate the proxy password from the salt and the password
        // Create the new Thread object and start it.
        new Thread(new Runnable () {
            public void run() {
                // Generate the salt
                byte[] salt = Hasher.generateSalt(attributes.domain(),
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
                String b64Hash = Hasher.generateHash(seedSHA,
                                                     salt,
                                                     attributes.iterations());
                if (null == b64Hash) {
                    String errMsg = "Hash.Generation.Failure";
                    Log.e(LOG_CATEGORY, errMsg);
                    setPassword(view, errMsg);
                    return;
                }
                Log.i(LOG_CATEGORY, "hash=" + b64Hash);
                final String password =
                    Hasher.getPasswdStr(b64Hash,
                                        attributes.truncation());
                Log.i(LOG_CATEGORY, "password=" + password);

                // Post the results to the UI thread for manipulation
                // (using "runOnUiThread" from the "Activity" class)
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setPassword(view, password);
                        checkAndSaveAttributes(attributes);
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
    private Attributes getAttributes(View view) {
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
     * @brief   A routine to calculate the SHA256 hash of the user's
     *          one true password.
     *          This is the only routine that sees the user's password;
     *          for security, it is not even passed around.
     * @return  The SHA256 hash of the user's password
     */
    private byte[] getSeedSHA(View view) {
        EditText password = (EditText)getView().findViewById(R.id.password);
        byte[] seedSHA = null;
        MessageDigest hash = null;
        try {
            hash = MessageDigest.getInstance(SHA256);
            seedSHA =
                hash.digest(password.getText().toString().getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        return seedSHA;
    }

    /**
     * @brief   
     * @return  
     */
    private void setPassword(View view,
                             String password) {
        EditText hashField = (EditText)getView().findViewById(R.id.hash);
        hashField.setEnabled(true);
        hashField.setText(password, TextView.BufferType.EDITABLE);
    }

    /**
     * @brief   
     * @return  
     */
    private void checkAndSaveAttributes(final Attributes attributes) {
        CheckBox saveAttributesBox =
            (CheckBox)getView().findViewById(R.id.saveAttributes);
        if (!saveAttributesBox.isChecked()) {
            Log.i(LOG_CATEGORY, "checkAndSaveAttributes(): " +
                  "Not asked to save attributes. Nothing to do...");
            return;
        }

        AttributesCodec codec = new AttributesCodec();
        Log.i(LOG_CATEGORY, "checkAndSaveAttributes(): " +
              "Checking if modified attributes exist...");
        Attributes attributesToSave =
            codec.getEncodedAttributesToSave(attributes,
                                             this.m_savedAttributes,
                                             this.m_proposedAttributes);
        String encodedAttributes = codec.encode(attributesToSave);
        Log.i(LOG_CATEGORY, "attributesToSave='" + encodedAttributes + "'");

        if (!attributesToSave.attributesExist()) {
            Log.i(LOG_CATEGORY, "No custom changes to save...");
            return;
        }

        // Add this encoded string to the siteAttributesList
        // which, upto this point, could have been null
        if (null == m_siteAttributesList) {
            m_siteAttributesList = new JSONObject();
        }

        // Add new or update existing
        try {
            m_siteAttributesList.put(attributes.domain(),
                                     encodedAttributes);
        } catch (JSONException e) {
            Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
            Toast.makeText(getActivity().getApplicationContext(),
                           ATTRIBUTES_SAVE_FAILURE_MESSAGE,
                           Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(LOG_CATEGORY, "checkAndSaveAttributes(): " +
              "new siteAttributesList=" + m_siteAttributesList.toString());

        // Stringify the JSON for saving in the default SharedPreferences
        String encodedAttributesList = m_siteAttributesList.toString();

        // Save the stringified JSON to SharedPreferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext());
        SharedPreferences.Editor preferenceEditor = preferences.edit();
        preferenceEditor.putString(
                getString(R.string.pref_siteAttributesList_key),
                encodedAttributesList);
        preferenceEditor.commit();
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
    private void deconfigureElements() {
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

        // "Save Custom Attributes" checkbox
        CheckBox saveAttributesBox =
            (CheckBox)getView().findViewById(R.id.saveAttributes);
        saveAttributesBox.setOnClickListener(null);

        // "Generate" button
        Button generateButton =
            (Button)getView().findViewById(R.id.generate);
        generateButton.setOnClickListener(null);
    }

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @brief   
     */
    private class Attributes {

        // ================================================================
        // Static Members

        public static final int DEFAULT_ITERATIONS = 10000;
        public static final int NO_TRUNCATION      = -1;

        // ================================================================
        // Public Methods

        // ----------------------------------------------------------------
        // Constructors

        public Attributes() {
            m_domain = null;
            m_iterations = null;
            m_truncation = Attributes.NO_TRUNCATION;
        }

        /**
         * @brief   
         * @return  
         */
        public Attributes(String domain, Integer... opts) {
            m_domain     = domain;
            m_iterations = ((opts.length > 0) ? opts[0] : null);
            m_truncation =
                ((opts.length > 1) ?
                 ((null == opts[1]) ?
                  Attributes.NO_TRUNCATION : opts[1]) : null);
        }

        // ----------------------------------------------------------------
        // Accessors

        /**
         * @brief   
         * @return  
         */
        public String domain() {
            return m_domain;
        }

        /**
         * @brief   
         * @return  
         */
        public Integer iterations() {
            return m_iterations;
        }

        /**
         * @brief   
         * @return  
         */
        public Integer truncation() {
            return m_truncation;
        }

        // ----------------------------------------------------------------
        // Mutators

        /**
         * @brief   
         * @return  
         */
        public void setDomain(String domain) {
            this.m_domain = domain;
        }

        /**
         * @brief   
         * @return  
         */
        public void setIterations(Integer iterations) {
            this.m_iterations = iterations;
        }

        /**
         * @brief   
         * @return  
         */
        public void setTruncation(Integer truncation) {
            // Sanity check
            if (null == truncation) {
                Log.e(LOG_CATEGORY, "ERROR: " +
                      "Thwarted attempt to set truncation to null!");
                return;
            }
            this.m_truncation = truncation;
        }

        // ----------------------------------------------------------------
        // Utilities

        /**
         * @brief   A method to check if this Attributes object is not the
         *          default initialized object
         * @return  Returns true or false
         */
        public boolean attributesExist() {
            return ((null != m_domain) &&
                    (null != m_iterations) &&
                    (NO_TRUNCATION != m_truncation));
        }

        // ----------------------------------------------------------------
        // Data Members

        private String  m_domain;       /** @brief The website domain-subdomain
                                          */
        private Integer m_iterations;   /** @brief The number of PBKDF2
                                          * iterations
                                          */
        private Integer m_truncation;   /** @brief The truncation size
                                          * for the generated password
                                          */
    }

    // --------------------------------------------------------------------
    // AttributesCodec

    /**
     * @brief   
     * @return  
     */
    private class AttributesCodec {

        /**
         * @brief   
         */
        static final String DELIMITER = "|";

        /**
         * @brief   
         * @return  
         */
        public String encode(Attributes attributes) {
            if (attributes.attributesExist()) {
                return
                    ((null != attributes.domain()) ?
                     attributes.domain() : "") +
                     AttributesCodec.DELIMITER +
                    ((null != attributes.iterations()) ?
                     attributes.iterations().toString() : "") +
                    AttributesCodec.DELIMITER +
                    ((Attributes.NO_TRUNCATION != attributes.truncation()) ?
                     attributes.truncation().toString() : "");
            } 

            return "";
        }

        /**
         * @brief   
         * @return  
         */
        public Attributes decode(String encodedAttributes) {
            // Create a default-initialized Attributes object
            Attributes attributes = new Attributes();
            Log.d(LOG_CATEGORY, "AttributesCodec.decode(): Decoding " +
                  ((null == encodedAttributes) ?
                   "null" : "'" + encodedAttributes + "'") + " ...");

            // Sanity checks
            if (null == encodedAttributes) {
                return attributes;
            }
            if (encodedAttributes.isEmpty()) {
                return attributes;
            }

            String[] siteAttributesArray =
                encodedAttributes.split(AttributesCodec.DELIMITER);
            // Sanity check for length of the split array
            if (3 != siteAttributesArray.length) {
                Log.e(LOG_CATEGORY, "ERROR: Malformed Attributes! " +
                        "(Expected <domain|iterations|truncation>)");
                return attributes;
            }

            if (siteAttributesArray[0] != "") {
                attributes.setDomain(siteAttributesArray[0]);
            }
            if (siteAttributesArray[1] != "") {
                try {
                    attributes.setIterations(
                            Integer.parseInt(siteAttributesArray[1]));
                } catch (ClassCastException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                    attributes.setIterations(Attributes.DEFAULT_ITERATIONS);
                }
            }
            if (siteAttributesArray[2] != "") {
                try {
                    attributes.setTruncation(
                            Integer.parseInt(siteAttributesArray[2]));
                } catch (ClassCastException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                    attributes.setTruncation(Attributes.NO_TRUNCATION);
                }
            }

            return attributes;
        }

        /**
         * @brief   Function to decode the saved JSON string of custom
         *          website attributes
         * @return  The decoded JSONObject if the encodedAttributesList
         *          was a valid JSON string, else null
         */
        public JSONObject
        getDecodedAttributesList(String encodedAttributesList) {
            Log.i(LOG_CATEGORY, "Decoding saved attributes list...");

            JSONObject siteAttributesList = null;

            // Sanity check - short-circuit evaluation
            if (null != encodedAttributesList &&
                !encodedAttributesList.isEmpty()) {
                try {
                    siteAttributesList =
                        new JSONObject(encodedAttributesList);
                    Log.d(LOG_CATEGORY, "siteAttributesList=" +
                          siteAttributesList.toString());
                } catch (JSONException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                }
            }

            return siteAttributesList;
        }

        /**
         * @brief   
         * @return  A valid Attributes object
         * (default initialized if no saved attributes exist)
         */
        public Attributes
        getSavedAttributes(String domain,
                           JSONObject siteAttributesList) {
            Log.i(LOG_CATEGORY, "Fetching saved attributes...");

            String encodedAttributes = null;

            if (null != siteAttributesList) {
                try {
                    if (siteAttributesList.has(domain)) {
                        encodedAttributes = siteAttributesList.getString(domain);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                }
            }

            Attributes decodedAttributes = this.decode(encodedAttributes);
            return decodedAttributes;
        }

        /**
         * @brief   
         * @return  A valid Attributes object,
         *          which can be default initialized
         */
        public Attributes 
        getEncodedAttributesToSave(final Attributes attributes,
                                   final Attributes savedAttributes,
                                   final Attributes proposedAttributes) {
            Attributes attributesToSave = new Attributes();

            if (savedAttributes.attributesExist()) {
                attributesToSave.setDomain(
                    (attributes.domain() !=
                     proposedAttributes.domain()) ?
                    attributes.domain() :
                    (null != savedAttributes.domain() ?
                     savedAttributes.domain() : null));
                attributesToSave.setIterations(
                    (attributes.iterations() !=
                     proposedAttributes.iterations()) ?
                    attributes.iterations() :
                    (null != savedAttributes.iterations() ?
                     savedAttributes.iterations() : null));
                attributesToSave.setTruncation(
                    (attributes.truncation() !=
                     proposedAttributes.truncation()) ?
                    attributes.truncation() :
                    (Attributes.NO_TRUNCATION != savedAttributes.truncation() ?
                     savedAttributes.truncation() : Attributes.NO_TRUNCATION));
            } else {
                attributesToSave.setDomain(
                    (attributes.domain() !=
                     proposedAttributes.domain()) ?
                    attributes.domain() : null);
                attributesToSave.setIterations(
                    (attributes.iterations() !=
                     proposedAttributes.iterations()) ?
                    attributes.iterations() : null);
                attributesToSave.setTruncation(
                    (attributes.truncation() !=
                     proposedAttributes.truncation()) ?
                    attributes.truncation() : Attributes.NO_TRUNCATION);
            }

            return attributesToSave;
        }

    }   // end class AttributesCodec

    // ====================================================================
    // NESTED CLASSES

    // --------------------------------------------------------------------
    // Hasher

    /**
     * @brief   The Workhorse class, which does all the Crypto magic.
     */
    private static class Hasher {

        public static byte[] generateSalt(String domain,
                                          String saltKey,
                                          Integer iterations) {
            byte[] salt = null;
            PKCS5S2ParametersGenerator generator =
                new PKCS5S2ParametersGenerator(new SHA256Digest());

            try {
                MessageDigest hash = MessageDigest.getInstance(SHA256);

                generator.init(hash.digest(domain.getBytes(UTF8)),
                               hash.digest(saltKey.getBytes()),
                               iterations);

                salt = ((KeyParameter)
                        generator.generateDerivedParameters(256)).getKey();
            } catch (NoSuchAlgorithmException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
            }

            return salt;
        }

        /**
         * @brief   
         * @return  
         */
        public static String generateHash(byte[] seedSHA,
                                          byte[] salt,
                                          Integer iterations) {
            byte[] hash = null;
            PKCS5S2ParametersGenerator generator =
                new PKCS5S2ParametersGenerator(new SHA256Digest());

            generator.init(seedSHA,
                           salt,
                           iterations);

            hash = ((KeyParameter)
                    generator.generateDerivedParameters(256)).getKey();

            String b64Hash = null;
            try {
                b64Hash = new String(Base64.encode(hash), UTF8);
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
            }

            return b64Hash;
        }

        /**
         * @brief   
         * @return  
         */
        public static String getPasswdStr(String b64Hash,
                                          Integer truncation) {
            return b64Hash.replace("=",
                                   "").replace("+",
                                               "-").replace("/",
                                                            "_");
        }

    }   // end class Hasher

    // --------------------------------------------------------------------
    // DATA MEMBERS

    String     m_saltKey;               /** @brief The salt key retrieved
                                          * from the default
                                          * shared preferences
                                          */
    Attributes m_savedAttributes;       /** @brief Attributes saved in the
                                          * preferences system.
                                          */
    Attributes m_proposedAttributes;    /** @brief Proposed attributes, after
                                          * reading from the saved preferences
                                          * and what would be used if further
                                          * unmodified by the user.
                                          */
    JSONObject m_siteAttributesList;    /** @brief The saved JSON of custom
                                          * website attributes.
                                          */
}
