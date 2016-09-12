/**
 * @module      eventpage
 * @overview    The background "eventpage" of the system.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Dec 14, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

"use strict";

// ========================================================================
// GLOBAL CONSTANTS

/**
 * @namespace
 * @summary A global namespace for miscellaneous "environment" variables.
 */
var BACKGROUND              = {

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "BACKGROUND: "

};

/**
 * @summary A listener for messages fired from the extensions UI
 * @param   {Object} request - The JSON payload
 *          @prop {string}  action - The action to perform when opening
 *                          the options page - Import/Export/undefined
 * @param   {Object} sender - Caller information
 * @param   {Object} sendResponse - The callback function
 * @return  {undefined}
 */
chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
    console.info(BACKGROUND.logCategory + "onMessage(): " +
                 "request=" + JSON.stringify(request) +
                 ", sender=" + JSON.stringify(sender));
    if (request.type !== "background") {
        return;
    }

    // Since Chrome 40, the options page for the extension has changed semantics;
    // the chrome.tabs API does not work with the options page in the same way
    // as with normal pages.

    // Open the options page
    // If the user requested Export/Import, we need to open the options page
    // and take that action automatically.
    // However, opening the page and then firing a message to the options JS
    // will not work, since the options page is not yet loaded.
    // Instead, we will store a variable in localStorage, and check for it
    // when the DOM of the options page is loaded
    console.info(BACKGROUND.logCategory + "Setting localStorage, " +
                 "action=" + request.action);
    chrome.storage.local.set({
        action  : request.action
    }, function() {
        if (chrome.runtime.lastError) {
            console.info(BACKGROUND.logCategory +
                         "ERROR setting localStorage!");
            // No need to not proceed;
            // the user has the import/export functionality available -
            // they'll just have to manually click the icons.
        }

        console.info(BACKGROUND.logCategory + "Opening options page...");
        if (chrome.runtime.openOptionsPage) {
            chrome.runtime.openOptionsPage(function() {
                console.info(BACKGROUND.logCategory + "Options page opened");
            });
        } else {
            window.open(chrome.runtime.getURL("options.html"));
        }
    });

    sendResponse({ msg : BACKGROUND.logCategory + "Done" });
})
