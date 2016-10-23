/**
 * @file        KrunchAbout.java
 * @brief       Source file for the Krunch! implementation of
 *              the AboutFragment class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.krunch;

// Libraries
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.AboutFragment;

/**
 * @brief  The KrunchAboutFragment class
 */
public class KrunchAboutFragment extends AboutFragment {

    // ====================================================================
    // PRIVATE METHODS

    /**
     * @brief   Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

}
