/**
 * @module      index
 * @overview    The entry point into the addon.
 *              The "index" module has access to all the high-level APIs
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
        SHOW                : "show",
        SETTINGS_SHOW       : "showSettings",
        SETTINGS_EXPORT     : "exportSettings",
        SETTINGS_IMPORT     : "importSettings"
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

    settingsFile            : "gobbledygook.json",

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

var self                = require('sdk/self');
var {Cc, Ci}            = require('chrome');
var clipboard           = require('sdk/clipboard');
var downloadDir         = require('sdk/system').pathFor('DfltDwnld');
var file                = require('sdk/io/file');
var notifications       = require('sdk/notifications');
var panels              = require("sdk/panel");
var path                = require('sdk/fs/path');
var prefsService        = require('sdk/preferences/service');
var simplePrefs         = require('sdk/simple-prefs');
var tabs                = require('sdk/tabs');
var { ToggleButton }    = require('sdk/ui/button/toggle');
var views               = require('sdk/view/core');
var windows             = require('sdk/window/utils');

var filePicker          = Cc["@mozilla.org/filepicker;1"].createInstance(
                                Ci.nsIFilePicker);
var prefs               = simplePrefs.prefs;

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
    height                  : 430,
    contentURL              : self.data.url("./gobbledygook.html"),
    contentScriptFile       : [
        self.data.url("./keygen.js"),
        self.data.url("./workhorse.js"),
        self.data.url("./workhorsefunctions.js"),
        self.data.url("./sjcl/sjcl_megalith.js")
        ],
    contentScriptOptions    : {},
    onHide                  : handleHide
});

// A bug in the addon SDK causes tooltips set with the "title" attribute
// in HTML to not show up.
// (https://bugzilla.mozilla.org/show_bug.cgi?id=918600)
// Assign a tooltip to the view, which will fix this.
views.getActiveView(panel).setAttribute('tooltip', 'aHTMLTooltip');

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
simplePrefs.on(ENV.preferences.generateSaltKey, function() {
    console.info(ENV.logCategory + "Generating salt 'key'...");
    if (prefs[ENV.preferences.unlockSaltKey] !== "N") {
        console.info(ENV.logCategory +
                     "'unlockSaltKey' is unlocked. Proceeding...");
        panel.port.emit(ENV.events.GENERATE_SALT_KEY);
    } else {
        console.info(ENV.logCategory +
                     "'unlockSaltKey' is locked. Doing nothing.");
    }
});

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

// ========================================================================
// SETTINGS

/**
 * @summary Event listener for the "showSettings" event
 * @return  {undefined}
 */
panel.port.on(ENV.events.SETTINGS_SHOW, function () {
    console.info(ENV.logCategory + "Opening addons tab...");
    tabs.open("about:addons");
});

/**
 * @summary Event listener for the "exportSettings" event.
 *          This method will gather the settings from SimplePrefs
 *          and export them to a JSON file in the user's
 *          default download directory.
 * @return  {undefined}
 */
panel.port.on(ENV.events.SETTINGS_EXPORT, function () {
    console.info(ENV.logCategory + "Exporting preferences...");
    // Construct the settings JSON object
    var settings = getApplicationSettings();

    // Open the file picker for the user to select the download location
    var thisWindow = windows.getMostRecentBrowserWindow();
    filePicker.init(thisWindow, "Save As...", Ci.nsIFilePicker.modeSave);
    filePicker.appendFilter("Text (JSON)", "*.json");
    var returnCode = filePicker.show();
    // If the file picker was closed by the user without hitting "Ok",
    // i.e., without selecting a file
    if (!(Ci.nsIFilePicker.returnOK === returnCode ||
          Ci.nsIFilePicker.returnReplace === returnCode)) {
        console.info(ENV.logCategory + "Export canceled. Nothing to do");
        return;
    }

    // Write to the file
    // var absPath = path.join(downloadDir, ENV.settingsFile);
    var absPath = filePicker.file.path;
    var fileHandle = file.open(absPath, "w");
    fileHandle.write(JSON.stringify(settings, null, ENV.indentation));
    fileHandle.close();

    // Notify the user
    notifications.notify({
        title   : "Export Settings",
        iconURL : "./icon/icon-64.png",
        text    : "Successfully exported settings to " + absPath,
        onClick : function(data) {
            // Do nothing
        }
    });
});

