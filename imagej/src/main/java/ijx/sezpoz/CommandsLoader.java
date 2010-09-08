/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.sezpoz;

import ij.plugin.PlugIn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.WindowConstants;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

public class CommandsLoader {

    Map<String, JMenu> topMenus = new HashMap<String, JMenu>();
    Map<String, Action> actions = new HashMap<String, Action>();

    public void loadCommands() {


        for (final IndexItem<MenuItem, ActionListener> item : Index.load(MenuItem.class, ActionListener.class)) {
            // Find or create menu if it does not exist
            String menuName = item.annotation().menu();

            // Create the menu item with label and icon

            Action action = createAction(item);




        }

    }

    JMenu findOrCreateMenu(String menuName) {
        JMenu menu = topMenus.get(menuName);
        if (menu == null) {
            menu = new JMenu(menuName);
            topMenus.put(menuName, menu);

        }
        return menu;
    }

//    ArrayList<Menu> rootMenu = new ArrayList();
//    Tree buildMenuTree() {
//
//    }
    JMenuBar createMenu() {

        JMenuBar bar = new JMenuBar();

        Iterator it = actions.entrySet().iterator();
        for (Entry<String, Action> e : actions.entrySet()) {
            String menuName = e.getKey();
            Action action = e.getValue();
            // parse the menuName topMenu>subMenu>subsubMenu

            JMenu menu = findOrCreateMenu(menuName);
            //equiv: JMenuItem menuItem = new JMenuItem(action); menu.add(menuItem);
            menu.add(action);
        }
        // create Top Level Menu
        //bar.add(menu);

        return bar;
    }

    public static Action createAction(final IndexItem<MenuItem, ActionListener> item) {
        String label = item.annotation().label();
        System.out.println("creating action: " + label);
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                try {
                    // class is instantiated when this is invoked
                    item.instance().actionPerformed(e);
                } catch (InstantiationException x) {
                    x.printStackTrace();
                }
            }
        };
        // Add Icon...
        if (!item.annotation().icon().isEmpty()) {
            try {
                ImageIcon img = new ImageIcon(ClassLoader.getSystemResource(item.annotation().icon()));
                //java.net.URL imgURL = item.getClass().getResource(item.annotation().icon());
                if (img == null) {
                    System.err.println("Couldn't find icon: " + item.annotation().icon());
                }
                if (img != null) {
                    action.putValue(Action.SMALL_ICON, img);
                }
            } catch (Exception e) {
            }
        }
        if (!item.annotation().label().isEmpty()) {
            action.putValue(Action.NAME, item.annotation().label());
        }
        if (!item.annotation().tip().isEmpty()) {
            action.putValue(Action.SHORT_DESCRIPTION, item.annotation().tip());
        }
        if (!item.annotation().commandKey().isEmpty()) {
            action.putValue(Action.ACTION_COMMAND_KEY, item.annotation().commandKey());
        }
        if (item.annotation().mnemonic() != 0) {
            action.putValue(Action.MNEMONIC_KEY, item.annotation().mnemonic());
        }
        if (!item.annotation().hotKey().isEmpty()) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(item.annotation().hotKey()));
        }

        return action;
    }

    /*
     * public interface Action extends ActionListener
    {
    public static final String DEFAULT = "Default";
    public static final String NAME = "Name";
    public static final String SHORT_DESCRIPTION = "ShortDescription";
    public static final String LONG_DESCRIPTION = "LongDescription";
    public static final String SMALL_ICON = "SmallIcon";
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public Object getValue(String key);
    public boolean isEnabled();
    public void putValue(String key, Object value);
    public void removePropertyChangeListener(PropertyChangeListener listener);
    public void setEnabled(boolean b);
    }

     */
    public void loadAllItems() {
        Map<String, IndexItem<MenuItem, ActionListener>> items = new HashMap<String, IndexItem<MenuItem, ActionListener>>();


        for (final IndexItem<MenuItem, ActionListener> item : Index.load(MenuItem.class, ActionListener.class)) {
            String label = item.annotation().label();
            items.put(label, item);
        }
    }

    public void loadResources(final IndexItem<MenuItem, ActionListener> item) {
        String bundle = item.annotation().bundle();
        ResourceBundle res = ResourceBundle.getBundle(bundle, Locale.getDefault());
        String label = res.getString("LABEL");


    }

    void createTestFrame(JMenuBar bar) {
        Logger logger = Logger.getLogger("net.java.sezpoz");
        logger.setLevel(Level.ALL);
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        JFrame f = new JFrame("Demo");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setJMenuBar(bar);
        f.add(new JLabel("ActionMenuLoadingDemo"));
        f.pack();
        f.setVisible(true);


    }

    public static void main(String[] args) {
        new CommandsLoader().loadCommands();


    }
}
