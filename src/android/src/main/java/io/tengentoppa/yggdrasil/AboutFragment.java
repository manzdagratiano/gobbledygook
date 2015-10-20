/**
 * @file        AboutFragment.java
 * @brief       Source file for the AboutFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 23, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

// Standard Java
import java.util.ArrayList;

/**
 * @brief   The AboutFragment class
 */
public class AboutFragment extends Fragment {

    // ====================================================================
    // PUBLIC METHODS

    /**
     * @brief   Called when the fragment is created.
     * @return  Does not return a value
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_CATEGORY, "onCreate(): Creating about activity...");
        super.onCreate(savedInstanceState);
    }

    /**
     * @brief   Called when the fragment is ready to display its UI
     * @return  The View representing the root of the fragment layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the main layout
        return inflater.inflate(R.layout.about,
                                container,
                                false);
    }

    /**
     * @brief   Called after onCreate()
     * @return  Does not return a value
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * @brief   Called after onCreate() (and onStart(),
     *          when the activity begins interacting with the user)
     * @return  Does not return a value
     */
    @Override
    public void onResume() {
        Log.i(LOG_CATEGORY, "onResume(): Configuring elements...");

        super.onResume();

        // Configure elements
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
        Log.i(LOG_CATEGORY, "onPause(): Deconfiguring elements...");

        this.deconfigureElements();

        super.onPause();
    }

    /**
     * @brief   
     * @return  
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY            = "GOBBLEDYGOOK";

    // --------------------------------------------------------------------
    // METHODS

    /**
     * @brief   
     * @return  Does not return a value
     */
    private void configureElements() {

        // Create the list of items
        this.createAboutItemList();

        m_title = getActivity().getTitle();
        m_aboutLayout = (ListView)getView().findViewById(R.id.aboutView);
        m_aboutLayout.setAdapter(new AboutListAdapter(
                                    getActivity().getApplicationContext(),
                                    m_aboutItemList));

        // Create the item click listener
    }

    /**
     * @brief   
     * @return  Does not return a value
     */
    private void createAboutItemList() {
        m_aboutItemList = new ArrayList<AboutItem>();

        m_aboutItemList.add(new AboutItem(
                    R.drawable.ic_action_person,
                    "Creator",
                    "Manjul Apratim"));
        m_aboutItemList.add(new AboutItem(
                    R.drawable.ic_action_web_site,
                    "Homepage",
                    "https://github.com/manzdagratiano"));
        m_aboutItemList.add(new AboutItem(
                    R.drawable.ic_menu_help,
                    "Help",
                    "First, you need the 'salt'..."));
    }

    /**
     * @brief   
     * @return  Does not return a value
     */
    private void deconfigureElements() {
    }

    // --------------------------------------------------------------------
    // PRIVATE DATA

    private ListView                m_aboutLayout;      /** @brief The layout
                                                          */
    private CharSequence            m_title;            /** @brief The title of
                                                          * the fragment
                                                          */
    private ArrayList<AboutItem>    m_aboutItemList;    /** @brief An array of
                                                          * the items in the
                                                          * fragment
                                                          */
}
