package ijx.gui;

import javax.swing.Action;

/**
 *
 * @author GBH
 */
public interface MenuBuilder {
    void setTopMenu(String topMenu);
    void addItem(Action action);
    void addSubItem(String subMenu, Action action);
    void removeItem();

}
