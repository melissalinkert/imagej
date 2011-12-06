//
// AbstractSwingUI.java
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

package imagej.ui.swing;

import imagej.event.EventSubscriber;
import imagej.ext.menu.MenuService;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.ui.swing.SwingJMenuBarCreator;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.ui.AbstractUserInterface;
import imagej.ui.OutputWindow;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTDropListener;

import java.awt.BorderLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

/**
 * Abstract superclass for Swing-based user interfaces.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @author Grant Harris
 */
public abstract class AbstractSwingUI extends AbstractUserInterface {

	private SwingApplicationFrame appFrame;
	private SwingToolBar toolBar;
	private SwingStatusBar statusBar;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	// -- UserInterface methods --

	@Override
	public SwingApplicationFrame getApplicationFrame() {
		return appFrame;
	}

	@Override
	public SwingToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public SwingStatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public void createMenus() {
		final JMenuBar menuBar = createMenuBar(appFrame);
		getEventService().publish(new AppMenusCreatedEvent(menuBar));
	}

	@Override
	public OutputWindow newOutputWindow(final String title) {
		return new SwingOutputWindow(title);
	}

	// -- Internal methods --

	@Override
	protected void createUI() {
		appFrame = new SwingApplicationFrame("ImageJ");
		toolBar = new SwingToolBar(getEventService());
		statusBar = new SwingStatusBar(getEventService());
		createMenus();

		setupAppFrame();

		super.createUI();

		// NB: The following setup happens for both SDI and MDI frames.

		appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		appFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				getUIService().getEventService().publish(new AppQuitEvent());
			}

		});

		appFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
		appFrame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		// listen for keyboard events on all components of the app frame
		final AWTKeyEventDispatcher keyDispatcher =
			new AWTKeyEventDispatcher(null, getEventService());
		appFrame.addEventDispatcher(keyDispatcher);

		appFrame.pack();
		appFrame.setVisible(true);

		// setup drag and drop targets
		final AWTDropListener dropListener = new AWTDropListener(getUIService());
		new DropTarget(toolBar, dropListener);
		new DropTarget(statusBar, dropListener);
		new DropTarget(appFrame, dropListener);

		if (getUIService().getPlatformService().isMenuBarDuplicated()) {
			// NB: If menu bars are supposed to be duplicated across all window
			// frames, listen for display creations and deletions and clone the menu
			// bar accordingly.
			subscribers = getEventService().subscribe(this);
		}
	}

	/**
	 * Configures the application frame for subclass-specific settings (e.g., SDI
	 * or MDI).
	 */
	protected abstract void setupAppFrame();

	/**
	 * Creates a {@link JMenuBar} from the master {@link ShadowMenu} structure,
	 * and adds it to the given {@link JFrame}.
	 */
	protected JMenuBar createMenuBar(final JFrame f) {
		final MenuService menuService = getUIService().getMenuService();
		final JMenuBar menuBar =
			menuService.createMenus(new SwingJMenuBarCreator(), new JMenuBar());
		f.setJMenuBar(menuBar);
		f.validate();
		return menuBar;
	}

}
