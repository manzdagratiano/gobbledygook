/**
 * @file        Yggdrasil.java
 * @summary     Source file for the Yggdrasil class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        May 07, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.io.UnsupportedEncodingException;
import java.lang.Boolean;
import java.lang.RuntimeException;

// JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @summary     The Yggdrasil class.
 * @description This is the only "Activity" in the package.
 *              It implements a navigation drawer with a FrameLayout.
 *              All other "activities" in the app have been implemented
 *              as "Fragments"; navigating to a different activity within
 *              the app is equivalent to replacing the contents of
 *              the FrameLayout with the relevant fragment.
 *              This class extends "AppCompatActivity" from support-v7
 *              as opposed to "Activity", so that it may use the "Toolbar"
 *              class as the AppBar as opposed to the "ActionBar".
 *              This class is abstract so that it can provide
 *              a generic implementation for navigation drawer behavior
 *              without reference to specific items in the drawer.
 *              The appropriate subclasses will implement item-list-specific
 *              behavior.
 */
public abstract class Yggdrasil extends AppCompatActivity {

    // ====================================================================
    // PUBLIC METHODS

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // Create the navigation drawer
        this.createNavigationDrawer();

        // If this is the first launch, create a "Salt Key"
        this.checkAndCreateSaltKey();

        // Create a "PrefsHandler"
        m_prefsHandler = new PrefsHandler(this,
                                          getLogCategory());

        // Allow the application to accept cookies in WebViews.
        CookieManager.getInstance().setAcceptCookie(true);

