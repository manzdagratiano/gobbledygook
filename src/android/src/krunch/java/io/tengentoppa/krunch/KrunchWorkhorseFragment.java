/**
 * @file        KrunchWorkhorseFragment.java
 * @summary     Source file for the KrunchWorkhorseFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.krunch;

// Libraries
import io.tengentoppa.yggdrasil.WorkhorseFragment;

// Android
import android.os.Bundle;

/**
 * @summary The KrunchWorkhorseFragment class
 *          This class extends the WorkhorseFragment class to
 *          provide specific implementations of the
 *          retrieveIngredients method.
 */
public class KrunchWorkhorseFragment extends WorkhorseFragment {

    /**
     * @summary Method to return a new instance of the
     *          concrete implementation of the workhorse fragment.
     * @return  {KrunchWorkhorseFragment} Returns an instance
     *          of the KrunchWorkhorseFragment class
     */
    static KrunchWorkhorseFragment
    newInstance(final String url,
                final boolean showAsDialog) {
        KrunchWorkhorseFragment workhorse =
            new KrunchWorkhorseFragment();
        Bundle args = new Bundle();

        fillBundle(args,
                   url,
                   showAsDialog);
        workhorse.setArguments(args);

        return workhorse;
    }

    // ====================================================================
    // PRIVATE MEMBERS

    /**
     * @summary Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

}
