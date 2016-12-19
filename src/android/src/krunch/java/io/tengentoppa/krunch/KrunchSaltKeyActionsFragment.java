/**
 * @file        KrunchSaltKeyActionsFragment.java
 * @summary     Source file for the Krunch implementation of
 *              the SaltKeyActionsFragment class.
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
import io.tengentoppa.yggdrasil.SaltKeyActionsFragment;

// Android
import android.os.Bundle;

/**
 * @summary The KrunchSaltKeyActionsFragment class
 */
public class KrunchSaltKeyActionsFragment
    extends SaltKeyActionsFragment {

    /**
     * @summary Static method to return a new instance of self
     * @return  {SaltKeyActionsFragment} Returns an instance of self
     */
    protected static
    SaltKeyActionsFragment newInstance(final boolean showAsDialog) {
        SaltKeyActionsFragment saltKeyActions =
            new KrunchSaltKeyActionsFragment();
        Bundle args = new Bundle();
        fillBundle(args,
                   showAsDialog);
        saltKeyActions.setArguments(args);

        return saltKeyActions;
    }

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
