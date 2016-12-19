/**
 * @file        KrunchPrefsFragment.java
 * @summary     Source file for the KrunchPrefsFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Apr 05, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.krunch;

// Libraries
import io.tengentoppa.yggdrasil.R;
import io.tengentoppa.yggdrasil.PrefsFragment;

// Android
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

// =======================================================================

/**
 * @summary 
 */
public class KrunchPrefsFragment extends PrefsFragment {

    // --------------------------------------------------------------------
    // HANDLERS

    /**
     * @summary Handler called when any of the SharedPreference elements
     *          are changed.
     * @return  Does not return a value.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        final String FUNC = "onSharedPreferencesChanged()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "key='" + key + "'");

        // Call the method from the super class to handle other keys
        super.onSharedPreferenceChanged(sharedPreferences,
                                        key);
    }

    // ====================================================================
    // PRIVATE MEMBERS

    // --------------------------------------------------------------------
    // INHERITED METHODS

    /**
     * @summary Method to return the log category.
     * @return  {String} The log category
     */
    @Override
    protected String getLogCategory() {
        return Logger.getCategory();
    }

    /**
     * @summary Overridden method from the superclass
     *          to configure all the elements of the list.
     * @return  Does not even.
     */
    @Override
    protected void configurePreferenceElements() {
        // Call the method from the superclass
        // to configure all the other elements.
        super.configurePreferenceElements();
    }

    /**
     * @summary Method to return an instance of the specific implementation
     *          of the SaltKeyActionsFragment class.
     * @return  {DialogFragment} An instance of
     *          the KrunchSaltKeyActionsFragment class.
     */
    @Override
    protected DialogFragment
    getSaltKeyActionsFragment(final boolean showAsDialog) {
        return KrunchSaltKeyActionsFragment.newInstance(showAsDialog);
    }

    /**
     * @summary Method to perform any cleanup, such as freeing handlers
     *          for garbage collection
     * @return  Does not return a value
     */
    @Override
    protected void deconfigurePreferenceElements() {
        // Call the method from the base class.
        super.deconfigurePreferenceElements();
    }

}
