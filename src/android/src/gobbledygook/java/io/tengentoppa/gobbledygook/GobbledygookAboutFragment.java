/**
 * @file        GobbledygookAbout.java
 * @summary     Source file for the Gobbledygook implementation of
 *              the AboutFragment class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.gobbledygook;

// Libraries
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.AboutFragment;

/**
 * @summary The GobbledygookAboutFragment class
 */
public class GobbledygookAboutFragment extends AboutFragment {

    // ====================================================================
    // PRIVATE METHODS

    /**
     * @summary Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

}
