/**
 * @file        HomeFragment.java
 * @summary     Source file for the HomeFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Dec 17, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.yggdrasil;

public class WebHelper {

    /**
     * @summary Method to convert a query string to a URL.
     *          using the following waterfall:
     *          * If the query starts with "https://", we assume
     *            the user entered the full URL and return as is.
     *          * If it starts with "http://", we force https.
     *          * If it doesn't start with either of the above,
     *            but does start with "www." or "www2.",
     *            we add the prefix "https://".
     *          * If it does not start with "www." (or "www2."),
     *            but does end with one of the common domain names,
     *            we add the prefix "https://www.".
     *          * If none of the above is met, we assume a freeform
     *            search string and invoke the preponderance of Google.
     * @return  {String} A loadable url string.
     */
    public static String getUrl(String query) {
        if (query.startsWith("https://")) {
            return query;
        } else if (query.startsWith("http://")) {
            // Force https://
            // Websites that do not support https and require a login
            // are beneath us and not supported.
            return query.replace("http://", "https://");
        } else if (query.startsWith("www.") ||
                   query.startsWith("www2.")) {
            // Will be optimized to StringBuilder
            return "https://" + query;
        } else if (query.endsWith(".com") ||
                   query.endsWith(".org") ||
                   query.endsWith(".net") ||
                   query.endsWith(".edu") ||
                   query.endsWith(".gov") ||
                   query.endsWith(".io")) {
            return "https://www." + query;
        } else {
            // Assume free-form search string. Invoke Google. 
            return getGoogleSearchUrl(query);
        }
    }

    /**
     * @summary Method to convert a query string into a Google search URL.
     * @return  {String} A Google search URL.
     */
    public static String getGoogleSearchUrl(String query) {
        return ("https://www.google.com/search?q=" +
                query.replace(" ", "+"));
    }

}   // end class WebHelper
