package com.manzdagratiano.yggdrasil;

// Android
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

// Standard Java
import java.util.ArrayList;

/**
 * @brief   The AboutListAdapter class
 *          Implements a custom adapter for the "About" fragment by
 *          extending the BaseAdapter class.
 */
public class AboutListAdapter extends BaseAdapter {

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
