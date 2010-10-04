package ijx.app;

/**
 *
 *
 * new ApplicationEvent(STARTUP);
 * @author GBH <imagejdev.org>
 */
public class ApplicationEvent {
    public static final String STARTUP = "startup";
    public static final String QUITING = "quiting";
    public static final String NEWMODULE = "newModule";
    private String type;

    public ApplicationEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