/**
 * @summary Event listener for the "importSettings" event.
 *          This method will read in a JSON file in the correct
 *          format, and set the SimplePrefs attributes from it.
 * @return  {undefined}
 */
panel.port.on(ENV.events.SETTINGS_IMPORT, function () {
    console.info(ENV.logCategory + "Importing preferences...");

    var thisWindow = windows.getMostRecentBrowserWindow();
    filePicker.init(thisWindow, "Open File...", Ci.nsIFilePicker.modeOpen);
    filePicker.appendFilter("Text (JSON)", "*.json");
    var returnCode = filePicker.show();
    // If the file picker was closed by the user without hitting "Ok",
    // i.e., without selecting a file
    if (Ci.nsIFilePicker.returnOK !== returnCode) {
        console.info(ENV.logCategory + "Import canceled. Nothing to do");
        return;
    }

    var filePath = filePicker.file.path;
    if (!file.exists(filePath)) {
        console.info(ENV.logCategory + "file=" + filePath +
                     ", does not exist!");
        return;
    }
    var fileHandle = file.open(filePath, "r");
    var settingsStr = fileHandle.read();
    fileHandle.close();
    console.info(ENV.logCategory + "settings=" + settingsStr);

    // Parse the string into a JSON object
    var settings = {};
    try {
        settings = JSON.parse(settingsStr);
    } catch (e) {
        console.info(ENV.logCategory +
                     "WARN Failed to parse settings" +
                     ", errorMsg=\"" + e + "\"");
        return;
    }

    setApplicationSettings(settings);
});

// -----------------------------------------------------------------------
// Utilities

/**
 * @summary Method to obtain a settings object from SimplePrefs
 * @return  {Object} A settings object with the three attributes
 *          saltKey, defaultIterations and siteAttributesList
 */
function getApplicationSettings() {
    var ids = ENV.preferences;
    var settings = {};
    settings[ids.saltKey] = prefs[ids.saltKey];
    settings[ids.defaultIterations] = prefs[ids.defaultIterations];
    settings[ids.siteAttributesList] = prefs[ids.siteAttributesList];
    return settings;
}

/**
 * @summary Method to set the application settings from a JSON object
 * @param   {Object} A JSON object with:
 *          @prop   {string} saltKey - salt key
 *          @prop   {int} defaultIterations - default # of PBKDF2 iterations
 *          @prop   {string} siteAttributesList - stringified JSON of
 *                  encoded siteAttributes list
 * @return  {undefined}
 */
function setApplicationSettings(settings) {
    // Sanity check
    var ids = ENV.preferences;
    if (!(settings.hasOwnProperty(ids.saltKey) &&
          settings.hasOwnProperty(ids.defaultIterations) &&
          settings.hasOwnProperty(ids.siteAttributesList) &&
          (3 === Object.keys(settings).length))) {
        console.info(ENV.logCategory + "Malformed settings file!");
        // Notify the user
        notifications.notify({
            title   : "Malformed Settings!",
            iconURL : "./icon/icon-64.png",
            text    : "The settings file chosen is invalid!",
            onClick : function(data) {
                // Do nothing
            }
        });
        return;
    }

    // Safe to proceed; import the settings
    prefs[ids.saltKey] = settings[ids.saltKey];
    prefs[ids.defaultIterations] = settings[ids.defaultIterations];
    prefs[ids.siteAttributesList] = settings[ids.siteAttributesList];
    console.info(ENV.logCategory + "Successfully imported settings!");
}