        // Check how this activity was started -
        // from the launcher, or another application (a browser).
        Intent intent = this.getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && (null != type) &&
            MIMETYPE_TEXT.equals(type)) {
            // Launched from a browser.
            // Launch the "workhorse" fragment
            this.onActivitySelection(R.id.workhorseFragment,
                                     intent.getStringExtra(Intent.EXTRA_TEXT));
        } else {
            // Launched from the launcher.
            // Select the default state.
            // If there is no saved state, launch the "home" fragment.
            if (null == savedInstanceState) {
                this.onActivitySelection(R.id.drawerHome,
                                         null);
            }
            // else TODO
        }
    }

    /**
     * @summary ActionBarDrawerToggle must have its state synced in
     *          "onPostCreate"
     * @return  Does not return a value
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        final String FUNC = "onPostCreate()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring ActionBarDrawerToggle...");
        super.onPostCreate(savedInstanceState);
        // Sync the drawer toggle state after onRestoreInstanceState
        // has been called
        if (null != m_drawerToggle) {
            m_drawerToggle.syncState();
        }
    }

    /**
     * @summary ActionBarDrawerToggle must be passed
     *          any configuration changes
     * @return  Does not return a value
     */
    @Override
    public void onConfigurationChanged(Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        if (null != m_drawerToggle) {
            m_drawerToggle.onConfigurationChanged(newConfiguration);
        }
    }

    /**
     * @summary 
     * @return  Does not return a value
     */
    @Override
    public void setTitle(CharSequence title) {
        m_title = title;
        getSupportActionBar().setTitle(title);
    }

    /**
     * @summary Called after onCreate()
     * @return  Does not return a value
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * @summary Called after onCreate() (and onStart(),
     *          when the activity begins interacting with the user)
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * @summary 
     * @return  
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * @summary 
     * @return  
     */
    @Override
    public void onStop() {
        // Destroy all saved cookies.
        CookieManager.getInstance()
            .removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                final String FUNC =  "onReceiveValue()";
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "Cookies.Removed!");
            }
        });

        super.onStop();
    }

    // --------------------------------------------------------------------
    // ACTION BAR

    /**
     * @summary Function called whenever "invalidateOptionsMenu" is invoked
     * @return  Returns true on success and false on failure
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isDrawerOpen = m_drawerLayout.isDrawerOpen(m_drawerView);
        // Iterate over the items in the menu,
        // and hide them if the drawer is open.
        // This is irrespective of whether these items are in the drawer,
        // since at any point of time the user would either interact
        // with the drawer, or the app bar, but not both.
        // Note that this is independent of which fragment is supplying the
        // "support" Action Bar.
        // Hide the actions present in the navigation drawer
        // if the drawer is open
        for (int i = 0; i < menu.size(); ++i) {
            menu.getItem(i).setVisible(!isDrawerOpen);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * @summary Called when an action bar menu item is selected.
     * @return  True on success and false on failure.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The Action Bar home/up button should open/close the drawer;
        // ActionBarDrawerToggle handles that.
        if ((null != m_drawerToggle) &&
            (m_drawerToggle.onOptionsItemSelected(item))) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @summary Method to set the drawer toggler for this class.
     *          This is public to compensate for poor API design
     *          of the DrawerLayout class;
     *          even though the DrawerLayout can be accessed directly
     *          using findViewById() on a handle to this activity,
     *          it does not provide access to the listeners configured
     *          for itself, making dispatching certain lifecycle events
     *          for this activity to those listeners impossible if
     *          the listeners were created outside this activity -
     *          say in one of the attached fragments (this is necessary here
     *          since the ActionBarDrawerToggle, which will act as the
     *          listener for the DrawerLayout, depends on
     *          the fragment-specific toolbar).
     * @return  Does not even.
     */
    public void setDrawerToggler(ActionBarDrawerToggle drawerToggle) {
        m_drawerToggle = drawerToggle;
    }

    // --------------------------------------------------------------------
    // HANDLERS

    /**
     * @summary Called when a spawned activity returns.
     *          This call breaks encapsulation somewhat, since this method
     *          assumes it was spawned from and will be handled by a method
     *          of a data member. This is unavoidable, since this callback
     *          needs to reside in an activity, which the data member is not.
     * @return  Does not return a value.
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent resultData) {
        m_prefsHandler.onActivityResult(requestCode,
                                        resultCode,
                                        resultData);
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * @summary Called when a permission is requested.
     * @return  Does not even.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        m_prefsHandler.onRequestPermissionsResult(requestCode,
                                                  permissions,
                                                  grantResults);
        super.onRequestPermissionsResult(requestCode,
                                         permissions,
                                         grantResults);
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
        final String LOG_TAG = "MAIN";
        return LOG_TAG + "." + FUNC + ": ";
    }

    /**
     * @summary A function to launch the activity selected in the
     *          navigation drawer.
     *          This is done by replacing the framelayout with the
     *          appropriate fragment.
     *          This method may be overridden in the concrete derived classes.
     * @param   {int} itemId - The "R" ID of the selection. 
     * @param   {String} intentData - An optional string passed in
     *          from the intent.
     *          Will be null unless the intent is from another application.
     * @return  Does not return a value.
     */
    protected void onActivitySelection(final int itemId,
                                       final String intentData) {
        final String FUNC =  "onActivitySelection()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Selecting activity id=" + itemId);

        switch(itemId) {
            case R.id.drawerSettingsExport:
                // Export settings
                m_prefsHandler.exportSettings();
                break;
            case R.id.drawerSettingsImport:
                // Import settings
                m_prefsHandler.importSettings();
                break;
            case R.id.drawerHelp:
                // Open the help page
                startActivity(new Intent(Intent.ACTION_VIEW,
                                   Uri.parse(getString(
                                        R.string.about_help_page))));
                break;
            default:
                // Do nothing
        }
    }

    /**
     * @summary Function to swap in a new fragment into
     *          the frameLayout of the Navigation Drawer
     * @return  Does not return a value
     */
    protected void swapFragment(Fragment fragment,
                                final String fragmentTag) {
        assert (null != fragment) : "Asked to swap in null fragment!!!";
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTx = fragmentManager.beginTransaction();
        Fragment prevInstance = fragmentManager.findFragmentByTag(fragmentTag);
        if (null != prevInstance) {
            fragmentTx.remove(prevInstance);
        }
        // Only add to back stack if there are existing fragments,
        // i.e., don't add the first (home) fragment to the back stack,
        // else the back button will result in an empty activity.
        if (null != fragmentManager.getFragments()) {
            fragmentTx.addToBackStack(null);
        }

        fragmentTx.replace(R.id.contentFrame,
                           fragment,
                           fragmentTag)
                  .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                  .commit();
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String MIMETYPE_TEXT                           =
        "text/plain";

    // Toast Messages
    private static final String INIT_MESSAGE                            =
        "Initializing...";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @summary Method to configure the navigation drawer.
     * @return  Does not even.
     */
    private void createNavigationDrawer() {
        final String FUNC = "createNavigationDrawer()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring the navigation drawer...");

        m_drawerLayout =
            (DrawerLayout)findViewById(R.id.navigationDrawerLayout);
        m_drawerView =
            (NavigationView)m_drawerLayout.findViewById(R.id.navigationDrawer);
        m_drawerView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean
                    onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        onActivitySelection(menuItem.getItemId(),
                                            null);
                        return true;
                    }
                });

        // Each fragment will create its own ActionBarDrawerToggle action
        // to act as the DrawerListener for this navigation drawer,
        // since each fragment owns its own toolbar.
        m_drawerToggle = null;

        // The navigation drawer header.
        View drawerHeaderView = m_drawerView.getHeaderView(0);
        TextView drawerHeader = (TextView)drawerHeaderView.findViewById(
                R.id.drawerHeaderText);
        drawerHeader.setText(R.string.drawer_header_text,
                             TextView.BufferType.NORMAL);
        drawerHeader.setTypeface(null, Typeface.BOLD);
    }

    /**
     * @summary Method to check if a salt key exists, and if not,
     *          to create a new one and save it to SharedPreferences.
     * @return  Does not return a value
     */
    private void checkAndCreateSaltKey() {
        final String FUNC = "checkAndCreateSaltKey()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Checking for salt key...");

        SharedPreferences sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(
                                    this.getApplicationContext());
        String saltKey = "";
        // Catch all exceptions when reading from SharedPreferences
        try {
            saltKey = sharedPrefs.getString(
                    getString(R.string.pref_saltKey_key),
                              "");
        } catch (Exception e) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "ERROR: Caught " + e);
            e.printStackTrace();
        }

        if (saltKey.isEmpty()) {
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                    "saltKey=null, Creating new key...");
            // Since this may take a few seconds,
            // inform the user
            Toast.makeText(this.getApplicationContext(),
                           INIT_MESSAGE,
                           Toast.LENGTH_SHORT).show();

            saltKey = null;
            try {
                saltKey = Crypto.generateSaltKey();
            } catch (UnsupportedEncodingException e) {
                Log.e(getLogCategory(), getLogPrefix(FUNC) +
                      "ERROR: Caught " + e);
                e.printStackTrace();
                // We cannot proceed.
                throw new RuntimeException("SaltKey.Generation.Failure");
            }
            Log.i(getLogCategory(), getLogPrefix(FUNC) +
                  "Generated saltKey='" + saltKey + "'");

            SharedPreferences.Editor preferenceEditor = sharedPrefs.edit();
            preferenceEditor.putString(getString(R.string.pref_saltKey_key),
                                       saltKey);
            preferenceEditor.apply();
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS

    protected DrawerLayout          m_drawerLayout; /**
                                                      * @brief The drawer
                                                      * layout
                                                      */
    protected NavigationView        m_drawerView;   /**
                                                      * @brief The drawer
                                                      * list view
                                                      */
    protected ActionBarDrawerToggle m_drawerToggle; /**
                                                      * @brief The toggle
                                                      * action for the
                                                      * drawer
                                                      */
    protected CharSequence          m_title;        /**
                                                      * @brief The activity
                                                      * title
                                                      */
    protected PrefsHandler          m_prefsHandler; /**
                                                      * @brief A handle to
                                                      * the PrefsHandler.
                                                      */

}
