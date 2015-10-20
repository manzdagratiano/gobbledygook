package io.tengentoppa.yggdrasil;

/**
 * @brief   The AboutItem class
 *          Provides a model for each item in the "About" page
 */
public class AboutItem {

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
