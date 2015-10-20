/**
 * @file        KrunchHomeFragment.java
 * @brief       Source file for the KrunchHomeFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.krunch;

// Libraries
import io.tengentoppa.yggdrasil.HomeFragment;

// Android


/**
 * @brief   The KrunchHomeFragment class
 *          This class extends the HomeFragment class to
 *          provide specific implementations of the
 *          retrieveIngredients method.
 */
public class KrunchHomeFragment extends HomeFragment {

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Method to retrieve the ingredients for the recipe from the
     *          appropriate source, in this case the SharedPreferences in
     *          the system.
     * @return  {Ingredients} Returns the populated ingredients object.
     */
    protected Ingredients retrieveIngredients() {
        // Allocate memory
        Ingredients ingredients = new Ingredients();

        // Retrieve ingredients from shared preferences
        this.retrieveIngredientsFromServer(ingredients);

        return ingredients;
    }

    /**
     * @brief   Method to retrieve the ingredients from the server.
     * @return  Does not return a value, but does populates the ingredients.
     */
    private void retrieveIngredientsFromServer(Ingredients ingredients) {
    }

}
