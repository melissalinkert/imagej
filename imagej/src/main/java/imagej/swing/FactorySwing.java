
package imagej.swing;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import ijx.IjxFactory;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.IjxTopComponent;
import ijx.app.IjxApplication;
import ijx.gui.AbstractImageCanvas;
import ijx.gui.AbstractImageWindow;
import ijx.gui.AbstractStackWindow;
import ijx.gui.IjxDialog;
import ijx.gui.IjxGenericDialog;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxProgressBar;
import ijx.gui.IjxToolbar;
import ijx.gui.IjxWindow;
import ijx.plugin.frame.IjxPluginFrame;
import imagej.awt.WindowAWT;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.ColorModel;
import javax.swing.JPanel;
import javax.swing.JToolBar;


/**
 *
 * @author GBH
 */
public class FactorySwing implements IjxFactory {
    static {
        System.out.println("using FactorySwing");
    }

    public IjxTopComponent newTopComponent(IjxApplication app, String title) {
        return new TopComponentSwing(app, title);
    }
    public IjxProgressBar newProgressBar(int canvasWidth, int canvasHeight) {
        return new ProgressBarSwing(canvasWidth, canvasHeight);
    }

    public IjxImagePlus newImagePlus() {
        return new ImagePlus();
    }

    public IjxImagePlus newImagePlus(String title, Image img) {
        return new ImagePlus(title, img);
    }

    public IjxImagePlus newImagePlus(String title, ImageProcessor ip) {
        return new ImagePlus(title, ip);
    }

    public IjxImagePlus newImagePlus(String pathOrURL) {
        return new ImagePlus(pathOrURL);
    }

    public IjxImagePlus newImagePlus(String title, IjxImageStack stack) {
        return new ImagePlus(title, (IjxImageStack) stack);
    }

    public IjxImagePlus CompositeImage(IjxImagePlus imp) {
        return new CompositeImage(imp);
    }

    public IjxImagePlus CompositeImage(IjxImagePlus imp, int mode) {
        return new CompositeImage(imp, mode);
    }

    public IjxImagePlus[] newImagePlusArray(int n) {
        ImagePlus[] ipa = new ImagePlus[n];
        return ipa;
    }

    public IjxImageCanvas newImageCanvas(IjxImagePlus imp) {
        return new AbstractImageCanvas(imp, new ImagePanelSwing(imp));
    }

    public IjxImageStack newImageStack() {
        return new ImageStack();
    }

    public IjxImageStack newImageStack(int width, int height) {
        return new ImageStack(width, height);
    }

    public IjxImageStack newImageStack(int width, int height, int size) {
        return new ImageStack(width, height, size);
    }

    public IjxImageStack newImageStack(int width, int height, ColorModel cm) {
        return new ImageStack(width, height, cm);
    }

    public IjxImageStack[] newImageStackArray(int n) {
        return new ImageStack[n];
    }

    public IjxImageWindow newImageWindow(String title) {
        return new AbstractImageWindow(title, (Container) new Frame());
        //return new ImageWindow(title);
    }

    public IjxImageWindow newImageWindow(IjxImagePlus imp) {
        return new AbstractImageWindow(imp, (Container) new Frame());
    }

    public IjxImageWindow newImageWindow(IjxImagePlus imp, IjxImageCanvas ic) {
        return new AbstractImageWindow(imp, ic, (Container) new Frame());
    }

    @Override
    public IjxImageWindow newStackWindow(IjxImagePlus imp) {
        return new AbstractStackWindow(imp, (Container) new Frame());
    }

    @Override
    public IjxImageWindow newStackWindow(IjxImagePlus imp, IjxImageCanvas ic) {
        return new AbstractStackWindow(imp, ic, (Container) new Frame());
    }

    public IjxWindow newWindow() {
        return new WindowSwing();
    }

    public IjxGenericDialog newGenericDialog() {
        throw new UnsupportedOperationException("Not supported yet."); // @todo
    }

    public IjxPluginFrame newPluginFrame(String title) {
        return (IjxPluginFrame) new PlugInFrameSwing(title);
    }

    public IjxDialog newDialog() {
        return null;
    }

    public IjxToolbar newToolBar() {
        return (IjxToolbar) new ToolbarSwing();
    }



}
