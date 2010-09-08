package ijx;

import java.util.Properties;

/**
 * interface for classes that have preferences to be saved on application exit
 * @author GBH
 */
public interface SavesPrefs {
	void savePrefs(Properties prefs);
}
