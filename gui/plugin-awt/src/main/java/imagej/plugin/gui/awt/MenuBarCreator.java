//
// MenuBarCreator.java
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

package imagej.plugin.gui.awt;

import imagej.Log;
import imagej.plugin.gui.ShadowMenu;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.util.List;

/**
 * Populate an AWT {@link MenuBar} with menu items.
 *
 * @author Curtis Rueden
 */
public class MenuBarCreator extends AWTMenuCreator<MenuBar> {

	@Override
	public void createMenus(final ShadowMenu root, final MenuBar menuBar) {
		final List<MenuItem> childMenuItems = createChildMenuItems(root);
		for (final MenuItem childMenuItem : childMenuItems) {
			if (childMenuItem instanceof Menu) {
				final Menu childMenu = (Menu) childMenuItem;
				menuBar.add(childMenu);
			}
			else {
				Log.warn("Ignoring unexpected leaf menu item: " + childMenuItem);
			}
		}
	}

}