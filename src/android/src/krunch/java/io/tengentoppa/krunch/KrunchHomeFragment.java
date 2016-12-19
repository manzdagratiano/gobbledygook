/**
 * @file        KrunchHomeFragment.java
 * @summary     Source file for the KrunchHomeFragment class
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
import io.tengentoppa.yggdrasil.R;

// Android
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

// Standard Java
import java.lang.RuntimeException;

/**
 * @summary 
 */
public class KrunchHomeFragment extends HomeFragment {

    // ====================================================================
    // PROTECTED METHODS

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
                showWorkhorseDialog();
            }
        });
    }

    /**
     * @summary Method to return an instance of
     *          the concrete implementation of the WorkhorseFragment.
     * @return  {DialogFragment} An instance of KrunchWorkhorseFragment
     */
    protected DialogFragment
    getWorkhorseFragment(final String url,
                         final boolean showAsDialog) {
        // Instantiate the fragment
        return KrunchWorkhorseFragment.newInstance(url,
                                                   showAsDialog);
    }

}
