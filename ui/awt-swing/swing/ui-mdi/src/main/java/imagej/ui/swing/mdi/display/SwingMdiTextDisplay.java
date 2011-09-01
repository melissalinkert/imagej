/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.ui.swing.mdi.display;

import imagej.display.DisplayPanel;
import imagej.display.TextDisplay;
import imagej.display.TextDisplayPanel;
import imagej.ui.swing.display.SwingTextDisplayPanel;
import imagej.ui.swing.display.SwingTextDisplayWindow;

/**
 *
 * @author GBH
 */
public class SwingMdiTextDisplay implements TextDisplay {

	TextDisplayPanel panel;
	String name;

	public SwingMdiTextDisplay(String name) {
		setName(name);
		SwingTextDisplayWindow win = new SwingTextDisplayWindow();
		panel = new SwingTextDisplayPanel(this, win);
		win.setTitle(getName());
		//window.pack();
		win.showDisplay(true);
	}

	@Override
	public void append(String text) {
		panel.append(text);
	}

	@Override
	public void clear() {
		panel.clear();
	}

	@Override
	public DisplayPanel getDisplayPanel() {
		return (DisplayPanel) panel;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
