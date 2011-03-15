//
// JMenuBarCreator.java
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

package imagej.plugin.gui.swing;

import imagej.Log;
import imagej.plugin.gui.ShadowMenu;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Populate a Swing {@link JMenuBar} with menu items.
 *
 * @author Curtis Rueden
 */
public class JMenuBarCreator extends SwingMenuCreator<JMenuBar> {

	@Override
	public void createMenus(final ShadowMenu root, final JMenuBar menuBar) {
		final List<JMenuItem> childMenuItems = createChildMenuItems(root);
		for (final JMenuItem childMenuItem : childMenuItems) {
			if (childMenuItem == null) {
				Log.warn("JMenuBarCreator: Ignoring separator");
			}
			else if (childMenuItem instanceof JMenu) {
				final JMenu childMenu = (JMenu) childMenuItem;
				Log.debug("JMenuBarCreator: Adding menu: " + childMenu.getText());
				menuBar.add(childMenu);
			}
			else {
				Log.warn("JMenuBarCreator: Ignoring leaf item: " +
					childMenuItem.getText());
			}
		}
	}

}