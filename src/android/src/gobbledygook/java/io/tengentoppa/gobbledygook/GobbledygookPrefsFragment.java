/**
 * @file        GobbledygookPrefsFragment.java
 * @summary     Source file for the Gobbledygook implementation of
 *              the PrefsFragment class.
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
import io.tengentoppa.yggdrasil.PrefsFragment;

// Android
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * @summary The GobbledygookPrefsFragment class
 */
public class GobbledygookPrefsFragment extends PrefsFragment {

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
     * @summary Method to return an instance of the specific implementation
     *          of the SaltKeyActionsFragment class.
     * @return  {DialogFragment} An instance of
     *          the GobbledygookSaltKeyActionsFragment class.
     */
    @Override
    protected DialogFragment
    getSaltKeyActionsFragment(final boolean showAsDialog) {
        return GobbledygookSaltKeyActionsFragment.newInstance(showAsDialog);
    }

}
