package ijx;

import java.util.Properties;

/**
 * interface for classes that have preferences to be saved on application exit
 * annotate classes that implement this with
 * @ServiceProvider()
 * @author GBH
 */
public interface SavesPrefs {
	void savePreferences(Properties prefs);
}
