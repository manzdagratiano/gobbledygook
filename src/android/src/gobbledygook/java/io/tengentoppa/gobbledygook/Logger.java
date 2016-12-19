/**
 * @file        Logger.java
 * @summary     The Logger class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Apr 10, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.gobbledygook;

/**
 * @summary The Logger class.
 */
public class Logger {

    // --------------------------------------------------------------------
    // PUBLIC METHODS

    /**
     * @summary Method to return the log category for the app.
     * @return  {String} The log category
     */
    public static String getCategory() {
        return LOG_CATEGORY;
    }

    // --------------------------------------------------------------------
    // PRIVATE METHODS

    private static final String LOG_CATEGORY    = "GOBBLEDYGOOK";

}
