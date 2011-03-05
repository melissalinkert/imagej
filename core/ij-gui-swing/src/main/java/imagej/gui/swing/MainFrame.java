//
// MainFrame.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.gui.swing;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.ShadowMenu;
import imagej.plugin.gui.swing.JMenuBarCreator;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * A simple and dumb Swing-based main window for ImageJ2.
 *
 * @author Curtis Rueden
 */
public class MainFrame {

	private final JFrame frame;
	private final ToolBar toolBar;
	private final StatusBar statusBar;

	/** Creates a new ImageJ frame that runs as an application. */
	public MainFrame() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		frame = new JFrame("ImageJ");
		toolBar = new ToolBar();
		statusBar = new StatusBar();
		createMenuBar();

		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		pane.add(toolBar, BorderLayout.NORTH);
		pane.add(statusBar, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
	}

	private void createMenuBar() {
		final List<PluginEntry<?>> entries = PluginUtils.findPlugins();
		statusBar.setStatus("Discovered " + entries.size() + " plugins");
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final JMenuBar menuBar = new JMenuBar();
		new JMenuBarCreator().createMenus(rootMenu, menuBar);
		frame.setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		new MainFrame();
	}

}