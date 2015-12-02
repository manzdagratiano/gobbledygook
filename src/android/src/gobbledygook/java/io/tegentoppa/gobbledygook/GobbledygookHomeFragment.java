/**
 * @file        GobbledygookHomeFragment.java
 * @brief       Source file for the GobbledygookHomeFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.gobbledygook;

// Libraries
import io.tengentoppa.yggdrasil.HomeFragment;
import io.tengentoppa.yggdrasil.R;

// Android
import android.app.DialogFragment;
import android.app.FragmentTransaction;

/**
 * @brief   
 */
public class GobbledygookHomeFragment extends HomeFragment {

    /**
     * @brief   
     * @return  Does not return a value
     */
    protected void showWorkhorseDialog(String url,
                                       FragmentTransaction fragmentTx) {
        // Instantiate the fragment
        boolean showAsDialog = true;
        DialogFragment workhorseDialog =
            GobbledygookWorkhorseFragment.newInstance(url,
                                                      showAsDialog);

        // "show" will commit the transaction as well
        workhorseDialog.show(fragmentTx,
                             getString(R.string.tag_workhorseFragment));
    }

}
