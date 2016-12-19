/**
 * @file        GobbledygookWorkhorseFragment.java
 * @summary     Source file for the GobbledygookWorkhorseFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.gobbledygook;

// Libraries
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.WorkhorseFragment;

// Android
import android.os.Bundle;

/**
 * @summary The GobbledygookWorkhorseFragment class
 *          This class extends the WorkhorseFragment class.
 */
public class GobbledygookWorkhorseFragment extends WorkhorseFragment {

    /**
     * @summary Method to return a new instance of the
     *          concrete implementation of the workhorse fragment.
     * @return  {GobbledygookWorkhorseFragment} Returns an instance
     *          of the GobbledygookWorkhorseFragment class
     */
    static GobbledygookWorkhorseFragment
    newInstance(final String url,
                final boolean showAsDialog) {
        GobbledygookWorkhorseFragment workhorse =
            new GobbledygookWorkhorseFragment();
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
