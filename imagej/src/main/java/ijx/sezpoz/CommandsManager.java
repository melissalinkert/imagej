/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.sezpoz;

import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilterRunner;
import ijx.app.Option;
import ijx.gui.MenuBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import org.pf.joi.Inspector;

public class CommandsManager {
    //
    Map<String, Action> commands = new HashMap<String, Action>();  // commandKey / action
    Map<String, String> menuCommands = new HashMap<String, String>();  // "menu>submenu" / commandKey
    Map<String, String> toolbarCommands = new HashMap<String, String>();  // toolbar / commandKey
    Map<String, IndexItem<ActionIjx, ?>> items =
            new HashMap<String, IndexItem<ActionIjx, ?>>();

    public Action getAction(String commandKey) {
        return commands.get(commandKey);
    }

    public void loadAllItems() {
        for (final IndexItem<ActionIjx, ActionListener> item : Index.load(ActionIjx.class, ActionListener.class)) {
            String commandKey = null;
            if (!item.annotation().commandKey().isEmpty()) {
                commandKey = item.annotation().commandKey();
            } else {
                commandKey = item.annotation().label();
            }
            Action action = createActionForActionListener(commandKey, item);
            addToMaps(commandKey, action, item);
        }
    }

        public void loadOptions() {
        for (final IndexItem<Option, Object> item : Index.load(Option.class, Object.class)) {
            try {
                String clazz = item.className();
                System.out.println("clazz = " + clazz);
                String fieldName = item.memberName();
                System.out.println("fieldName = " + fieldName);
                System.out.println("item.element().getClass()" +item.element().getClass().getCanonicalName());
            } catch (InstantiationException ex) {
                Logger.getLogger(CommandsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
//    public void loadAllPlugins() {
//        for (final IndexItem<ActionIjx, PlugIn> item : Index.load(ActionIjx.class, PlugIn.class)) {
//            String commandKey = null;
//            if (!item.annotation().commandKey().isEmpty()) {
//                commandKey = item.annotation().commandKey();
//            } else {
//                commandKey = item.annotation().label();
//            }
//            Action action = createActionForPlugIn(commandKey, item);
//            addToMaps(commandKey, action, item);
//
//        }
//    }

    private void addToMaps(String commandKey, Action action, IndexItem<ActionIjx, ?> item) {
        commands.put(commandKey, action);
        action.putValue(Action.ACTION_COMMAND_KEY, commandKey);
        decorateAction(action, item);
        items.put(commandKey, item);  // save for use in creating UI components
        if (!item.annotation().menu().isEmpty()) {
            menuCommands.put(item.annotation().menu(), commandKey);
        }
        if (!item.annotation().toolbar().isEmpty()) {
            toolbarCommands.put(item.annotation().toolbar(), commandKey);
        }
    }

//    public void loadImporters() {
//        for (final IndexItem<Importer, ij.plugin.PlugIn> item : Index.load(Importer.class, ij.plugin.PlugIn.class)) {
//            System.out.println("Importer Found: " + item.annotation().commandKey() + item.annotation().fileExts());
//        }
//    }

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

    
// <editor-fold defaultstate="collapsed" desc=" Action Creation ">
    public static Action createActionForActionListener(String commandKey, final IndexItem<ActionIjx, ActionListener> item) {
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
        return action;
    }

    public static Action createActionForPlugIn(String commandKey, final IndexItem<ActionIjx, PlugIn> item) {
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
        return action;
    }

    public void decorateAction(Action action, final IndexItem<ActionIjx, ?> item) {
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

    // </editor-fold>
     */
    private void AddAllItemsToUI() {
        for (Entry<String, IndexItem<ActionIjx, ?>> e : items.entrySet()) {
            String commandKey = e.getKey();
            IndexItem<ActionIjx, ?> item = e.getValue();
            AddItemToUI(commandKey, item);
        }
    }
    // Just lists them out, so far...

    private void AddItemToUI(String commandKey, IndexItem<ActionIjx, ?> item) {
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
    }

    private void buildMenu() {
        MenuBuilder mBuilder = ij.IJ.getFactory().newMenuBuilder(commands, menuCommands, toolbarCommands, items);
        mBuilder.build();
    }

    public void loadResources(final IndexItem<ActionIjx, ActionListener> item) {
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

        // cl.loadImporters();

        cl.AddAllItemsToUI();

        Inspector.inspectWait(cl);
//        System.out.println("Invoking action: radioA");
//        Action a = cl.getAction("radioA");
//        a.actionPerformed(null);

    }
}
