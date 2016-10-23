/**
 * @file        CryptoTest.java
 * @brief       Source file for the CryptoTest class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 27, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.yggdrasil;

// Standard Java
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

// JUnit Classes
import org.junit.Test;
import org.junit.Assert;

// Spongycastle (Bouncycastle)
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.encoders.Base64;

/**
 * @brief   A test class for the methods in the Crypto class.
 */
public class CryptoTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ====================================================================
    // TESTS

    /**
     * @brief   Tests if the SHA256 Hash of known "seed" foo matches
     *          the expected hash.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testSeed_asserts() {
        Assert.assertEquals(
            getSeedSHAHex(),
            "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae");
    }

    /**
     * @brief   Tests if the salt from a known saltKey and domain matches
     *          the expected hash.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testSalt_asserts() {
        Assert.assertEquals(
            getSaltB64(),
            "zOVGQE+x32Xys9/l/JWETPuzZaOIr1e1iDyxXM+WRS4=");
    }

    /**
     * @brief   Tests if the base64-encoded hash from a known saltKey,
     *          domain and seed matches the expected value.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testB64Hash_asserts() {
        try {
            Assert.assertEquals(
                Crypto.generateHash(Crypto.getSeedSHA(SEED),
                                    Crypto.generateSalt(DOMAIN,
                                                        SALTKEY,
                                                        ITERATIONS),
                                    ITERATIONS,
                                    0),
                "PlntUbsKGDH2Lsp5JMvHljS074mkCFxUgJ3wxBoDg1I=");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail(EXCEPTION_NOALGO + "|" +
                        EXCEPTION_NOENCODE);
        }
    }

    /**
     * @brief   Tests if the Z85-encoded hash from a known saltKey,
     *          domain and seed matches the expected value.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testZ85Hash_asserts() {
        try {
            Assert.assertEquals(
                Crypto.generateHash(Crypto.getSeedSHA(SEED),
                                    Crypto.generateSalt(DOMAIN,
                                                        SALTKEY,
                                                        ITERATIONS),
                                    ITERATIONS,
                                    1),
                "k3vnIY9Yxf{aBHkb*jk]g{(.dQZgj8FsVhF8uUQ&");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail(EXCEPTION_NOALGO + "|" +
                        EXCEPTION_NOENCODE);
        }
    }

    /**
     * @brief   Tests if the base64-encoded proxy password from a known
     *          saltKey, domain and seed, and without truncation,
     *          matches the expected value.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testB64ProxyPasswordNoTruncation_asserts() {
        try {
            Assert.assertEquals(
                Crypto.getPasswdStr(
                    Crypto.generateHash(Crypto.getSeedSHA(SEED),
                                        Crypto.generateSalt(DOMAIN,
                                                            SALTKEY,
                                                            ITERATIONS),
                                        ITERATIONS,
                                        0),
                    -1,
                    0),
                "PlntUbsKGDH2Lsp5JMvHljS074mkCFxUgJ3wxBoDg1I");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail(EXCEPTION_NOALGO + "|" +
                        EXCEPTION_NOENCODE);
        }
    }

    /**
     * @brief   Tests if the Z85-encoded proxy password from a known
     *          saltKey, domain and seed, and without truncation,
     *          matches the expected value.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testZ85ProxyPasswordNoTruncation_asserts() {
        try {
            Assert.assertEquals(
                Crypto.getPasswdStr(
                    Crypto.generateHash(Crypto.getSeedSHA(SEED),
                                        Crypto.generateSalt(DOMAIN,
                                                            SALTKEY,
                                                            ITERATIONS),
                                        ITERATIONS,
                                        1),
                    -1,
                    1),
                "k3vnIY9Yxf{aBHkb*jk]g{(.dQZgj8FsVhF8uUQ&");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail(EXCEPTION_NOALGO + "|" +
                        EXCEPTION_NOENCODE);
        }
    }

    /**
     * @brief   Tests if the base64-encoded proxy password from a known
     *          saltKey, domain and seed, and with truncation,
     *          matches the expected value.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testB64ProxyPasswordWithTruncation_asserts() {
        try {
            Assert.assertEquals(
                Crypto.getPasswdStr(
                    Crypto.generateHash(Crypto.getSeedSHA(SEED),
                                        Crypto.generateSalt(DOMAIN,
                                                            SALTKEY,
                                                            ITERATIONS),
                                        ITERATIONS,
                                        0),
                    TRUNCATION,
                    0),
                "PlntUbsKGDH2Lsp5");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail(EXCEPTION_NOALGO + "|" +
                        EXCEPTION_NOENCODE);
        }
    }

    /**
     * @brief   Tests if the Z85-encoded proxy password from a known
     *          saltKey, domain and seed, and with truncation,
     *          matches the expected value.
     * @return  Does not even.
     */
    @Test
    public void cryptoTest_testZ85ProxyPasswordWithTruncation_asserts() {
        try {
            Assert.assertEquals(
                Crypto.getPasswdStr(
                    Crypto.generateHash(Crypto.getSeedSHA(SEED),
                                        Crypto.generateSalt(DOMAIN,
                                                            SALTKEY,
                                                            ITERATIONS),
                                        ITERATIONS,
                                        1),
                    TRUNCATION,
                    1),
                "k3vnIY9Yxf{aBHkb");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail(EXCEPTION_NOALGO + "|" +
                        EXCEPTION_NOENCODE);
        }
    }

    // ===================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String     DOMAIN                  = "google.com";
    private static final Integer    ITERATIONS              = 10000;
    private static final String     SEED                    = "foo";
    private static final String     UTF8                    = "UTF-8";
    private static final Integer    TRUNCATION              = 16;
    private static final String     SALTKEY                 =
        "np/hF+PCxK25Unqao/wq2+ybZcpxoeRubXcezOU6nhE0CejUYcCFzLBtR/PW8zZMvt6+IySIF7LTJfEoD91M6J+tPaqsb3flDUyolwLxMqT2fRmgPjZoLHLW3/zGy4xm01jqoxwUrQ5obBaLeVPofx6ev3ukFJpLiNScVS/ng+QaP/pEjXz0q8v0iPiskhee8lfjZK7mG+FxDHYDmtsGaLv0SKBH6joN1i7srXjyAzCFRrjCoP4q09IHwnbR/A56TC5vhKIYul6/L2gG+6JIjF7XrRWX+pMx8DjMV0lU6cPDMHtygQyEZJ92NnJ40rBvFKgkTJq8E8TjyFBYxlKuWDW/DdLy89LdzzDByMOyVamPBodN8gTrrMsWawTm0sBvwwcy5/hdo4cQE/XECZmryHUmvgQ+PEjBd+99hMezrA0wLX86UQ8kh8x2WhPz3w244kcfKqsiwPRniz4W6pw1084lM+hqM/oRZJSNfjGtlB2xfjVONRgjgLxMkTPnHdEWoleAi3zIHbVhn1ZgLgbvcjoSGSkIUHmC7+GupLTPSqZb+i53yJMGBPLfk5Uqk9/FfxjRvgcnlOmc3sRzMoLXnTzF13saEtiPbTW8MaY4KOSAbaC0If/3Ak7I2br+zaUQvD0E8W6uuxjRI3ZlN+GBZxmJLMNvzrhPNyR4F3cI9sk=";

    // Exception Messages
    private static final String     EXCEPTION_NOALGO        =
        "Exception.NoSuchAlgorithm.Thrown!";
    private static final String     EXCEPTION_NOENCODE      =
        "Exception.UnsupportedEncoing.Thrown!";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Method to obtain a Hex-encoded SHA of the "seed".
     * @return  {String} The Hex-encoded SHA of the seed;
     *          null if encoding fails.
     */
    private static String getSeedSHAHex() {
        String seedSHAHex = null;
        try {
            seedSHAHex = new String(Hex.encode(Crypto.getSeedSHA(SEED)),
                                    UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return seedSHAHex;
    }

    /**
     * @brief   Method to obtain a Base64-encoded salt from a domain
     *          and a salt key.
     * @return  {String} The Base64-encoded salt, null if encoding fails.
     */
    private static String getSaltB64() {
        String saltB64 = null;
        try {
            saltB64 =
                new String(Base64.encode(Crypto.generateSalt(DOMAIN,
                                                             SALTKEY,
                                                             ITERATIONS)),
                                 UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return saltB64;
    }

}
