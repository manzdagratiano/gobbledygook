/**
 * @file        GobbledygookWorkhorseFragment.java
 * @brief       Source file for the GobbledygookWorkhorseFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.gobbledygook;

// Libraries
import io.tengentoppa.yggdrasil.WorkhorseFragment;

// Android
import android.os.Bundle;

/**
 * @brief   The GobbledygookWorkhorseFragment class
 *          This class extends the WorkhorseFragment class to
 *          provide specific implementations of the
 *          retrieveIngredients method.
 */
public class GobbledygookWorkhorseFragment extends WorkhorseFragment {

    /**
     * @brief   
     * @return  {GobbledygookWorkhorseFragment} Returns an instance
     *          of the GobbledygookWorkhorseFragment class
     */
    static GobbledygookWorkhorseFragment newInstance(String url,
                                                     boolean showAsDialog) {
        GobbledygookWorkhorseFragment workhorse =
            new GobbledygookWorkhorseFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_URL, url);
        args.putBoolean(PARAM_DIALOG, showAsDialog);
        workhorse.setArguments(args);

        return workhorse;
    }

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Method to retrieve the ingredients for the recipe from the
     *          appropriate source, in this case the SharedPreferences in
     *          the system
     * @return  {Ingredients} Returns the populated ingredients object
     */
    protected Ingredients retrieveIngredients() {
        // Retrieve ingredients from shared preferences
        return this.retrieveIngredientsFromSharedPreferences();
    }

}
