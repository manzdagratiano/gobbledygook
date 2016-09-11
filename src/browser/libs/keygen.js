/**
 * @module      keygen
 * @overview    A set of functions to generate the salt "key".
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Dec 26, 2014
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2014
 */

"use strict";

// ========================================================================
// GLOBAL CONSTANTS

/**
 * @namespace
 * @summary A global namespace for miscellaneous "environment" variables.
 */
var KEYGEN                  = {

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "KEYGEN: "

};


/**
 * @namespace AttributesCodec
 * @summary A class to generate a "key" to create salts from domain names.
 */
var Keygen                  = {

    /**
     * @summary A function to generate the key used for creating salts
     *          from domain names. It uses the default singleton
     *          "sjcl.random" defined in the SJCL library for the prng,
     *          but overrides the default paranoia with 512 (level 8)
     *          instead of 256 (level 6).
     * @return  {string} The base64 encoded key for creating salts
     *          from domain names.
     */
    generate : function() {

        // Each 'word' is 4 bytes, so 512 bytes of random data
        // requires 128 words.
        var NUM_WORDS = 128;
        // Set the "paranoia" level to 512
        var PARANOIA = 8;
        var keyBits = "";
        try {
            // Generate the random data using "randomWords" from
            // the default singleton sjcl.random.
            // On modern browsers, this method just uses the
            // window.crypto API, which is a CSPRNG.
            keyBits = sjcl.random.randomWords(NUM_WORDS, PARANOIA);
        } catch(e) {
            // TODO: Use other "collectors" in SJCL to generate data
            // in the event the browser crypto API is not available.
            console.error(KEYGEN.logCategory + e);
            return;
        }

        return sjcl.codec.base64.fromBits(keyBits);
    }

};
