/**
 * @file        KrunchPrefsFragmentContainer.java
 * @summary     Source file for the Krunch implementation of
 *              the PrefsFragmentContainer class.
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
import io.tengentoppa.yggdrasil.PrefsFragmentContainer;

// Android
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * @summary The KrunchPrefsFragmentContainer class
 */
public class KrunchPrefsFragmentContainer
    extends PrefsFragmentContainer {

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
     * @summary Method to obtain an instance of the "Prefs" fragment.
     * @return  Does not even.
     */
    @Override
    protected PreferenceFragmentCompat getPrefsFragment() {
        return new KrunchPrefsFragment();
    }

}
