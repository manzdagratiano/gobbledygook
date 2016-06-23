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
     *          For Google Chrome/Chromium, this is the
     *          chrome.storage.sync API, which syncs data across
     *          browser instances.
     * @return  {Object} A method.
     */
    getSyncMethod   : function() {
        return chrome.storage.sync;
    }

};
