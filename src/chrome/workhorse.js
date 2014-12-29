/**
 * @module      workhorse
 * @overview    The main "workhorse" responsible for the generation of
 *              the proxy password.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Nov 15, 2014
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
var ENV                     = {

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
        DOM_CONTENT_LOADED  : "DOMContentLoaded"
    },

    /**
     * @summary The indentation for "pretty-printing" JSON objects.
     */
    indentation             : 4,

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "MAIN: ",

    /**
     * @summary Types referenced for the "typeof" command.
     * @enum    {string}
     */
    types                   : {
        OBJECT              : "object",
        UNDEFINED           : "undefined"
    }

};

/**
 * @namespace
 * @summary A namespace for environment variables related to DOM/HTML.
 */
var HTML                    = {

    /**
     * @summary The commands that could be fired on the "document".
     * @enum    {string}
     */
    commands                : {
        COPY                : "Copy",
    },

    /**
     * @summary The event message types the document could listen to.
     * @enum    {string}
     */
    messages                : {
        COPY                : "copy"
    },

    /**
     * @summary The mime types understood by the system clipboard.
     * @enum    {string}
     */
    mimeTypes               : {
        TEXT                : "text/plain"
    }
};

// ========================================================================
// EVENT HANDLERS

/**
 * @summary The main "entry" function into the algorithm,
 *          It will obtain the current url, load the user default
 *          as well as site-specific attributes from the browser's
 *          preference system, and call into the algorithm.
 * @return  {undefined}
 */
document.addEventListener(ENV.events.DOM_CONTENT_LOADED,
                          function() {
    console.info(ENV.logCategory +
                 "Configuring elements...");
    var tabs = chrome.tabs.query({
        active          : true,
        currentWindow   : true
    }, function(arrayOfTabs) {
        // Get the current URL
        var activeTab = arrayOfTabs[0];
        var url = activeTab.url;
        console.info(ENV.logCategory + "url=" + url);

        chrome.storage.sync.get({
            saltKey             : "",
            defaultIterations   : ENV.defaultIterations,
            siteAttributesList  : ""
        }, function(items) {
            if (chrome.runtime.lastError) {
                console.error(ENV.logCategory +
                              "ERROR loading default options" +
                              ", errorMsg=" + chrome.runtime.lastError);
                // No need to return; defaults will be set.
                // The user can choose not to proceed.
            }

            var persistentOptions           = {};
            var params                      = {
                url                         : url,
                saltKey                     : items.saltKey,
                defaultIterations           : items.defaultIterations,
                encodedAttributesList       : items.siteAttributesList
            };

            if (ENV.types.OBJECT === typeof(DOM)) {
                DOM.configure(persistentOptions, params);
            }
        });
    });
});

// ========================================================================
// AUXILIARY FUNCTIONS

/**
 * @summary A function to "finalize" the algorithm.
 *          It has two primary responsibilities:
 *          a) Copy the generated proxy password to the system clipboard
 *          for readily pasting into the target password field,
 *          b) Save overridden attributes, if any,
 *          into the browser's preference system.
 * @param   {object} doneData - The final payload.
 *          It has the following properties:
 *          @prop   {string} doneData.password - The generated
 *                  proxy password.
 *          @prop   {string} doneData.attributesListString - The encoded
 *                  list of attributes for all urls (modified with data
 *                  from the current url if asked to save).
 *                  If this is empty, the existing attributes list string
 *                  in the browser's preference system is left untouched.
 * @return  {undefined}
 */
function finalize(doneData) {
    console.debug(ENV.logCategory + "Received 'done'..., doneData=" +
                  JSON.stringify(doneData, null, ENV.indentation));

    /**
     * @summary A function to copy the generated proxy password
     *          to the system clipboard.
     * @param   {string} password - The generated proxy password.
     * @return  {function}
     */
    var copyPasswordToClipboard = function(password) {
        document.addEventListener(HTML.messages.COPY,
                                  function copyPassword(e) {
            document.removeEventListener(HTML.messages.COPY,
                                         copyPassword,
                                         false);
            e.clipboardData.setData(HTML.mimeTypes.TEXT, password);
            // Prevent random selections from interfering with our data
            e.preventDefault();
        });
        document.execCommand(HTML.commands.COPY, false, null);
    }

    console.info(ENV.logCategory + "Copying to clipboard...");
    // Copy the generated password to clipboard.
    copyPasswordToClipboard(doneData.password);

    // Save the attributes list string, if non-empty, to sync.
    if ("" !== doneData.attributesListString) {
        console.info(ENV.logCategory + "Saving site attributes...");
        chrome.storage.sync.set({
            siteAttributesList  : doneData.attributesListString,
        }, function() {
            // Check if the options were successfully saved.
            var success = false;
            if (chrome.runtime.lastError) {
                console.error(ENV.logCategory +
                              "ERROR saving attributes" +
                              ", errorMsg=" + chrome.runtime.lastError);
            } else {
                console.info(ENV.logCategory +
                             "Attributes saved successfully");
                success = true;
            }

            if (ENV.types.OBJECT === typeof(DOM)) {
                DOM.togglers.toggleAttributesSaveSuccess(success);
            }
        });
    }
}