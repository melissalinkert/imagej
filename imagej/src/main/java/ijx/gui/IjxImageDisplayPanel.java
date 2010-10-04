
package ijx.gui;

import ij.gui.Overlay;
import java.awt.Rectangle;

/**
 *
 * @author GBH <imagejdev.org>
 */
public interface IjxImageDisplayPanel {

    public void setDrawingDelegate(AbstractImageCanvas _drawer);
    public void setDrawingSize(int width, int height);
    
}
