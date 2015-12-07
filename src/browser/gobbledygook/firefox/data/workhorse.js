/**
 * @module      workhorse
 * @overview    A thin passthrough layer that serves as the transport
 *              layer between the "main" module, which interacts with
 *              high-level APIs of the browser, and the "workhorse",
 *              which implements the algorithm, via the message-passing
 *              protocols of communication.
 *              It obtains the parameters necessary for the workhorse
 *              module from the main module and passes them down,
 *              and obtains the computed payload from the workhorse
 *              and passes it up to the main module
 *              for copying to clipboard/saving overridden attributes.
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
var WORKHORSE           = {

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
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "WORKHORSE: ",

    /**
     * @summary Types referenced for the "typeof" command.
     * @enum    {string}
     */
    types                   : {
        OBJECT              : "object",
        UNDEFINED           : "undefined"
    }

};  // end namepace WORKHORSE

// ========================================================================
// EVENT HANDLERS

/**
 * @summary An event listener for the "generateSaltKey" event from
 *          the Options UI.
 * @param   {String} key - The saltKey to set. The caller can either
 *          pass in a key, or have the method generate one (this is
 *          to facilitate being called from two different contexts).
 * @return  {undefined}
 */
function onGenerateSaltKey(key) {
    // If "key" is undefined, generate one;
    if (WORKHORSE.types.undefined === key) {
        if (WORKHORSE.types.OBJECT === typeof(Keygen)) {
            key = Keygen.generate();
        } else {
            console.error(WORKHORSE.logCategory +
                          "ERROR: 'Keygen' undefined");
        }
    }

    if (WORKHORSE.types.UNDEFINED !== typeof(key)) {
        self.port.emit(WORKHORSE.events.SET_SALT_KEY, key);
    }
}
self.port.on(WORKHORSE.events.GENERATE_SALT_KEY,
             onGenerateSaltKey);

/**
 * @summary An event listener for the "show" event for the addon UI.
 * @return  {undefined}
 */
self.port.on(WORKHORSE.events.SHOW, function onShow(params) {
    if (WORKHORSE.types.OBJECT === typeof(DOM)) {
        DOM.configure(self.options, params);
    } else {
        console.error(WORKHORSE.logCategory +
                      "ERROR: 'DOM' undefined");
    }
});

/**
 * @summary An event listener for the "hide" event for the addon UI.
 * @return  {undefined}
 */
self.port.on(WORKHORSE.events.HIDE, function onHide() {
    if (WORKHORSE.types.OBJECT === typeof(DOM)) {
        DOM.deconfigure();
    } else {
        console.error(WORKHORSE.logCategory +
                      "ERROR: 'DOM' undefined");
    }
});

/**
 * @summary An event listener for the "save" event emitted from the
 *          main module.
 * @return  {undefined}
 */
self.port.on(WORKHORSE.events.SAVE, function onSave(success) {
    if (WORKHORSE.types.OBJECT === typeof(DOM)) {
        DOM.togglers.toggleAttributesSaveSuccess(success);
    } else {
        console.error(WORKHORSE.logCategory +
                      "ERROR: 'DOM' undefined");
    }
});

/**
 * @summary Method to emit the "showSettings" event.
 * @return  {undefined}
 */
function onShowSettings() {
    self.port.emit(WORKHORSE.events.SETTINGS_SHOW);
}

/**
 * @summary Method to emit the "showSettings" event.
 * @return  {undefined}
 */
function onExportSettings() {
    self.port.emit(WORKHORSE.events.SETTINGS_EXPORT);
}

/**
 * @summary Method to emit the "showSettings" event.
 * @return  {undefined}
 */
function onImportSettings() {
    self.port.emit(WORKHORSE.events.SETTINGS_IMPORT);
}

/**
 * @summary A function to "finalize" the algorithm. The finalization
 *          is implemented in the main module itself, and this
 *          routine merely emits the "done" event with the payload
 *          for the main module to intercept.
 * @param   {object} doneData - The payload to be delivered to the
 *          main module.
 * @return  {undefined}
 */
function finalize(doneData) {
    // Fire a signal to the main panel code with the result.
    self.port.emit(WORKHORSE.events.DONE, doneData);
}
