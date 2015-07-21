package com.manzdagratiano.yggdrasil;

// Android
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

// Standard Java
import java.util.ArrayList;

/**
 * @brief   The NavigationDrawerListAdpater class
 *          Provides a custom adapter for Navigation Drawer items
 *          (which are more complex types than String types) by
 *          extending the BaseAdapter class.
 */
public class NavigationDrawerListAdapter extends BaseAdapter {

    // ----------------------------------------------------------------
    // CREATORS

    /**
     * @brief   Constructor
     * @return  Does not have a return type
     */
    public NavigationDrawerListAdapter(Context context,
                                       ArrayList<NavigationDrawerItem>
                                            navigationDrawerItems) {
        this.m_context = context;
        this.m_navigationDrawerItems = navigationDrawerItems;
    }

    // ----------------------------------------------------------------
    // PUBLIC METHODS

    /**
     * @brief   
     * @return  
     */
    @Override
    public int getCount() {
        return m_navigationDrawerItems.size();
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
        return m_navigationDrawerItems.get(position);
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
            view = inflater.inflate(R.layout.navigation_drawer_item,
                                    null);
        }

        ImageView itemIcon =
            (ImageView)view.findViewById(R.id.navigationItemIcon);
        TextView itemText =
            (TextView)view.findViewById(R.id.navigationItemText);

        // Obtain a reference to the NavigationItem
        // at position "position", and use it to set resources.
        NavigationDrawerItem navigationItem =
            (NavigationDrawerItem)this.getItem(position);
        itemIcon.setImageResource(navigationItem.getIconId());
        itemText.setText(navigationItem.getText());

        return view;
    }

    // ----------------------------------------------------------------
    // PRIVATE MEMBERS

    /**
     * @brief   A reference to the Application context
     */
    private Context                         m_context;

    /**
     * @brief   A reference to the list of items in the Navigation Drawer
     */
    private ArrayList<NavigationDrawerItem> m_navigationDrawerItems;
}
