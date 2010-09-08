package ijx.sezpoz;

import java.awt.event.ActionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.java.sezpoz.Indexable;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Indexable(type = ActionListener.class)
public @interface MenuItem {

    String menu(); // "menu/submenu" parsed

    String toolbar() default "";

    String label(); // displayed name, Action.NAME

    String commandKey() default "";  // Action.ACTION_COMMAND_KEY, default to label, if not specified

    String hotKey() default ""; // Action.ACCELERATOR_KEY, parsed by KeyStroke.getKeyStroke()

    int mnemonic() default 0;  // Action.MNEMONIC_KEY, e.g. new Integer(java.awt.event.KeyEvent.VK_1)

    String tip() default ""; // Action.SHORT_DESCRIPTION, tooltip

    String icon() default "";  // Action.SMALL_ICON, a path to icon used in menues

    int position() default Integer.MAX_VALUE; // at end

    boolean separate() default false;  // to preceed this item with separator

    String bundle() default ""; // properties file baseName, a fully qualified class name
}
/*


 */
/* Usage
 * with all parameters
@MenuItem(
label = "Exit",
menu = "File",
commandKey="commandName",
toolbar="main",
hotKey="alt shift X",
mnemonic=java.awt.event.KeyEvent.VK_1,
tip="Tool tip displayed",
position=9,
separate=true,
icon = "demo/plugin1/movieNew16gif",
bundle = "demo.plugin1.properties"
)

public class ExitAction implements ActionListener {
public void actionPerformed(ActionEvent e) {
System.exit(0);
}
}
 *
 * For KeyStroke to String, see ijx.gui.util.KeyStrokeToString
 *
 * public static KeyStroke getKeyStroke(String s)

    Parses a string and returns a KeyStroke. The string must have the following syntax:

        <modifiers>* (<typedID> | <pressedReleasedID>)

        modifiers := shift | control | ctrl | meta | alt | button1 | button2 | button3
        typedID := typed <typedKey>
        typedKey := string of length 1 giving Unicode character.
        pressedReleasedID := (pressed | released) key
        key := KeyEvent key code name, i.e. the name following "VK_".
+

    If typed, pressed or released is not specified, pressed is assumed. Here are some examples:

         "INSERT" => getKeyStroke(KeyEvent.VK_INSERT, 0);
         "control DELETE" => getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK);
         "alt shift X" => getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK);
         "alt shift released X" => getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, true);
         "typed a" => getKeyStroke('a');

 */
