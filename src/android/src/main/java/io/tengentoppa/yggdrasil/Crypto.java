/**
 * @file        Crypto.java
 * @brief       Source file for the Crypto class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.util.Log;

// Standard Java
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

// SpongyCastle
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Base64;

/**
 * @brief   The crypto workhorse, which does all the crypto magic
 */
public class Crypto {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ===================================================================
    // PUBLIC METHODS

    /**
     * @brief   Method to generate a new salt key
     * @return  {String} The newly generated salt key
     */
    public static String generateSaltKey() {
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
        return saltKey;
    }

    /**
     * @brief   Method to generate a salt using a domain name
     *          and the salt key.
     * @return  {byte[]} The byte sequence for the generated salt
     */
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
     * @brief   Method to generate a key-stretched password from
     *          a seed password (hashed) and a salt.
     * @return  {String} The base64-encoded generated key-stretched password
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
     * @brief   Method to generate the final password string from
     *          a base64-encoded key-stretched hash.
     * @return  {String} The kosher key-stretched password string to use
     */
    public static String getPasswdStr(String b64Hash,
                                      Integer truncation) {
        return b64Hash.replace("=",
                               "").replace("+",
                                           "-").replace("/",
                                                        "_");
    }

    // ===================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY        = "YGGDRASIL.CRYPTO";
    private static final int    SALT_KEY_LENGTH     = 512;
    private static final String SHA256              = "SHA-256";
    private static final String UTF8                = "UTF-8";

}
