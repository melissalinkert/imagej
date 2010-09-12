/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.awt;

import ijx.IjxFactory;
import ijx.IjxImageStack;
import ijx.IjxImagePlus;
import ijx.gui.IjxDialog;
import ijx.gui.IjxGenericDialog;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ijx.plugin.frame.IjxPluginFrame;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.image.ColorModel;

/**
 *
 * @author GBH
 */
public class FactoryAWT implements IjxFactory {

  static {
    System.out.println("using FactoryAWT");
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

  public IjxImagePlus[] newImagePlusArray(int n) {
    ImagePlus[] ipa = new ImagePlus[n];
    return ipa;
  }

  public IjxImageCanvas newImageCanvas(IjxImagePlus imp) {
    return new ImageCanvas(imp);
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
    return new ImageWindow(title);
  }

  public IjxImageWindow newImageWindow(IjxImagePlus imp) {
    return new ImageWindow(imp);
  }

  public IjxImageWindow newImageWindow(IjxImagePlus imp, IjxImageCanvas ic) {
    return new ImageWindow(imp, ic);
  }

  @Override
  public IjxImageWindow newStackWindow(IjxImagePlus imp) {
    return new StackWindow(imp);
  }

  @Override
  public IjxImageWindow newStackWindow(IjxImagePlus imp, IjxImageCanvas ic) {
    return new StackWindow(imp, ic);
  }


  public IjxWindow newWindow() {
    return new WindowAWT();
  }

  public IjxDialog newDialog() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public IjxGenericDialog newGenericDialog() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public IjxPluginFrame newPluginFrame(String title) {
    return (IjxPluginFrame) new PlugInFrame(title);
  }

}
