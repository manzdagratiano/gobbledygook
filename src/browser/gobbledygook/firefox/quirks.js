/**
 * @module      quirks
 * @overview    Methods for browser-specifc quirks.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 28, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2014-2016
 */

// ========================================================================
// METHODS

var Quirks          = {

    /**
     * @brief   A method to return a browser-specific method for
     *          syncing preferences.
     *          For Firefox, this is the chrome.storage.local API,
     *          since Firefox does not, at this time, support
     *          the chrome.storage.sync API.
     *          As a result, users do not get automatic syncing
     *          across browsers.
     * @return  {Object} A method.
     */
    getSyncMethod   : function() {
        return chrome.storage.local;
    }

};
