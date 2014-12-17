/**
 * @module      options
 * @overview    A set of functions to save/restore user default options
 *              to/from the browser's preference system.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Dec 08, 2014
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2014, 2015
 */

"use strict";

// ========================================================================
// GLOBAL CONSTANTS

/**
 * @namespace
 * @summary A global namespace for miscellaneous "environment" variables.
 */
var OPTIONS_ENV             = {

    /**
     * @summary The default number of iterations of PBKDF2 (when unset).
     */
    defaultIterations       : 10000,

    /**
     * @summary The actionable events that could be triggered when
     *          interacting with the UI.
     * @enum    {string}
     */
    events                  : {
        DOM_CONTENT_LOADED  : "DOMContentLoaded",
        CLICK               : "click"
    },

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "OPTIONS: ",

    /**
     * @summary The UTF-8 code used to identify "success" or "failure"
     *          when communicating back to the user (in this case when
     *          saving site attributes).
     *          SUCCESS would show up as a "check mark", and
     *          FAILURE would show up as a "cross".
     * @enum    {string}
     */
    successString           : {
        SUCCESS             : "\u2713",
        FAILURE             : "\u2717"
    },

    /**
     * @summary Types referenced for the "typeof" command.
     * @enum    {string}
     */
    types                   : {
        OBJECT              : "object",
        UNDEFINED           : "undefined"
    }

};  // end namepace OPTIONS_ENV

/**
 * @namespace
 * @summary A namespace for elements of the DOM of the Options page.
 */
var OPTIONS_DOM             = {
    defaultIterations       : "defaultIterations",
    saltKey                 : "saltKey",
    generateKeyButton       : "generateKeyButton",
    optionsSaveSuccessLabel : "optionsSaveSuccessLabel",
    siteAttributesList      : "siteAttributesList",
    saveButton              : "saveButton"
};

// ========================================================================
// FUNCTIONS

/**
 * @summary Function to generate the 'key' for creating salts from domains.
 * @return  {undefined}
 */
function generateKey() {
    console.info(OPTIONS_ENV.logCategory +
                 "Generating salt 'key'...");
    var saltBox = document.getElementById(OPTIONS_DOM.saltKey);
    saltBox.value = "<Generating...>";

    var keyGenerated = false;
    if (OPTIONS_ENV.types.OBJECT === typeof(Keygen)) {
        var key = Keygen.generate();
        if (OPTIONS_ENV.types.UNDEFINED !== typeof(key)) {
            keyGenerated = true;
        }
    } else {
        console.error(OPTIONS_ENV.logCategory +
                      "ERROR: 'Keygen' undefined");
    }

    saltBox.value = keyGenerated ? key : "ERROR generating key!";
}

/**
 * @summary Function to save user default attributes to
 *          the browser preference system.
 * @return  {undefined}
 */
function saveOptions() {
    console.info(OPTIONS_ENV.logCategory +
                 "Saving default options...");
    chrome.storage.sync.set({
        saltKey             : document.getElementById(
                                OPTIONS_DOM.saltKey).value,
        defaultIterations   : parseInt(document.getElementById(
                                OPTIONS_DOM.defaultIterations).value),
        siteAttributesList  : document.getElementById(
                                OPTIONS_DOM.siteAttributesList).value,
    }, function() {
        // Check if the options were successfully saved
        if (chrome.runtime.lastError) {
            console.error(OPTIONS_ENV.logCategory +
                          "ERROR saving options" +
                          ", errorMsg=" + chrome.runtime.lastError);
            document.getElementById(
                        OPTIONS_DOM.optionsSaveSuccessLabel).innerHTML =
                OPTIONS_ENV.successString.FAILURE;
            return;
        }

        console.info(OPTIONS_ENV.logCategory + "Options saved");
        document.getElementById(
                    OPTIONS_DOM.optionsSaveSuccessLabel).innerHTML =
            OPTIONS_ENV.successString.SUCCESS;
    });
}

/**
 * @summary Function to restore user default attributes to the options
 *          page from th browser's preference system.
 * @return  {undefined}
 */
function restoreOptions() {
    console.info(OPTIONS_ENV.logCategory +
                 "Loading default options...");
    chrome.storage.sync.get({
        saltKey             : "",
        defaultIterations   : OPTIONS_ENV.defaultIterations,
        siteAttributesList  : ""
    }, function(items) {
        if (chrome.runtime.lastError) {
            console.info(OPTIONS_ENV.logCategory +
                         "ERROR loading options" +
                         ", errorMsg=" + chrome.runtime.lastError);
            // No need to return; defaults will be set.
            // The user can choose not to proceed susbequently.
        }

        console.info(OPTIONS_ENV.logCategory + "Default options loaded");
        document.getElementById(OPTIONS_DOM.saltKey).value =
            items.saltKey;
        document.getElementById(OPTIONS_DOM.defaultIterations).value =
            items.defaultIterations;
        document.getElementById(OPTIONS_DOM.siteAttributesList).value =
            items.siteAttributesList;
    });
}

// ========================================================================
// EVENT HANDLERS

// Restore options on loading the "Options" page
document.addEventListener(OPTIONS_ENV.events.DOM_CONTENT_LOADED,
                          restoreOptions);

// Generate a key when the "Generate Key" button is clicked
document.getElementById(
            OPTIONS_DOM.generateKeyButton).addEventListener(
                                                OPTIONS_ENV.events.CLICK,
                                                generateKey);

// Save options when the "Save" button is clicked
document.getElementById(
            OPTIONS_DOM.saveButton).addEventListener(
                                        OPTIONS_ENV.events.CLICK,
                                        saveOptions);

// Configure "optionsSaveSuccessLabel"
document.getElementById(
            OPTIONS_DOM.optionsSaveSuccessLabel).innerHTML = "";
