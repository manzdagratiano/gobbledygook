/**
 * @file        Crypto.java
 * @summary     Source file for the Crypto class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

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

// ZeroMQ
import org.zeromq.codec.Z85;

/**
 * @summary The crypto workhorse, which does all the crypto magic
 */
public class Crypto {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ===================================================================
    // PUBLIC METHODS

    /**
     * @summary A routine to calculate the SHA256 hash of the user's
     *          one true password.
     *          This is the only routine that sees the user's password;
     *          for security, it is not even passed around.
     * @return  The SHA256 hash of the user's password
     */
    public static byte[] getSeedSHA(final String seed)
        throws NoSuchAlgorithmException {
        byte[] seedSHA = null;
        MessageDigest hash = null;
        try {
            hash = MessageDigest.getInstance(SHA256);
            seedSHA = hash.digest(seed.getBytes());
        } catch (NoSuchAlgorithmException e) {
            // Throw it to the caller
            throw e;
        }

        return seedSHA;
    }

    /**
     * @summary Method to generate a new salt key
     * @return  {String} The newly generated salt key
     */
    public static String generateSaltKey()
        throws UnsupportedEncodingException {

        SecureRandom csprng = new SecureRandom();
        byte[] saltKeyBytes = new byte[SALT_KEY_LENGTH];
        csprng.nextBytes(saltKeyBytes);
        String saltKey = null;
        try {
            saltKey = new String(Base64.encode(saltKeyBytes), UTF8);
        } catch (UnsupportedEncodingException e) {
            // Throw it to the caller.
            throw e;
        }
        return saltKey;
    }

    /**
     * @summary Method to generate a salt using a domain name
     *          and the salt key.
     * @return  {byte[]} The byte sequence for the generated salt
     */
    public static byte[] generateSalt(String domain,
                                      String saltKey,
                                      Integer iterations)
        throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
            throw e;
        } catch (UnsupportedEncodingException e) {
            throw e;
        }

        return salt;
    }

    /**
     * @summary Method to generate a key-stretched password from
     *          a seed password (hashed) and a salt.
     * @return  {String} The encoded generated key-stretched password.
     */
    public static String generateHash(final byte[] seedSHA,
                                      final byte[] salt,
                                      final Integer iterations,
                                      final Integer specialCharsFlag)
        throws UnsupportedEncodingException {
        byte[] hash = null;
        PKCS5S2ParametersGenerator generator =
            new PKCS5S2ParametersGenerator(new SHA256Digest());

        generator.init(seedSHA,
                       salt,
                       iterations);

        hash = ((KeyParameter)
                generator.generateDerivedParameters(256)).getKey();

        String encodedHash = null;
        if (specialCharsFlag.equals(1)) {
            encodedHash = Z85.Z85Encoder(hash);
        } else {
            try {
                encodedHash = new String(Base64.encode(hash), UTF8);
            } catch (UnsupportedEncodingException e) {
                // Throw it to the caller
                throw e;
            }
        }

        return encodedHash;
    }

    /**
     * @summary Method to generate the final password string from
     *          an encoded key-stretched hash.
     * @return  {String} The final password string to use.
     */
    public static String getPasswdStr(final String encodedHash,
                                      final Integer truncation,
                                      final Integer specialCharsFlag) {
        String password = null;
        if (1 != specialCharsFlag) {
            // For the case of base64, which is being used in
            // the "no special characters" mode, remove the special
            // characters (+,/,=) to limit the result set to
            // [a-z][A-Z][0-9].
            password = encodedHash.replace("=",
                                           "").replace("+",
                                                       "-").replace("/",
                                                                    "_");
        } else {
            // For the case of Z85, replace "/" with "_", which is
            // NOT part of the Z85 alphabet, to make the hash
            // filename safe.
            password = encodedHash.replace("/","_");
        }

        if (truncation > 0) {
            password = password.substring(0,
                                          Math.min(password.length(),
                                                   truncation));
        }

        return password;
    }

    // ===================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final int    SALT_KEY_LENGTH     = 512;
    private static final String SHA256              = "SHA-256";
    private static final String UTF8                = "UTF-8";

}
