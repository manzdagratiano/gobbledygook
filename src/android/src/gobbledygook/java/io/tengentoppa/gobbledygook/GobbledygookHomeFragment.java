/**
 * @file        GobbledygookHomeFragment.java
 * @summary     Source file for the GobbledygookHomeFragment class
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * @summary 
 */
public class GobbledygookHomeFragment extends HomeFragment {

    /**
     * @summary Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

    /**
     * @summary Method to configure the floating action button.
     * @return  Does not return a value.
     */
    @Override
    protected void configureFloatingActionButton() {
        m_floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String FUNC = "OnClickListener.onClick()";
                // "Gobbledygook" is the no-frills version
                // for the truly paranoid.
                // Show the vanilla "Workhorse" dialog,
                // where the user enters her password.
                showWorkhorseDialog();
            }
        });
    }

    /**
     * @summary Method to return an instance of
     *          the concrete implementation of the WorkhorseFragment.
     * @return  {DialogFragment} An instance of GobbledygookWorkhorseFragment
     */
    @Override
    protected DialogFragment
    getWorkhorseFragment(final String url,
                         final boolean showAsDialog) {
        // Instantiate the fragment
        return GobbledygookWorkhorseFragment.newInstance(url,
                                                         showAsDialog);
    }

}
