package ijx;

/**
 * IjxMenus
 * Constants for menus...
 * @author GBH
 */
public interface IjxMenus {
    char PLUGINS_MENU = 'p';
    char IMPORT_MENU = 'i';
    char SAVE_AS_MENU = 's';
    char SHORTCUTS_MENU = 'h'; // 'h'=hotkey
    char ABOUT_MENU = 'a';
    char FILTERS_MENU = 'f';
    char TOOLS_MENU = 't';
    char UTILITIES_MENU = 'u';
    int WINDOW_MENU_ITEMS = 5; // fixed items at top of Window menu
    int NORMAL_RETURN = 0;
    int COMMAND_IN_USE = -1;
    int INVALID_SHORTCUT = -2;
    int SHORTCUT_IN_USE = -3;
    int NOT_INSTALLED = -4;
    int COMMAND_NOT_FOUND = -5;
    int MAX_OPEN_RECENT_ITEMS = 15;
    int RGB_STACK = 10;
    int HSB_STACK = 11;
}
