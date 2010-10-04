package ijx;

import java.util.Properties;

/**
 * Interface for classes that have preferences to be saved on application exit.
 * Annotate classes that implement this with
 * @ServiceProvider()
 * @author GBH
 */
public interface SavesPrefs {
	void savePreferences(Properties prefs);
}
 