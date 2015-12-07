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
var OPTIONS             = {

    /**
     * @summary The default number of iterations of PBKDF2 (when unset).
     */
    defaultIterations       : 10000,

    /**
     * @summary The modes of display for a panel that can be shown/hidden.
     */
    display                 : {
        NONE                : "none"
    },

    /**
     * @summary The actionable events that could be triggered when
     *          interacting with the UI.
     * @enum    {string}
     */
    events                  : {
        DOM_CONTENT_LOADED  : "DOMContentLoaded",
        CHANGE              : "change",
        CLICK               : "click",
        EXPORT              : "Export",
        IMPORT              : "Import"
    },

    /**
     * @summary The indentation for "pretty-printing" JSON objects.
     */
    indentation             : 4,

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "OPTIONS: ",

    /**
     * @summary The list of all the user "preferences" stored in the
     *          browser's preferences system.
     */
    preferences             : {
        defaultIterations   : "defaultIterations",
        saltKey             : "saltKey",
        siteAttributesList  : "siteAttributesList"
    },

    /**
     * @summary The name of the settings file when exporting
     */
    settingsFile            : "gobbledygook.json",

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

};  // end namepace OPTIONS

/**
 * @namespace
 * @summary A namespace for elements of the DOM of the Options page.
 */
var OPT_DOM             = {
    saltKeyBox              : "saltKey",
    editSaltKeyCheckBox     : "editSaltKey",
    generateKeyButton       : "generateKeyButton",
    defaultIterationsBox    : "defaultIterations",
    siteAttributesListBox   : "siteAttributesList",
    saveButton              : "saveButton",
    optionsSaveSuccessLabel : "optionsSaveSuccessLabel",
    exportSettingsIcon      : "exportSettingsIcon",
    importSettingsIcon      : "importSettingsIcon"
};

// ========================================================================
// FUNCTIONS

/**
 * @summary A method to toggle the editability of the Salt Key.
 *          When the "Edit" checkbox is checked, the salt key textbox is
 *          enabled for editing and the "Generate New Key" button is enabled.
 * @return  {undefined}
 */
function toggleSaltKey() {
    var checkBox =
        document.getElementById(OPT_DOM.editSaltKeyCheckBox);
    var saltKeyBox =
        document.getElementById(OPT_DOM.saltKeyBox);
    var generateKeyButton =
        document.getElementById(OPT_DOM.generateKeyButton);

    if (checkBox.checked) {
        console.info(OPTIONS.logCategory +
                     "Enabling the salt key for editing...");
        // Allow the Salt Key to be edited
        saltKeyBox.disabled = false;
        // Enable the "Generate Salt Key"
        generateKeyButton.disabled = false;

        // Generate a key when the "Generate Key" button is clicked
        generateKeyButton.addEventListener(OPTIONS.events.CLICK,
                                           generateKey);
    } else {
        console.info(OPTIONS.logCategory +
                     "Locking the salt key...");
        generateKeyButton.removeEventListener(OPTIONS.events.CLICK,
                                              generateKey);
        generateKeyButton.disabled = true;
        saltKeyBox.disabled = true;
    }
}

/**
 * @summary Function to generate the 'key' for creating salts from domains.
 * @return  {undefined}
 */
