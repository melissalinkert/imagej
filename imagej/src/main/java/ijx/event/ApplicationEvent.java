package ijx.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Use: e.g. EventBus.getDefault().publish(new ApplicationEvent(AppEventType.STARTING));
 *
 * @author GBH <imagejdev.org>
 */
public class ApplicationEvent {

    private AppEventType type;

    public ApplicationEvent(AppEventType type) {
        this.type = type;
    }

    public AppEventType getType() {
        return type;
    }

}
