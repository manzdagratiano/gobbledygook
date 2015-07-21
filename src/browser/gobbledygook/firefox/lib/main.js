/**
 * @module      main
 * @overview    The entry point into the addon.
 *              The "main" module has access to all the high-level APIs
 *              for interacting with the browser.
 *              This module does not implement any algorithm logic itself,
 *              but merely extracts parameters from,
 *              and saves parameters to, the browser environment.
 *              It communicates with the main addon logic using
 *              message passing.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 07, 2014
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
     * @summary The actionable events that could be triggered when
     *          interacting with the UI.
     * @enum    {string}
     */
    events                  : {
        DONE                : "done",
        GENERATE_SALT_KEY   : "generateSaltKey",
        HIDE                : "hide",
        SET_SALT_KEY        : "setSaltKey",
        SAVE                : "save",
        SHOW                : "show"
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
     * @summary The mime types recognized by the clipboard.
     * @enum    {string}
     */
    mimeTypes               : {
        TEXT                : "text"
    },

    /**
     * @summary The list of all the user "preferences" stored in the
     *          browser's preferences system.
     */
    preferences             : {
        defaultIterations   : "defaultIterations",
        generateSaltKey     : "generateSaltKey",
        saltKey             : "saltKey",
        siteAttributesList  : "siteAttributesList",
        unlockSaltKey       : "unlockSaltKey"
    }

};

// ========================================================================
// SDK METHODS

var self                = require("sdk/self");
var clipboard           = require("sdk/clipboard");
var panels              = require("sdk/panel");
var tabs                = require("sdk/tabs");
var { ToggleButton }    = require('sdk/ui/button/toggle');
var simplePrefs         = require('sdk/simple-prefs');
var prefs               = simplePrefs.prefs;
var prefsService        = require('sdk/preferences/service');

// ========================================================================
// TOGGLE BUTTON

/**
 * @summary The toggle button, clicking which shows the addon UI
 *          and emits the "show" event for the addon code to intercept.
 *          Clicking again will hide the UI and emit the "hide" event
 *          for the addon code.
 */
var button = ToggleButton({
    id          : "Gobbledygook",
    label       : "Generate a password",
    icon        : {
        "16"    : "./icon/icon-16.png",
        "32"    : "./icon/icon-32.png",
        "48"    : "./icon/icon-48.png",
        "64"    : "./icon/icon-64.png"
    },
    onChange    : handleChange
});

/**
 * @summary Event handler for changing the state of the ToggleButton
 * @return  {undefined}
 */
function handleChange(state) {
    if (state.checked) {
        panel.show({
            position    : button
        });
    }
}

// ========================================================================
// PANEL

/**
 * @summary The addon panel, which hosts the UI of the addon.
 *          The addon logic is encompassed in the "contentScriptFiles"
 *          which are known to this panel.
 */
var panel = panels.Panel({
    width                   : 350,
    height                  : 510,
    contentURL              : self.data.url("gobbledygook.html"),
    contentScriptFile       : [
        self.data.url("keygen.js"),
        self.data.url("workhorse.js"),
        self.data.url("workhorsefunctions.js"),
        self.data.url("sjcl/sjcl_megalith.js")
        ],
    contentScriptOptions    : {},
    onHide                  : handleHide
});

/**
 * @summary Event handler for the "show" event. It obtains the necessary
 *          parameters from the url and the browser's preference system,
 *          and emits the "show" event with this payload for the
 *          addon code to intercept.
 * @return  {undefined}
 */
panel.on(ENV.events.SHOW, function() {
    panel.port.emit(ENV.events.SHOW, {
        url                     : tabs.activeTab.url,
        saltKey                 : prefs[ENV.preferences.saltKey],
        defaultIterations       : prefs[ENV.preferences.defaultIterations],
        encodedAttributesList   : prefs[ENV.preferences.siteAttributesList]
    });
});

/**
 * @summary Event handler for the "hide" event. It hides the addon panel.
 * @return  {undefined}
 */
function handleHide() {
    button.state('window', {checked : false});
}

/**
 * @summary Event handler for the "hide" event.
 *          It emits the "hide" event for the addon code to intercept.
 * @return  {undefined}
 */
panel.on(ENV.events.HIDE, function() {
    panel.port.emit(ENV.events.HIDE);
});

/**
 * @summary Event handler for the "done" event received from the
 *          addon code, in other words, function to "finalize"
 *          the algorithm.
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
 *                  list of attributes for all domains (modified with data
 *                  from the current domain if asked to save).
 *                  If this is empty, the existing attributes list string
 *                  in the browser's preference system is left untouched.
 * @return  {undefined}
 */
panel.port.on(ENV.events.DONE, function (doneData) {
    console.debug(ENV.logCategory + "Received 'done'..., doneData=" +
                  JSON.stringify(doneData, null, ENV.indentation));

    // Copy the proxy password to the clipboard.
    console.info(ENV.logCategory + "Copying to clipboard...");
    clipboard.set(doneData.password, ENV.mimeTypes.TEXT);

    // Check if attributes need to be saved into the preference system.
    if ("" !== doneData.attributesListString) {
        console.info(ENV.logCategory + "Saving site attributes...");
        prefs[ENV.preferences.siteAttributesList] =
            doneData.attributesListString;
        panel.port.emit(ENV.events.SAVE, true);
    }
});

// ========================================================================
// SYNC PREFERENCES

// Create a syncable attribute for each of the preferences for the
// addon, so that their values are sync'ed across machines.
var prefKeys = prefsService.keys('extensions.' + self.id);
prefKeys.forEach(function(preferenceName) {
    prefsService.set('services.sync.prefs.sync.extensions.' +
                     self.id + preferenceName,
                     true);
});

/**
 * @summary Event listener for the "Generate Key" button
 *          in the Options UI.
 *          It calls out to the 'Workhorse' to do the actual generation,
 *          but IFF the "editSaltKey" radio button is switched "On"
 *          (to prevent accidental resets of the key).
 * @return  {undefined}
 */
function onGenerateSaltKey() {
    console.info(ENV.logCategory + "Generating salt 'key'...");
    if (prefs[ENV.preferences.unlockSaltKey] !== "N") {
        console.info(ENV.logCategory +
                     "'unlockSaltKey' is unlocked. Proceeding...");
        panel.port.emit(ENV.events.GENERATE_SALT_KEY);
    } else {
        console.info(ENV.logCategory +
                     "'unlockSaltKey' is locked. Doing nothing.");
    }
}
simplePrefs.on(ENV.preferences.generateSaltKey, onGenerateSaltKey);

/**
 * @summary Event listener for the "setSaltKey" event
 *          fired from the Workhorse.
 * @return  {undefined}
 */
panel.port.on(ENV.events.SET_SALT_KEY, function (key) {
    // Set the salt key
    prefs[ENV.preferences.saltKey] = key;
    // Automatically re-lock the salt key to
    // prevent accidental regeneration.
    prefs[ENV.preferences.unlockSaltKey] = "N";
});