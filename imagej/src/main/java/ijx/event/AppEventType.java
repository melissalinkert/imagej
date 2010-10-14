package ijx.event;

/**
 * AppEventType: Application level events
 * @author GBH <imagejdev.org>
 */
public enum AppEventType {
    STARTING,
    STARTED,
    ERROR,
    QUITING,
    MODULE_ADDED;

  @Override public String toString() {
   //only capitalize the first letter
   String s = super.toString();
   return s.substring(0, 1) + s.substring(1).toLowerCase();
 }
}
