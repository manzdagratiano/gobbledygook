/**
 * @file        AttributesCodec.java
 * @brief       Source file for the AttributesCodec class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 27, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.yggdrasil;

// Android
import android.util.Log;

// JSON
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @brief   The AttributesCodec class.
 *          This class encapsulates methods for encoding/decoding
 *          objects of the Attributes class.
 */
public class AttributesCodec {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   
     * @return  
     */
    public static String encode(Attributes attributes) {
        if (!attributes.attributesExist()) {
            return "";
        }

        return
            (((null != attributes.domain()) ?
              attributes.domain() : "") +
             AttributesCodec.DELIMITER +
             ((null != attributes.iterations()) ?
              attributes.iterations().toString() : "") +
             AttributesCodec.DELIMITER +
             ((Attributes.NO_TRUNCATION != attributes.truncation()) ?
              attributes.truncation().toString() : "") +
             AttributesCodec.DELIMITER +
             ((1 != attributes.specialCharsFlag()) ?
              "0" : ""));
    }

    /**
     * @brief   Method to decoded an encoded Attributes string.
     * @return  {Attributes} The decoded object; default constructed
     *          on an error condition.
     */
    public static Attributes decode(String encodedAttributes) {
        // Create a default-initialized Attributes object
        Attributes attributes = new Attributes();
        Log.d(LOG_CATEGORY, "AttributesCodec.decode(): Decoding " +
              ((null == encodedAttributes) ?
               "null" : "'" + encodedAttributes + "'") + " ...");

        // Sanity checks
        // ("short-circuit")
        if (null == encodedAttributes || encodedAttributes.isEmpty()) {
            return attributes;
        }

        String[] attributesArray =
            encodedAttributes.split(AttributesCodec.DELIMITER);
        // Sanity check for length of the split array
        if (4 != attributesArray.length) {
            Log.e(LOG_CATEGORY, "ERROR: Malformed Attributes! Expected " +
                  "<domain|iterations|truncation|noSpecialChars>");
            return attributes;
        }

        if (attributesArray[0] != "") {
            attributes.setDomain(attributesArray[0]);
        }
        if (attributesArray[1] != "") {
            try {
                attributes.setIterations(
                        Integer.parseInt(attributesArray[1]));
            } catch (ClassCastException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
                attributes.setIterations(Attributes.DEFAULT_ITERATIONS);
            }
        }
        if (attributesArray[2] != "") {
            try {
                attributes.setTruncation(
                        Integer.parseInt(attributesArray[2]));
            } catch (ClassCastException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
                attributes.setTruncation(Attributes.NO_TRUNCATION);
            }
        }
        if (attributesArray[3] != "") {
            attributes.setSpecialCharsFlag(0);
        }

        return attributes;
    }

    /**
     * @brief   Function to read the saved JSON string of custom
     *          website attributes into a JSON object.
     * @return  The decoded JSONObject if the encodedOverrides
     *          was a valid JSON string, else null
     */
    public static JSONObject
    getEncodedOverridesMap(final String encodedOverrides) {
        Log.i(LOG_CATEGORY, "Decoding saved attributes list...");

        JSONObject encodedOverridesMap = null;

        // Sanity check - short-circuit evaluation
        if (null != encodedOverrides &&
            !encodedOverrides.isEmpty()) {
            try {
                encodedOverridesMap = new JSONObject(encodedOverrides);
                Log.d(LOG_CATEGORY, "encodedOverridesMap=" +
                      encodedOverridesMap.toString());
            } catch (JSONException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
            }
        }

        return encodedOverridesMap;
    }

    /**
     * @brief   Method to retrieve the saved overrides for a domain
     * @return  {Attributes} The retrieved object, (default constructed
     *          if no saved attributes exist)
     */
    public static Attributes
    getDomainOverrides(String domain,
                       JSONObject customOverrides) {
        Log.i(LOG_CATEGORY, "Fetching saved attributes...");

        String encodedOverrides = null;

        if (null != customOverrides) {
            try {
                if (customOverrides.has(domain)) {
                    encodedOverrides = customOverrides.getString(domain);
                }
            } catch (JSONException e) {
                Log.e(LOG_CATEGORY, "ERROR: Caught " + e);
                e.printStackTrace();
            }
        }

        Attributes decodedOverrides = decode(encodedOverrides);
        return decodedOverrides;
    }

    /**
     * @brief   
     * @return  A valid Attributes object,
     *          which can be default initialized
     */
    public static Attributes
    getOverridesToSave(final Attributes attributes,
                       final Attributes savedOverrides,
                       final Attributes proposedAttributes) {
        Attributes overrides = new Attributes();

        // If proposedAttributes is the same as attributes,
        // there's nothing to save.
        if (proposedAttributes.equals(attributes)) {
            return overrides;
        }

        if (savedOverrides.attributesExist()) {
            overrides.setDomain(
                !attributes.domain().equals(
                        proposedAttributes.domain()) ?
                attributes.domain() :
                savedOverrides.domain());
            overrides.setIterations(
                !attributes.iterations().equals(
                        proposedAttributes.iterations()) ?
                attributes.iterations() :
                savedOverrides.iterations());
            overrides.setTruncation(
                !attributes.truncation().equals(
                        proposedAttributes.truncation()) ?
                attributes.truncation() :
                savedOverrides.truncation());
            overrides.setSpecialCharsFlag(
                !attributes.specialCharsFlag().equals(
                        proposedAttributes.specialCharsFlag()) ?
                attributes.specialCharsFlag() :
                savedOverrides.specialCharsFlag());
        } else {
            overrides.setDomain(
                !attributes.domain().equals(
                        proposedAttributes.domain()) ?
                attributes.domain() : null);
            overrides.setIterations(
                !attributes.iterations().equals(
                        proposedAttributes.iterations()) ?
                attributes.iterations() : null);
            overrides.setTruncation(
                !attributes.truncation().equals(
                        proposedAttributes.truncation()) ?
                attributes.truncation() : Attributes.NO_TRUNCATION);
            overrides.setSpecialCharsFlag(
                !attributes.specialCharsFlag().equals(
                        proposedAttributes.specialCharsFlag()) ?
                attributes.specialCharsFlag() : 1);
        }

        return overrides;
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY    = "YGGDRASIL.CODEC";
    private static final String DELIMITER       = "|";

}   // end class AttributesCodec
