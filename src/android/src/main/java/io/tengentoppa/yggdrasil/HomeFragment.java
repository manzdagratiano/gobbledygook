/**
 * @file        HomeFragment.java
 * @brief       Source file for the HomeFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;

/**
 * @brief   The HomeFragment class
 *          This class is abstract since it needs to display
 *          the appropriate flavor of the "Workhorse" fragment
 *          in the dialog upon pressing the FloatingActionButton.
 *          The child flavor classes will do that implementation.
 */
public abstract class HomeFragment extends Fragment {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   Called when the fragment is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String FUNC = "onCreate()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating home activity...");
        super.onCreate(savedInstanceState);

        // Nullify the private data members
        this.m_searchView = null;
        this.m_webView = null;
        this.m_floatingButton = null;
    }

    /**
     * @brief   Called when the fragment is ready to display its UI
     * @return  The View representing the root of the fragment layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home,
                                     container,
                                     false);

        m_searchView =
            (SearchView)view.findViewById(R.id.homeSearchView);
        m_webView =
            (WebView)view.findViewById(R.id.homeWebView);
        m_floatingButton =
            (FloatingActionButton)view.findViewById(R.id.floatingButton);

        return view;
    }

    /**
     * @brief   Called after onCreate() (and onStart(),
     *          when the activity begins interacting with the user)
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        final String FUNC = "onResume()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring elements...");

        super.onResume();

        // Configure elements here
        this.configureElements();
    }

    /**
     * @brief   Called when the activity is partially covered by another.
     *          Perform any cleanup here - symmetric to onResume
     * @return  Does not return a value
     */
    @Override
    public void onPause() {
        // Perform any cleanup here
        final String FUNC = "onPause()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Deconfiguring elements...");

        this.deconfigureElements();

        super.onPause();
    }

    // ====================================================================
    // PROTECTED METHODS

    /**
     * @brief   An method to obtain the log category,
     *          suitably overridden in the concrete implementation.
     * @return  {String} The log category.
     */
    protected abstract String getLogCategory();

    /**
     * @brief   A method to get a prefix for the log.
     * @return  {String} The log prefix
     */
    protected String getLogPrefix(String FUNC) {
        final String LOG_TAG = "HOMEFRAGMENT";
        return LOG_TAG + "." + FUNC + ": ";
    }

    /**
     * @brief   Method to configure the floating action button.
     *          Suitably overridden in the concrete implementations.
     * @return  Does not return a value
     */
    protected abstract void configureFloatingActionButton();

    /**
     * @brief   A method to show the concrete implementation
     *          of the WorkhorseFragment dialog.
     * @return  {DialogFragment} The concrete implementation instance.
     */
    protected abstract DialogFragment
    getWorkhorseFragment(final String url,
                         final boolean showAsDialog);

    /**
     * @brief   A method to show the "Workhorse" dialog.
     * @return  Does not even.
     */
    protected void showWorkhorseDialog() {
        final String FUNC = "showWorkhorseDialog()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating Workhorse dialog...");
        // Show the WorkhorseFragment as a Dialog
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        FragmentTransaction fragmentTx = fragmentManager.beginTransaction();
        Fragment prevInstance = fragmentManager.findFragmentByTag(
                getString(R.string.tag_workhorseFragment));
        if (null != prevInstance) {
            fragmentTx.remove(prevInstance);
        }
        // Provide proper "back" navigation
        fragmentTx.addToBackStack(null);

        // Obtain the current WebView url to pass as input
        // to the WorkhorseFragment dialog
        String url = m_webView.getUrl();

        // Instantiate the fragment
        boolean showAsDialog = true;
        DialogFragment workhorseDialog = getWorkhorseFragment(url,
                                                              showAsDialog);

        // "show" will commit the transaction as well
        workhorseDialog.show(fragmentTx,
                             getString(R.string.tag_workhorseFragment));
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   Method to configure the view elements.
     * @return  Does not even.
     */
    private void configureElements() {
        // Configure the WebView with a "safe" URL to start with
        this.configureWebView();

        // Configure the search bar
        this.configureSearchView();

        // Configure the Floating Action Button
        this.configureFloatingActionButton();
    }

    /**
     * @brief   Method to configure the WebView.
     * @return  Does not return a value.
     */
    private void configureWebView() {

        // Enable JavaScript by default,
        // without which most websites will break anyway.
        // Food for thought: if we're concerned about malicious javascript,
        // should we even be logging in (which is what this is for)
        // to websites we cannot trust?
        m_webView.getSettings().setJavaScriptEnabled(true);

        // Load all links internally
        m_webView.setWebViewClient(new WebViewClient());

        // Set the WebView to accept cookies
        // (since we're logging into places).
        // TODO: discard them when the application exits
        CookieManager.getInstance().setAcceptCookie(true);

        // Load the WebView with a "safe" URL to start with
        this.loadWebView(getString(R.string.default_url));
    }

    /**
     * @brief   Method to configure the search bar at the top.
     * @return  Does not return a value
     */
    private void configureSearchView() {
        m_searchView
            .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String query) {
                    // Do nothing
                    // Handled by the listener, so return true
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    final String FUNC = "onQueryTextSubmit()";

                    String url = WebViewHelper.getUrlFromQuery(query);
                    Log.i(getLogCategory(), getLogPrefix(FUNC) +
                          "query='" + query + "', url='" + url + "'");
                    // Update the WebView with the query as the URL
                    // (we have access to the methods of the enclosing class)
                    loadWebView(url);
                    // Handled by the listener, so return true
                    return true;
                }
            });
    }

    /**
     * @brief   Method to load the WebView with a URL.
     * @return  Does not even.
     */
    private void loadWebView(String url) {
        m_webView.loadUrl(url);
        // Request focus away from the SearchView
        m_webView.requestFocus();
        // Hide the soft keyboard if it is visible
        InputMethodManager inputMethodManager =
            (InputMethodManager)getActivity().getSystemService(
                    Activity.INPUT_METHOD_SERVICE);
        inputMethodManager
            .hideSoftInputFromWindow(m_webView.getWindowToken(),
                                     0);
    }

    /**
     * @brief   A method to perform any cleanup (listeners etc).
     * @return  Does not return a value
     */
    protected void deconfigureElements() {
        m_searchView.setOnQueryTextListener(null);
        m_floatingButton.setOnClickListener(null);
    }

    // --------------------------------------------------------------------
    // DATA

    /**
     * @brief   The FloatingActionButton.
     *          This is an inheritable data member,
     *          to which different actions will be assigned
     *          in the concrete implementations.
     */
    protected FloatingActionButton  m_floatingButton;

    /**
     * @brief   The search bar at the top of the fragment.
     */
    private SearchView              m_searchView;

    /**
     * @brief   The WebView to display.
     */
    private WebView                 m_webView;

    // --------------------------------------------------------------------
    // NESTED CLASSES

    /**
     * @brief   A utility class to encapsulate methods for manipulating
     *          the Webview.
     *          For some reason, Android freaks out if this class is
     *          declared private, with a static public method.
     *          "Uncaught translation error:
     *           com.android.dex.util.ExceptionWithContext"
     */
    public static class WebViewHelper {

        /**
         * @brief   
         * @return  
         */
        public static String getUrlFromQuery(String query) {
            if (query.startsWith("https://")) {
                return query;
            } else if (query.startsWith("http://")) {
                // Force https://
                // Websites that do not support https and require a login
                // are beneath us and not supported
                return query.replace("http://", "https://");
            } else {
                // Will be optimized to StringBuilder
                return "https://" + query;
            }
        }

    }   // end class WebViewHelper

}