function generateKey() {
    console.info(OPTIONS.logCategory +
                 "Generating salt 'key'...");
    // Empty out the "optionsSaveSuccessLabel", if set.
    document.getElementById(
                OPT_DOM.optionsSaveSuccessLabel).textContent = "";
    var saltBox = document.getElementById(OPT_DOM.saltKeyBox);
    saltBox.value = "<Generating...>";

    var keyGenerated = false;
    if (OPTIONS.types.OBJECT === typeof(Keygen)) {
        var key = Keygen.generate();
        if (OPTIONS.types.UNDEFINED !== typeof(key)) {
            keyGenerated = true;
        }
    } else {
        console.error(OPTIONS.logCategory +
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
    console.info(OPTIONS.logCategory +
                 "Saving default options...");
    chrome.storage.sync.set({
        saltKey             : document.getElementById(
                                OPT_DOM.saltKeyBox).value,
        defaultIterations   : parseInt(document.getElementById(
                                OPT_DOM.defaultIterationsBox).value),
        siteAttributesList  : document.getElementById(
                                OPT_DOM.siteAttributesListBox).value,
    }, function() {
        // Check if the options were successfully saved
        if (chrome.runtime.lastError) {
            console.error(OPTIONS.logCategory +
                          "ERROR saving options" +
                          ", errorMsg=" + chrome.runtime.lastError);
            document.getElementById(
                        OPT_DOM.optionsSaveSuccessLabel).textContent =
                OPTIONS.successString.FAILURE;
            return;
        }

        console.info(OPTIONS.logCategory + "Options saved");
        document.getElementById(
                    OPT_DOM.optionsSaveSuccessLabel).textContent =
            OPTIONS.successString.SUCCESS;
    });
}

/**
 * @summary Function to restore user default attributes to the options
 *          page from the browser's preference system.
 * @return  {undefined}
 */
function loadOptions() {
    console.info(OPTIONS.logCategory +
                 "Loading default options...");

    // Set default Salt Key state
    document.getElementById(OPT_DOM.editSaltKeyCheckBox).checked = false;
    document.getElementById(OPT_DOM.saltKeyBox).disabled = true;
    document.getElementById(OPT_DOM.generateKeyButton).disabled = true;

    // Restore options from the preferences system
    chrome.storage.sync.get({
        saltKey             : "",
        defaultIterations   : OPTIONS.defaultIterations,
        siteAttributesList  : ""
    }, function(items) {
        if (chrome.runtime.lastError) {
            console.info(OPTIONS.logCategory +
                         "ERROR loading options" +
                         ", errorMsg=" + chrome.runtime.lastError);
            // No need to return; defaults will be set.
            // The user can choose not to proceed susbequently.
        }

        // Set the DOM elements
        setDomElements(items);

        // Check if the options page was loaded as a result of
        // the user requesting an import/export action,
        // and if so, execute the action
        chrome.storage.local.get({
            action  : ""
        }, function(items) {
            if (chrome.runtime.lastError) {
                console.info(OPTIONS.logCategory +
                             "ERROR loading localStorage" +
                             ", errorMsg=" + chrome.runtime.lastError);
                // In case if error, the "action" property will remain unset
                // and nothing will be done
            }

            console.info(OPTIONS.logCategory + "localStorage loaded, " +
                         "items=" + JSON.stringify(items));
            var action = items.action;
            if ("" === action) {
                return;
            }

            // Reset the localStorage variable, and take the action
            console.info(OPTIONS.logCategory + "Resetting localStorage...");
            chrome.storage.local.set({
                action  : ""
            }, function() {
                if (chrome.runtime.lastError) {
                    console.info(OPTIONS.logCategory +
                                 "ERROR resetting localStorage!");
                }

            });

            if (OPTIONS.events.EXPORT === action) {
                exportSettings();
            } else if (OPTIONS.events.IMPORT === action) {
                importSettings();
            }
        });
    });
}

/**
 * @summary Method to set the DOM elements from the passed in object
 * @param   {Object} settings - The settings object
 *          @prop {string} saltKey
 *          @prop {int} defaultIterations
 *          @prop {string} siteAttributesList
 * @return  {undefined}
 */
function setDomElements(settings) {
    // Sanity check
    var ids = OPTIONS.preferences;
    if (!(settings.hasOwnProperty(ids.saltKey) &&
          settings.hasOwnProperty(ids.defaultIterations) &&
          settings.hasOwnProperty(ids.siteAttributesList) &&
          (3 === Object.keys(settings).length))) {
        console.info(OPTIONS.logCategory + "Malformed settings!");
        return;
    }

    document.getElementById(OPT_DOM.saltKeyBox).value =
        settings.saltKey;
    document.getElementById(OPT_DOM.defaultIterationsBox).value =
        settings.defaultIterations;
    document.getElementById(OPT_DOM.siteAttributesListBox).value =
        settings.siteAttributesList;
    console.info(OPTIONS.logCategory + "DOM elements configured.");
}

/**
 * @summary A method called when the "Export Settings" icon is clicked
 * @return  {undefined}
 */
function exportSettings() {
    console.info(OPTIONS.logCategory + "exportSettings() handler called.");
    chrome.storage.sync.get({
        saltKey             : "",
        defaultIterations   : OPTIONS.defaultIterations,
        siteAttributesList  : ""
    }, function(items) {
        if (chrome.runtime.lastError) {
            console.error(OPTIONS.logCategory +
                          "ERROR loading default options, " +
                          "errorMsg=" + chrome.runtime.lastError);
            // Nothing to export, return
            return;
        }
        // We could try to use the window.requestFileSystem API,
        // but it is not a web standard, and is chrome-specific ATM.
        // Probably a bad idea to use unless it is wrapped into 
        // a native chrome.* API.
        // We'll instead use the HTML5 "download" attribute.
        console.info(OPTIONS.logCategory +
                     "Creating temporary download element...");
        // Create a new active ('a', as in <a href="..." />) element
        var a = window.document.createElement('a');
        a.href = window.URL.createObjectURL(new Blob([
                                    JSON.stringify(items,
                                                   null,
                                                   OPTIONS.indentation)
        ],{
            type    : "application/json"
        }));
        a.download = OPTIONS.settingsFile;
        a.style.display = OPTIONS.display.NONE;
        // Append the anchor to the body, simulate a click, and remove it
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    });
}

/**
 * @summary A method called when the "Import Settings" icon is clicked
 * @return  {undefined}
 */
function importSettings() {
    console.info(OPTIONS.logCategory + "importSettings() handler called. " +
                 "Creating temporary fileinput element...");
    var fileInput = window.document.createElement('input');
    fileInput.type = "file";
    fileInput.accept = "application/json";
    fileInput.addEventListener("change", function(e) {
        console.info(OPTIONS.logCategory + "importSettings.change(): " +
                     "files=" + JSON.stringify(this.files));

        var settingsFile = this.files[0];

        var fileReader = new FileReader();
        fileReader.onload = (function(settingsFile) {
            return function(e) {
                var settings = JSON.parse(e.target.result);
                console.info(OPTIONS.logCategory +
                             "importSettings.onload(): settings=" +
                             JSON.stringify(settings));

                // Sanity check
                var ids = OPTIONS.preferences;
                if (!(settings.hasOwnProperty(ids.saltKey) &&
                      settings.hasOwnProperty(ids.defaultIterations) &&
                      settings.hasOwnProperty(ids.siteAttributesList) &&
                      (3 === Object.keys(settings).length))) {
                    console.info(ENV.logCategory + "Malformed settings file!");
                    return;
                }

                // Save the options read
                console.info(OPTIONS.logCategory +
                             "Saving options to browser...");
                chrome.storage.sync.set({
                    saltKey             : settings[ids.saltKey],
                    defaultIterations   : parseInt(settings[ids.defaultIterations]),
                    siteAttributesList  : settings[ids.siteAttributesList]
                }, function() {
                    // Check if the options were successfully saved
                    if (chrome.runtime.lastError) {
                        console.error(OPTIONS.logCategory +
                                      "ERROR saving options" +
                                      ", errorMsg=" + chrome.runtime.lastError);
                        return;
                    }

                    console.info(OPTIONS.logCategory + "Options saved");

                    // The view will not refresh automatically; do so manually
                    setDomElements(settings);
                });
            };
        })(settingsFile);

        fileReader.readAsText(settingsFile);

    }, false);
    document.body.appendChild(fileInput);
    fileInput.click();
    document.body.removeChild(fileInput);
}

// ========================================================================
// EVENT HANDLERS

// Restore options on loading the "Options" page
document.addEventListener(OPTIONS.events.DOM_CONTENT_LOADED,
                          loadOptions);

// Enable/disable editing the salt key
document.getElementById(OPT_DOM.editSaltKeyCheckBox).addEventListener(
                                                        OPTIONS.events.CHANGE,
                                                        toggleSaltKey);

// Save options when the "Save" button is clicked
document.getElementById(OPT_DOM.saveButton).addEventListener(
                                                OPTIONS.events.CLICK,
                                                saveOptions);

// Configure "optionsSaveSuccessLabel"
document.getElementById(OPT_DOM.optionsSaveSuccessLabel).textContent = "";

// Settings Export/Import
document.getElementById(OPT_DOM.exportSettingsIcon).addEventListener(
                                                        OPTIONS.events.CLICK,
                                                        exportSettings);
document.getElementById(OPT_DOM.importSettingsIcon).addEventListener(
                                                        OPTIONS.events.CLICK,
                                                        importSettings);
