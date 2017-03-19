/**
 * @file        HomeFragment.java
 * @summary     Source file for the HomeFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        June 20, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnScrollChangeListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @summary The HomeFragment class
 *          This class is abstract since it needs to display
 *          the appropriate flavor of the "Workhorse" fragment
 *          in the dialog upon pressing the FloatingActionButton.
 *          The child flavor classes will do that implementation.
 */
public abstract class HomeFragment extends Fragment {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @summary Called when the fragment is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String FUNC = "onCreate()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating home activity...");
        super.onCreate(savedInstanceState);

        // Indicate that this fragment would like to
        // contribute to the options menu
        // (i.e., receive the onCreateOptionsMenu() call).
        this.setHasOptionsMenu(true);

        // Nullify the private data members
        this.m_searchView = null;
        this.m_webView = null;
        this.m_floatingActionButton = null;
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
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Inflating View...");

        // Inflate the view.
        View view = inflater.inflate(R.layout.home_fragment,
                                     container,
                                     false);

        // Obtain handles to the view elements.
        m_webView =
            (NestedWebView)view.findViewById(R.id.homeWebView);
        m_floatingActionButton =
            (FloatingActionButton)view.findViewById(R.id.floatingButton);

        // Configure the Navigation Drawer toggle behavior.
        NavigationDrawerToggler.configureToggler(
                (AppCompatActivity)this.getActivity(),
                (Toolbar)view.findViewById(R.id.homeAppBar));

        // Configure elements here,
        // as opposed to onStart()/onResume(), since we do not want
        // activity pauses to reload the WebView etc.
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Configuring elements...");
        this.configureElements();

        return view;
    }

    /**
     * @summary Called after onCreate().
     * @return  Does not return a value.
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * @summary Called after onStart(),
     *          when the activity begins interacting with the user.
     * @return  Does not return a value.
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * @summary Called when the activity is partially covered by another.
     * @return  Does not return a value.
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * @summary Called when the activity is stopped.
     * @return  Does not return a value.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * @summary Called when the fragment is being destroyed.
     *          Perform any cleanup here - symmetric to onCreate().
     * @return  Does not return a value.
     */
    public void onDestroy() {
        // Perform any cleanup here
        final String FUNC = "onDestroy()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Deconfiguring elements...");

        this.deconfigureElements();

        super.onDestroy();
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
        inflater.inflate(R.menu.app_bar_home,
                         menu);
        // Not checking for malformed menu,
        // which will and should crash the app.
        m_searchView =
            (SearchView)(menu.findItem(R.id.homeSearchView).getActionView());
        this.configureSearchView();

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
            case R.id.homeSearchView:
                // No implementation here,
                // since the OnQueryTextListener has been attached
                // to the SearchView.
                return false;
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
        final String LOG_TAG = "HOMEFRAGMENT";
        return LOG_TAG + "." + FUNC + ": ";
    }

    /**
     * @summary Method to configure the floating action button.
     *          Suitably overridden in the concrete implementations.
     *          NOTE: Allowing drag and drop for the floating action button
     *          is a recipe for a host of unanticipated UI bugs.
     *          The only "hiding" of the button will be on scrolling in the
     *          WebView using the tricks of the CoordinatorLayout.
     * @return  Does not return a value.
     */
    protected abstract void configureFloatingActionButton();

    /**
     * @summary A method to show the concrete implementation
     *          of the WorkhorseFragment dialog.
     * @return  {DialogFragment} The concrete implementation instance.
     */
    protected abstract DialogFragment
    getWorkhorseFragment(final String url,
                         final boolean showAsDialog);

    /**
     * @summary A method to show the "Workhorse" dialog.
     * @return  Does not even.
     */
    protected void showWorkhorseDialog() {
        final String FUNC = "showWorkhorseDialog()";
        Log.i(getLogCategory(), getLogPrefix(FUNC) +
              "Creating Workhorse dialog...");

        // Instantiate the WorkhorseFragment;
        // pass the current WebView url as input to the fragment.
        boolean showAsDialog = true;
        DialogFragment workhorseDialog =
            getWorkhorseFragment(m_webView.getUrl(),
                                 showAsDialog);

        final String fragmentTag = getString(R.string.tag_workhorseFragment);
        FragmentManager fragmentManager =
            getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTx = fragmentManager.beginTransaction();
        Fragment prevInstance = fragmentManager.findFragmentByTag(fragmentTag);
        if (null != prevInstance) {
            fragmentTx.remove(prevInstance);
        }
        fragmentTx.addToBackStack(null);

        // Call "show" on the DialogFragment with a FragmentTransaction.
        // "show" will commit the transaction as well
        workhorseDialog.show(fragmentTx,
                             fragmentTag);
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @summary Method to configure the view elements.
     * @return  Does not even.
     */
    private void configureElements() {
        // Configure the WebView with a "safe" URL to start with
        this.configureWebView();

        // Configure the Floating Action Button
        this.configureFloatingActionButton();
    }

    /**
     * @summary Method to configure the WebView.
     * @return  Does not return a value.
     */
    private void configureWebView() {
        // Enable built-in zoom controls
        // (needs to have height/width set to MATCH_PARENT,
        // not WRAP_CONTEXT).
        m_webView.getSettings().setBuiltInZoomControls(true);
        // However, hide the on-screen zoom control display,
        // while still allowing pinch to zoom.
        m_webView.getSettings().setDisplayZoomControls(false);

        // Enable JavaScript by default,
        // without which most websites will break anyway.
        // Food for thought: if we're concerned about malicious javascript,
        // should we even be logging in (which is what this is for)
        // to websites we cannot trust?
        m_webView.getSettings().setJavaScriptEnabled(true);

        // Load all links internally
        m_webView.setWebViewClient(new WebViewClient() {
            /**
             * @summary Method to handle specific errors while loading
             *          the requested URL.
             *          Since the URL is guessed from the query, we may
             *          end up in a situation where the URL is not valid
             *          and fails to load. In such a case, we will attempt
             *          to turn to Google instead.
             *          It is theoretically possible that this becomes a
             *          feedback loop when loading the same URL again
             *          with the same error; however, given that the reload
             *          attempt performs a Google search, which URL is known
             *          in advance to be valid, this cannot happen.
             * @return  Does not even.
             */
            @Override
            public void onReceivedError(WebView webView,
                                        WebResourceRequest request,
                                        WebResourceError error) {
                final String FUNC = "onReceivedError()";
                Log.i(getLogCategory(), getLogPrefix(FUNC) +
                      "error=" + error.getDescription());
                switch(error.getErrorCode()) {
                    case WebViewClient.ERROR_BAD_URL:
                        // Load a Google search with the bad URL.
                        webView.loadUrl(WebHelper.getGoogleSearchUrl(
                                            request.getUrl().toString()));
                        break;
                    default:
                        // Do nothing
                        ;
                }
            }
        });
        m_webView.setWebChromeClient(new WebChromeClient());

        // No need to accept third-party cookies.
        // (Accepting cookies is already enabled).
        CookieManager.getInstance()
            .setAcceptThirdPartyCookies(m_webView,
                                        false);

        // Provide "back" navigation in the webview
        m_webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode &&
                    m_webView.canGoBack()) {
                    m_webView.goBack();
                    return true;
                }
                // There is no "super" method, since it is abstract.
                return false;
            }
        });

        // Set an onScrollChangeListener for the WebView
        // to manipulate the FloatingActionButton.
        // This is being done in lieu of a FloatingActionButton.Behavior
        // implementation, since the "onNestedScroll" method of that behavior
        // does not seem to be triggered with a custom NestedScrollingChild
        // implementation in the NestedWebView, even when
        // "onStartNestedScroll" returns true.
        m_webView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            /**
             * @summary Hides the FloatingActionButton if we're scrolling down
             *          (content scrolls up), else shows it.
             *          No need to make this logic overly complicated,
             *          since that may impact performance (e.g., make the scroll
             *          jittery) without any perceivable gain.
             * @return  Does not even.
             */
            @Override
            public void onScrollChange(View view,
                                       int scrollX,
                                       int scrollY,
                                       int oldScrollX,
                                       int oldScrollY) {
                if ((scrollY > oldScrollY) && (oldScrollY > 0)) {
                    m_floatingActionButton.hide();
                } else {
                    m_floatingActionButton.show();
                }
            }
        });

        // Load the WebView with a "safe" URL to start with
        this.loadWebView(getString(R.string.default_url));
    }

    /**
     * @summary Method to configure the search bar at the top.
     * @return  Does not return a value
     */
    private void configureSearchView() {
        // We do not need to associate the SearchView with
        // a Search Interface, since we will directly use the text
        // to load a URL.

        m_searchView.setQueryHint(getString(R.string.search_hint));

        m_searchView
            .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String query) {
                    // Do nothing.
                    // Handled by the listener, so return true.
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    final String FUNC = "onQueryTextSubmit()";

                    String url = WebHelper.getUrl(query);
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
     * @summary Method to load the WebView with a URL.
     * @return  Does not even.
     */
    private void loadWebView(String url) {
        m_webView.loadUrl(url);
        // Request focus away from the SearchView
        m_webView.requestFocus();
        // Hide the soft keyboard if it is visible
        InputMethodManager inputMethodManager =
            (InputMethodManager)getActivity().getSystemService(
                    getActivity()
                        .getApplicationContext().INPUT_METHOD_SERVICE);
        inputMethodManager
            .hideSoftInputFromWindow(m_webView.getWindowToken(),
                                     0);
    }

    /**
     * @summary A method to perform any cleanup (listeners etc).
     * @return  Does not return a value
     */
    protected void deconfigureElements() {
        m_searchView.setOnQueryTextListener(null);
        m_floatingActionButton.setOnClickListener(null);
        m_webView.setOnKeyListener(null);
        m_webView.setOnScrollChangeListener(null);
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS

    /**
     * @summary The FloatingActionButton.
     *          This is an inheritable data member,
     *          to which different actions will be assigned
     *          in the concrete implementations.
     */
    protected FloatingActionButton  m_floatingActionButton;

    /**
     * @summary The search bar at the top of the fragment.
     */
    private SearchView              m_searchView;

    /**
     * @summary The WebView to display.
     *          This is an instance of our special "NestedWebView".
     */
    private NestedWebView           m_webView;

}
