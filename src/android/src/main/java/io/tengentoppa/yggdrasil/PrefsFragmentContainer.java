/**
 * @file        PrefsFragmentContainer.java
 * @summary     Source file for the PrefsFragmentContainer class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 23, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * @summary The PrefsFragmentContainer class.
 */
public abstract class PrefsFragmentContainer extends Fragment {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @summary Called when the fragment is created.
     * @return  Does not return a value.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String FUNC = "onCreate()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating PrefsFragment...");

        super.onCreate(savedInstanceState);

        // Indicate this fragment has an options menu.
        this.setHasOptionsMenu(true);

        // Create a "PrefsHandler"
        m_prefsHandler = new PrefsHandler((AppCompatActivity)getActivity(),
                                          getLogCategory());
    }

    /**
     * @summary Called when the fragment is ready to display its UI
     * @return  The View representing the root of the fragment layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final String FUNC = "onCreateView()";

        // Inflate the view.
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Inflating container...");
        View view = inflater.inflate(R.layout.prefs_fragment_container,
                                     container,
                                     false);

        // Configure the Navigation Drawer toggle behavior.
        NavigationDrawerToggler.configureToggler(
                (AppCompatActivity)this.getActivity(),
                (Toolbar)view.findViewById(R.id.prefsAppBar));

        return view;
    }

    /**
     * @summary Called after onCreateView() to do final initializations.
     *          This is where the child fragment should be inflated.
     * @return  Does not return a value.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final String FUNC = "onActivityCreated()";

        super.onActivityCreated(savedInstanceState);

        // Load the preference fragment into the FrameLayout.
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Inflating Prefs fragment...");
        this.getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.prefsContentFragment,
                     getPrefsFragment(),
                     getString(R.string.tag_prefsFragment))
            .commit();
    }

    // --------------------------------------------------------------------
    // ACTION BAR

    /**
     * @summary Called to populate the action bar menu, if it is present
     * @return  Returns true on success and false on failure
     */
    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.app_bar_prefs,
                         menu);
        super.onCreateOptionsMenu(menu,
                                  inflater);
    }

    /**
     * @summary Called when an action bar menu item is selected.
     * @return  True on success and false on failure.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.actionExportSettings:
                m_prefsHandler.exportSettings();
                return true;
            case R.id.actionImportSettings:
                m_prefsHandler.importSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ====================================================================
    // PROTECTED METHODS

    /**
     * @summary An method to obtain the log category,
     *          suitably overridden in the concrete implementation.
     * @return  {String} The log category.
     */
    protected abstract String getLogCategory();

    /**
     * @summary A method to get a prefix for the log.
     * @return  {String} The log prefix
     */
    protected String getLogPrefix(String FUNC) {
        final String LOG_TAG = "PREFSCONTAINER";
        return LOG_TAG + "." + FUNC + ": ";
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @summary Method to obtain an instance of the "Prefs" fragment.
     *          Suitably overridden in the concrete implementation.
     * @return  Does not return a value
     */
    protected abstract PreferenceFragmentCompat getPrefsFragment();

    // --------------------------------------------------------------------
    // DATA MEMBERS

    /**
     * @brief A handle to the PrefsHandler class.
     */
    private PrefsHandler        m_prefsHandler;

}

