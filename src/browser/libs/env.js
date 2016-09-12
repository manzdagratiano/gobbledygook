/**
 * @module      env
 * @overview    Common constants and methods.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 11, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

// ========================================================================
// CONSTANTS

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
     * @summary The modes of display for a panel that can be shown/hidden.
     */
    display                 : {
        BLOCK               : "block",
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
        DONE                : "done",
        ERROR               : "error",
        EXPORT              : "export",
        GENERATE            : "generate",
        IMPORT              : "import"
    },

    /**
     * @summary The indentation for "pretty-printing" JSON objects.
     */
    indentation             : 4,

    /**
     * @summary The text field input type;
     *          when "text", the text field shows its contents,
     *          when "password", the text field contents are not visible.
     * @enum    {string}
     */
    inputType                       : {
        PASSWORD                    : "password",
        TEXT                        : "text"
    },

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from.
     */
    logCategory             : "ENV: ",

    /**
     * @summary The list of all the user "preferences" stored in the
     *          browser's preferences system.
     */
    preferences             : {
        defaultIterations   : "defaultIterations",
        saltKey             : "saltKey",
        customOverrides     : "customOverrides"
    },

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
        FUNCTION            : "function",
        OBJECT              : "object",
        STRING              : "string",
        UNDEFINED           : "undefined"
    }

};  // end namespace ENV

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
        COPY                : "Copy"
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

};  // end namespace HTML

// ========================================================================
// METHODS

/**
 * @namespace
 * @summary A namespace for methods used across routines.
 */
var env                     = {

    /**
     * @brief   A method to show a notification to the user.
     * @param   {string} The message to display
     * @return  {undefined}
     */
    notifyUser              : function (message) {
        console.info(ENV.logCategory +
                     "notifyUser(): message=" + message);
        chrome.notifications.create({
            "type"      : "basic",
            "iconUrl"   : "icon/icon-48.png",
            "title"     : "Gobbledygook",
            "message"   : message
        });
    }

};
