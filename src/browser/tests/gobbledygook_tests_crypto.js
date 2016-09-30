/**
 * @module      gobbledygook_tests_crypto.js
 * @overview    Unit tests for cryptographic calls in Gobbledygook.
 *              Requires the QUnit framework.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jul 01, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

"use strict";

// ========================================================================
// GLOBAL CONSTANTS

/**
 * @brief   A namespace for the parameters injected into the unit tests.
 *          The unit test assertion statements depend on the values of these
 *          parameters, i.e., changing them here will cause tests to fail.
 */
var PARAMS = {
    domain      : "google.com",
    iterations  : 10000,
    seed        : "foo",
    truncation  : 16,
    saltKey     : "np/hF+PCxK25Unqao/wq2+ybZcpxoeRubXcezOU6nhE0CejUYcCFzLBtR/PW8zZMvt6+IySIF7LTJfEoD91M6J+tPaqsb3flDUyolwLxMqT2fRmgPjZoLHLW3/zGy4xm01jqoxwUrQ5obBaLeVPofx6ev3ukFJpLiNScVS/ng+QaP/pEjXz0q8v0iPiskhee8lfjZK7mG+FxDHYDmtsGaLv0SKBH6joN1i7srXjyAzCFRrjCoP4q09IHwnbR/A56TC5vhKIYul6/L2gG+6JIjF7XrRWX+pMx8DjMV0lU6cPDMHtygQyEZJ92NnJ40rBvFKgkTJq8E8TjyFBYxlKuWDW/DdLy89LdzzDByMOyVamPBodN8gTrrMsWawTm0sBvwwcy5/hdo4cQE/XECZmryHUmvgQ+PEjBd+99hMezrA0wLX86UQ8kh8x2WhPz3w244kcfKqsiwPRniz4W6pw1084lM+hqM/oRZJSNfjGtlB2xfjVONRgjgLxMkTPnHdEWoleAi3zIHbVhn1ZgLgbvcjoSGSkIUHmC7+GupLTPSqZb+i53yJMGBPLfk5Uqk9/FfxjRvgcnlOmc3sRzMoLXnTzF13saEtiPbTW8MaY4KOSAbaC0If/3Ak7I2br+zaUQvD0E8W6uuxjRI3ZlN+GBZxmJLMNvzrhPNyR4F3cI9sk="
};

function getSeedSHA(seed) {
    return sjcl.codec.hex.fromBits(sjcl.hash.sha256.hash(seed));
}

QUnit.module("Crypto");

QUnit.test("TestSeed", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);
    assert
        .ok(("2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"
             === seedSHA),
            "Seed.QED!");
});

QUnit.test("TestSalt", function(assert) {
    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    assert.ok("zOVGQE+x32Xys9/l/JWETPuzZaOIr1e1iDyxXM+WRS4=" === salt,
              "Salt.QED!");
});

QUnit.test("TestB64Hash", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);

    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    var b64Hash = Hasher.generateHash(seedSHA,
                                      salt,
                                      PARAMS.iterations,
                                      0);
    assert.ok("PlntUbsKGDH2Lsp5JMvHljS074mkCFxUgJ3wxBoDg1I=" === b64Hash,
              "B64Hash.QED!");
});

QUnit.test("TestZ85Hash", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);

    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    var z85Hash = Hasher.generateHash(seedSHA,
                                      salt,
                                      PARAMS.iterations,
                                      1);
    assert.ok("k3vnIY9Yxf{aBHkb*jk]g{(.dQZgj8FsVhF8uUQ&" === z85Hash,
              "Z85Hash.QED!");
});

QUnit.test("TestB64ProxyPasswordNoTruncation", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);

    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    var b64Hash = Hasher.generateHash(seedSHA,
                                      salt,
                                      PARAMS.iterations,
                                      0);
    var proxy = Hasher.getPasswdStr(b64Hash, -1, 0);
    assert.ok("PlntUbsKGDH2Lsp5JMvHljS074mkCFxUgJ3wxBoDg1I" === proxy,
              "B64ProxyPasswordNoTruncation.QED!");
});

QUnit.test("TestZ85ProxyPasswordNoTruncation", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);

    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    var z85Hash = Hasher.generateHash(seedSHA,
                                      salt,
                                      PARAMS.iterations,
                                      1);
    var proxy = Hasher.getPasswdStr(z85Hash, -1, 1);
    assert.ok("k3vnIY9Yxf{aBHkb*jk]g{(.dQZgj8FsVhF8uUQ&" === proxy,
              "Z85ProxyPasswordNoTruncation.QED!");
});

QUnit.test("TestB64ProxyPasswordWithTruncation", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);

    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    var b64Hash = Hasher.generateHash(seedSHA,
                                      salt,
                                      PARAMS.iterations,
                                      0);
    var proxy = Hasher.getPasswdStr(b64Hash,
                                    PARAMS.truncation,
                                    0);
    console.log(proxy);
    assert.ok("PlntUbsKGDH2Lsp5" === proxy,
              "B64ProxyPasswordWithTruncation.QED!");
});

QUnit.test("TestZ85ProxyPasswordWithTruncation", function(assert) {
    var seedSHA = getSeedSHA(PARAMS.seed);

    // Generate the salt
    var salt = Hasher.generateSalt(PARAMS.domain,
                                   PARAMS.saltKey,
                                   PARAMS.iterations);
    var z85Hash = Hasher.generateHash(seedSHA,
                                      salt,
                                      PARAMS.iterations,
                                      1);
    var proxy = Hasher.getPasswdStr(z85Hash,
                                    PARAMS.truncation,
                                    1);
    assert.ok("k3vnIY9Yxf{aBHkb" === proxy,
              "Z85ProxyPasswordWithTruncation.QED!");
});
