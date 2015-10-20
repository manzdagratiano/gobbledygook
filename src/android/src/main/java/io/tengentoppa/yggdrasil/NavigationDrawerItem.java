package io.tengentoppa.yggdrasil;

/**
 * @brief   The NavigationDrawerItem class
 *          Models an item in the Navigation Drawer list.
 */
public class NavigationDrawerItem {

    // ----------------------------------------------------------------
    // CREATORS

    /**
     * @brief   Constructor
     * @return  Does not have a return type
     */
    public NavigationDrawerItem(int iconId,
                                String text) {
        this.m_iconId = iconId;
        this.m_text = text;
    }

    // ----------------------------------------------------------------
    // PUBLIC METHODS

    /**
     * @brief   
     * @return  {int} The ID associated with the icon in the "R" class
     */
    public int getIconId() {
        return this.m_iconId;
    }

    /**
     * @brief   
     * @return  {String} The text associated with the item
     */
    public String getText() {
        return this.m_text;
    }

    // ----------------------------------------------------------------
    // PRIVATE MEMBERS

    private int     m_iconId;       /** @brief The id of the icon
                                      * resource in the "R" class
                                      */
    private String  m_text;         /** @brief The display text
                                      * associated with the item
                                      */
}
