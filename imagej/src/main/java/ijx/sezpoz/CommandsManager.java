/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.sezpoz;

import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilterRunner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

public class CommandsManager {
    //
    Map<String, Action> commands = new HashMap<String, Action>();

    Map<String, IndexItem<MenuItem, ActionListener>> items =
            new HashMap<String, IndexItem<MenuItem, ActionListener>>();

    public Action getAction(String commandKey) {
        return commands.get(commandKey);
    }

    public void loadAllItems() {
        for (final IndexItem<MenuItem, ActionListener> item : Index.load(MenuItem.class, ActionListener.class)) {
            String commandKey = null;
            if (!item.annotation().commandKey().isEmpty()) {
                commandKey = item.annotation().commandKey();
            } else {
                commandKey = item.annotation().label();
            }
            Action action = createActionActionListener(commandKey, item);
            commands.put(commandKey, action);
            decorateAction(action, item);
            items.put(commandKey, item);  // save for use in creating UI components
        }
    }

    public void loadAllPlugins() {
        for (final IndexItem<MenuItem, PlugIn> item : Index.load(MenuItem.class, PlugIn.class)) {
            String commandKey = null;
            if (!item.annotation().commandKey().isEmpty()) {
                commandKey = item.annotation().commandKey();
            } else {
                commandKey = item.annotation().label();
            }
            Action action = createActionPlugIn(commandKey, item);
            decorateAction(action, item);
            commands.put(commandKey, action);
            // will need another item type map here...
        }
    }

    public void loadImporters() {
        for (final IndexItem<Importer, ij.plugin.PlugIn> item : Index.load(Importer.class, ij.plugin.PlugIn.class)) {
            System.out.println("Importer Found: " + item.annotation().commandKey() + item.annotation().fileExts());
        }
    }

//    public void loadExtendedPluginFilters() {
//        for (final IndexItem<PluginFilterItem, ActionListener> item :
//                Index.load(PluginFilterItem.class, ActionListener.class)) {
//            String commandKey = item.annotation().commandKey();
//            Action action = createFilterAction(commandKey, item);
//            commands.put(commandKey, action);
//
//            System.out.println("Filter Found: " + item.annotation().commandKey());
//        }
//    }

//    public static Action createFilterAction(String commandKey,
//            final IndexItem<PluginFilterItem, ActionListener> item) {
//        System.out.println("creating action: " + commandKey);
//        Action action = new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    // class is instantiated when this is invoked
//                    String commandKey = item.annotation().commandKey();
//                    String label = item.annotation().label();
//                    String arg = item.annotation().argument();
//                    new PlugInFilterRunner(item.instance(), commandKey, arg);
//
//                } catch (InstantiationException x) {
//                    x.printStackTrace();
//                }
//            }
//        };
//        action.putValue(Action.ACTION_COMMAND_KEY, commandKey);
//        return null;
//    }


    public static Action createActionActionListener(String commandKey, final IndexItem<MenuItem, ActionListener> item) {
        System.out.println("creating action: " + commandKey);
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
        action.putValue(Action.ACTION_COMMAND_KEY, commandKey);
        return action;
    }


    public static Action createActionPlugIn(String commandKey, final IndexItem<MenuItem, PlugIn> item) {
        System.out.println("creating action (plugin): " + commandKey);
        String[] args = item.annotation().args();
        final String arg = args[0];
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // class is instantiated when this is invoked
                    item.instance().run(arg);
                } catch (InstantiationException x) {
                    x.printStackTrace();
                }
            }
        };
        action.putValue(Action.ACTION_COMMAND_KEY, commandKey);
        // Add Icon...
        return action;
    }

    public void decorateAction(Action action, final IndexItem<MenuItem, ?> item) {
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
        if (item.annotation().mnemonic() != 0) {
            action.putValue(Action.MNEMONIC_KEY, item.annotation().mnemonic());
        }
        if (!item.annotation().hotKey().isEmpty()) {
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(item.annotation().hotKey()));
        }
    }

    /* for reference:
    public interface Action extends ActionListener {
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
    private void AddAllItemsToUI() {
        //Iterator it = items.entrySet().iterator();
        for (Entry<String, IndexItem<MenuItem, ActionListener>> e : items.entrySet()) {
            String commandKey = e.getKey();
            IndexItem<MenuItem, ActionListener> item = e.getValue();
            AddItemToUI(commandKey, item);
        }
    }
    // Just lists them out, so far...

    private void AddItemToUI(String commandKey, IndexItem<MenuItem, ActionListener> item) {
        String menu = item.annotation().menu();
        String bundlePath = item.annotation().bundle(); // for i18n
        String toolbar = item.annotation().toolbar();
        int pos = item.annotation().position();
        boolean separator = item.annotation().separate();
        boolean state = item.annotation().state();
        String group = item.annotation().group();
        System.out.println(commandKey + " "
                + menu + " "
                + bundlePath + " "
                + toolbar + " "
                + pos + " "
                + separator + " "
                + state + " " + group);
        //
    }
    Map<String, JMenu> topMenus = new HashMap<String, JMenu>();
    Map<String, ButtonGroup> groups = new HashMap<String, ButtonGroup>();

    public void selectRadioButton(JRadioButton b, ButtonGroup group) {
        // Select the radio button; the currently selected radio button is deselected.
        // This operation does not cause any action events to be fired.
        // Better to use Action.SELECTED_KEY, but only in JDK 6.
        ButtonModel model = b.getModel();
        group.setSelected(model, true);
    }

    JMenu findOrCreateMenu(String menuName) {
        JMenu menu = topMenus.get(menuName);
        if (menu == null) {
            menu = new JMenu(menuName);
            topMenus.put(menuName, menu);
        }
        return menu;
    }

    JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();
        Iterator it = commands.entrySet().iterator();
        for (Entry<String, Action> e : commands.entrySet()) {
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

    private void createItem(IndexItem<MenuItem, ActionListener> item) {
        String radioButtonGroup = item.annotation().group();
        if (!radioButtonGroup.isEmpty()) {
            // this is a radioButton item
            if (!item.annotation().menu().isEmpty()) {
                createRadioButtonMenuItem(item);
            }
            if (!item.annotation().toolbar().isEmpty()) {
                createRadioButtonOnToolbar(item);
            }
            //     JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(getAction(action);
            // does group already exist?  If not, create.
            if (groups.get(radioButtonGroup) == null) {
                groups.put(radioButtonGroup, new ButtonGroup());
            }
            //     groups.get(radioButtonGroup).add(radioItem);
        }
    }

    private void createRadioButtonMenuItem(IndexItem<MenuItem, ActionListener> item) {
        String menuName = item.annotation().menu();
        //...
    }

    private void createRadioButtonOnToolbar(IndexItem<MenuItem, ActionListener> item) {
        String toolbarName = item.annotation().toolbar();
        //...
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
        CommandsManager cl = new CommandsManager();
        cl.loadAllItems();

        cl.loadImporters();

        cl.AddAllItemsToUI();

        System.out.println("Invoking action: radioA");
        Action a = cl.getAction("radioA");
        a.actionPerformed(null);

    }
}
