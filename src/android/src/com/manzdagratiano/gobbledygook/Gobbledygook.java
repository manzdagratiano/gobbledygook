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
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

public class Gobbledygook extends Activity
{

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ====================================================================
    // PUBLIC DATA

    public class Env
    {

        // ----------------------------------------------------------------
        // CONSTANTS

        public static final String LOG_CATEGORY = "GOBBLEDYGOOK";
        public static final String SHA256       = "SHA-256";
        public static final String UTF8         = "UTF-8";

    }

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   Called when the activity is created.
     * *** NOTE *** A screen rotation is also an activity re-creation.
     * The "state" is saved, and then reloaded on screen rotation.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(Env.LOG_CATEGORY, "onCreate(): Creating activity...");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
    }

    /**
     * @brief   Called after onCreate() (and onStart(),
     * when the activity begins interacting with the user)
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        Log.i(Env.LOG_CATEGORY, "onResume(): Configuring...");
        super.onResume();

        // ----------------------------------------------------------------
        // Retrieve saved preferences
        // The SharedPreferences handle
        Log.i(Env.LOG_CATEGORY, "Reading saved preferences...");
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(this);
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
            // and is entered as an integer,
            // it is saved as a string.
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
            Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        Log.d(Env.LOG_CATEGORY,
              "savedPreferences=[ saltKey='" + saltKey + "', " +
              "defaultIterations=" + defaultIterations.toString() + ", " +
              "siteAttributesList='" + encodedAttributesList + "' ]");

        // ----------------------------------------------------------------
        // Create the "actors"

        AttributesCodec codec = new AttributesCodec();
        Configurator config = new Configurator();

        // ----------------------------------------------------------------
        // Read the url from the clipboard
        Log.i(Env.LOG_CATEGORY, "Reading url from clipboard...");
        String url = "";
        ClipboardManager clipboard =
            (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData.Item clipItem = clipboard.getPrimaryClip().getItemAt(0);
            CharSequence clipText = clipItem.getText();
            if (null != clipText) {
                url = clipText.toString();
            }
        }
        Log.i(Env.LOG_CATEGORY, "url='" + url + "'");

        // ----------------------------------------------------------------
        // Extract the domain from the url

        String domain = config.extractDomain(url);
        Log.i(Env.LOG_CATEGORY, "domain='" + domain + "'");

        // ----------------------------------------------------------------
        // Saved and Proposed Attributes

        // Obtain the saved attributes for this domain, if any
        Attributes savedAttributes =
            codec.getSavedAttributes(domain,
                                     encodedAttributesList);
        Log.i(Env.LOG_CATEGORY,
              "savedAttributes='" + codec.encode(savedAttributes) + "'");

        // The "proposed" attributes,
        // which would be used to generate the proxy password,
        // unless overridden
        Attributes proposedAttributes =
            new Attributes(
                config.configureDomain(domain,
                                       savedAttributes.domain()),
                config.configureIterations(defaultIterations,
                                           savedAttributes.iterations()),
                config.configureTruncation(savedAttributes.truncation()));

        config.configureHash();
    }

    /**
     * @brief   Called when the menu is created.
     * @return  
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.gobbledygook_actions,
                                  menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @brief   Called when a menu item is selected.
     * @return  
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, GobbledygookPrefs.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @brief   
     * @return  
     */
    public void generate(final View view) {
        Log.i(Env.LOG_CATEGORY, "Generating proxy password...");
        AttributesCodec codec = new AttributesCodec();
        final Attributes attributes = getAttributes(view);
        Log.i(Env.LOG_CATEGORY,
              "attributes='" + codec.encode(attributes) + "'");

        final byte[] seedSHA = getSeedSHA(view);
        if (null == seedSHA) {
            Log.e(Env.LOG_CATEGORY, "ERROR: seedSHA.generation.failure");
            return;
        }
        try {
            Log.d(Env.LOG_CATEGORY,
                  "seedSHA=" + (new String(Hex.encode(seedSHA),
                                           Env.UTF8)));
        } catch (UnsupportedEncodingException e) {
            Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        // Retrieve the salt key
        // The SharedPreferences handle
        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(this);
        // The salt key 
        final String saltKey =
            sharedPrefs.getString(getString(R.string.pref_saltKey_key),
                                  "");

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
                    Log.e(Env.LOG_CATEGORY, errMsg);
                    setPassword(view, errMsg);
                    return;
                }
                try {
                    Log.i(Env.LOG_CATEGORY,
                          "salt=" + (new String(Base64.encode(salt),
                                                Env.UTF8)));
                } catch (UnsupportedEncodingException e) {
                    Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                }

                // Generate the hash (the "proxy password")
                String b64Hash = Hasher.generateHash(seedSHA,
                                                     salt,
                                                     attributes.iterations());
                if (null == b64Hash) {
                    String errMsg = "Hash.Generation.Failure";
                    Log.e(Env.LOG_CATEGORY, errMsg);
                    setPassword(view, errMsg);
                    return;
                }
                Log.i(Env.LOG_CATEGORY, "hash=" + b64Hash);
                final String password =
                    Hasher.getPasswdStr(b64Hash,
                                        attributes.truncation());
                Log.i(Env.LOG_CATEGORY, "password=" + password);

                // Post the results to the UI thread for manipulation
                // (using "runOnUiThread" from the "Activity" class)
                runOnUiThread(new Runnable() {
                    public void run() {
                        setPassword(view, password);
                    }
                });
            }
        }).start();
    }

    // ====================================================================
    // PRIVATE METHODS

    /**
     * @brief   
     * @return  
     */
    private Attributes getAttributes(View view)
    {
        EditText domainField     = (EditText)findViewById(R.id.domain);
        EditText iterationsField = (EditText)findViewById(R.id.iterations);
        EditText truncationField = (EditText)findViewById(R.id.truncation);

        String domain = domainField.getText().toString();
        Integer iterations = null;
        try {
            iterations =
                ((0 == iterationsField.getText().toString().trim().length()) ?
                 Attributes.DEFAULT_ITERATIONS :
                 Integer.parseInt(iterationsField.getText().toString()));
        } catch (ClassCastException e) {
            Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
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
            Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
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
    private byte[] getSeedSHA(View view)
    {
        EditText password = (EditText) findViewById(R.id.password);
        byte[] seedSHA = null;
        MessageDigest hash = null;
        try {
            hash = MessageDigest.getInstance(Env.SHA256);
            seedSHA =
                hash.digest(password.getText().toString().getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
            e.printStackTrace();
        }

        return seedSHA;
    }

    private void setPassword(View view,
                             String password)
    {
        EditText hash = (EditText) findViewById(R.id.hash);
        hash.setText(password, TextView.BufferType.EDITABLE);
    }

    // ====================================================================
    // PRIVATE DATA

    // --------------------------------------------------------------------
    // INNER CLASSES

    /**
     * @brief   
     * @return  
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
                Log.e(Env.LOG_CATEGORY, "ERROR: " +
                      "Thwarted attempt to set truncation to null!");
                return;
            }
            this.m_truncation = truncation;
        }

        // ----------------------------------------------------------------
        // Data Members

        private String  m_domain;
        private Integer m_iterations;
        private Integer m_truncation;

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
            String encodedAttributes = null;
            return
                ((null == attributes.domain()) ?
                 "" : attributes.domain()) +
                 AttributesCodec.DELIMITER +
                ((null == attributes.iterations()) ?
                 "" : attributes.iterations().toString()) +
                AttributesCodec.DELIMITER +
                ((null == attributes.truncation()) ?
                 "" : attributes.truncation().toString());
        }

        /**
         * @brief   
         * @return  
         */
        public Attributes decode(String encodedAttributes) {
            // Create a default-initialized Attributes object
            Attributes attributes = new Attributes();
            Log.d(Env.LOG_CATEGORY, "AttributesCodec.decode(): Decoding " +
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
                Log.e(Env.LOG_CATEGORY, "ERROR: Malformed Attributes! " +
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
                    Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                    attributes.setIterations(Attributes.DEFAULT_ITERATIONS);
                }
            }
            if (siteAttributesArray[2] != "") {
                try {
                    attributes.setTruncation(
                            Integer.parseInt(siteAttributesArray[2]));
                } catch (ClassCastException e) {
                    Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                    attributes.setTruncation(Attributes.NO_TRUNCATION);
                }
            }

            return attributes;
        }

        /**
         * @brief   
         * @return  A valid Attributes object
         * (default initialized if no saved attributes exist)
         */
        public Attributes
        getSavedAttributes(String domain,
                           String encodedAttributesList) {
            Log.i(Env.LOG_CATEGORY, "Fetching saved attributes...");

            String encodedAttributes = null;
            JSONObject encodedAttributesJson = null;

            // Sanity check - short-circuit evaluation
            if (null != encodedAttributesList &&
                !encodedAttributesList.isEmpty()) {
                try {
                    encodedAttributesJson =
                        new JSONObject(encodedAttributesList);
                    Log.d(Env.LOG_CATEGORY, "encodedAttributesList=" +
                          encodedAttributesJson.toString());
                } catch (JSONException e) {
                    Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                }
            }

            if (null != encodedAttributesJson) {
                try {
                    if (encodedAttributesJson.has(domain)) {
                        encodedAttributes = encodedAttributesJson.getString(domain);
                    }
                } catch (JSONException e) {
                    Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
                    e.printStackTrace();
                }
            }

            Attributes decodedAttributes = this.decode(encodedAttributes);
            return decodedAttributes;
        }

    }   // end class AttributesCodec

    // --------------------------------------------------------------------
    // Configuration

    /**
     * @brief   The Configurator class, which reads the saved preferences
     * and configures the UI elements upon load.
     */
    private class Configurator {

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
         * @return  
         */
        public String configureDomain(String domain,
                                      String savedDomain) {
            EditText domainField = (EditText)findViewById(R.id.domain);
            String theDomain = (null == savedDomain) ? domain : savedDomain;
            domainField.setText(theDomain,
                                TextView.BufferType.EDITABLE);
            return theDomain;
        }

        /**
         * @brief   
         * @return  
         */
        public Integer configureIterations(Integer defaultIterations,
                                           Integer savedIterations) {
            EditText iterationsField = (EditText)findViewById(R.id.iterations);
            Integer theIterations =
                ((null == savedIterations) ? defaultIterations : savedIterations);
            iterationsField.setText(theIterations.toString(),
                                    TextView.BufferType.EDITABLE);
            return theIterations;
        }

        /**
         * @brief   
         * @return  
         */
        public Integer configureTruncation(Integer truncation) {
            EditText truncationField = (EditText)findViewById(R.id.truncation);
            // The truncation is never null
            truncationField.setText(truncation.toString(),
                                    TextView.BufferType.EDITABLE);
            return truncation;
        }

        /**
         * @brief   
         * @return  
         */
        public void configureHash() {
            CheckBox showHashBox = (CheckBox)findViewById(R.id.showHash);
            showHashBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText hash = (EditText) findViewById(R.id.hash);
                    CheckBox showHashBox = (CheckBox)view; 
                    if (showHashBox.isChecked()) {
                        hash.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        hash.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });
        }

    }   // end class Configurator

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
                MessageDigest hash = MessageDigest.getInstance(Env.SHA256);

                generator.init(hash.digest(domain.getBytes(Env.UTF8)),
                               hash.digest(saltKey.getBytes()),
                               iterations);

                salt = ((KeyParameter)
                        generator.generateDerivedParameters(256)).getKey();
            } catch (NoSuchAlgorithmException e) {
                Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
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
                b64Hash = new String(Base64.encode(hash), Env.UTF8);
            } catch (UnsupportedEncodingException e) {
                Log.e(Env.LOG_CATEGORY, "ERROR: Caught " + e);
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

    }
}
