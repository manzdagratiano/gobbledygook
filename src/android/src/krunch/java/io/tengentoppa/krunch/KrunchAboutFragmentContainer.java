/**
 * @file        KrunchAboutFragmentContainer.java
 * @summary     Source file for the Krunch implementation of
 *              the AboutFragmentContainer class.
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
import io.tengentoppa.yggdrasil.AboutFragmentContainer;

// Android
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * @summary The KrunchAboutFragmentContainer class
 */
public class KrunchAboutFragmentContainer
    extends AboutFragmentContainer {

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

    /**
     * @summary Method to obtain an instance of the "About" fragment.
     * @return  Does not even.
     */
    @Override
    protected PreferenceFragmentCompat getAboutFragment() {
        return new KrunchAboutFragment();
    }

}
