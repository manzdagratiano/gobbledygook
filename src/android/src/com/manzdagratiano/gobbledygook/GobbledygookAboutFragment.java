/**
 * @file        GobbledygookAboutFragment.java
 * @brief       Source file for the GobbledygookAboutFragment class
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 23, 2015
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package com.manzdagratiano.gobbledygook;

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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// Standard Java
import java.util.ArrayList;

/**
 * @brief   The GobbledygookAboutFragment class
 */
public class GobbledygookAboutFragment extends Fragment {

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
    // INNER CLASSES

    /**
     * @brief   The AboutItem class
     *          Provides a model for each item in the "About" page
     */
    private class AboutItem {

        // ----------------------------------------------------------------
        // CREATORS

        public AboutItem(int iconId,
                         String title,
                         String text) {
            this.m_iconId = iconId;
            this.m_title = title;
            this.m_text = text;
        }

        // ----------------------------------------------------------------
        // PUBLIC METHODS

        /**
         * @brief   
         * @return  
         */
        public int getIconId() {
            return m_iconId;
        }

        /**
         * @brief   
         * @return  
         */
        public String getTitle() {
            return m_title;
        }

        /**
         * @brief   
         * @return  
         */
        public String getText() {
            return m_text;
        }

        // ----------------------------------------------------------------
        // PRIVATE DATA

        private int     m_iconId;   /** @brief The item ID of the icon
                                      * associated with
                                      * the element in the "R" class
                                      */
        private String  m_title;    /** @brief The title of the item
                                      */
        private String  m_text;     /** @brief The description or value of
                                      * the item
                                      */
    }

    /**
     * @brief   The AboutListAdapter class
     *          Implements a custom adapter for the "About" fragment by
     *          extending the BaseAdapter class.
     */
    private class AboutListAdapter extends BaseAdapter {

        // ----------------------------------------------------------------
        // CREATORS

        public AboutListAdapter(Context context,
                                ArrayList<AboutItem> aboutItemList) {
            this.m_context = context;
            this.m_aboutItemList = aboutItemList;
        }

        // ----------------------------------------------------------------
        // PUBLIC METHODS

        /**
         * @brief   
         * @return  
         */
        @Override
        public int getCount() {
            return m_aboutItemList.size();
        }

        /**
         * @brief   Method to return a reference to the object at
         *          a specific position.
         *          This would need to be typecast to the approriate type
         *          to use that object's methods.
         * @param   The position to look up
         * @return  A reference to the object at position "position"
         */
        @Override
        public Object getItem(int position) {
            return m_aboutItemList.get(position);
        }

        /**
         * @brief   
         * @return  
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * @brief   
         * @return  
         */
        @Override
        public View getView(int position,
                            View view,
                            ViewGroup parentViewGroup) {
            if (null == view) {
                LayoutInflater inflater =
                    (LayoutInflater)m_context.getSystemService(
                            Activity.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.about_item,
                                        null);
            }

            ImageView itemIcon =
                (ImageView)view.findViewById(R.id.aboutItemIcon);
            TextView itemTitle =
                (TextView)view.findViewById(R.id.aboutItemTitle);
            TextView itemText =
                (TextView)view.findViewById(R.id.aboutItemText);

            // Obtain a reference to the AboutItem
            // at position "position", and use it to set resources.
            AboutItem aboutItem =
                (AboutItem)this.getItem(position);
            itemIcon.setImageResource(aboutItem.getIconId());
            itemTitle.setText(aboutItem.getTitle());
            itemText.setText(aboutItem.getText());

            return view;
        }

        // ----------------------------------------------------------------
        // PRIVATE DATA

        private Context                 m_context;          /** @brief
                                                              * A reference
                                                              * to the Context
                                                              * associated
                                                              * with
                                                              * the activity
                                                              */

        private ArrayList<AboutItem>    m_aboutItemList;    /** @brief
                                                              * A reference
                                                              * to the list
                                                              * of items in
                                                              * the view
                                                              */
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
