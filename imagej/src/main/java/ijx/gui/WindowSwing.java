package ijx.gui;

import ijx.gui.IjxWindow;
import javax.swing.JFrame;

/**
 *
 * @author GBH
 */
public class WindowSwing extends JFrame implements IjxWindow{

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canClose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
