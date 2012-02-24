//
// AWTMenuCreator.java
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

package imagej.ext.ui.awt;

import imagej.ext.menu.ShadowMenu;

import java.awt.Menu;
import java.util.HashMap;
import java.util.Map;

/**
 * Populates an AWT {@link Menu} with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 */
public class AWTMenuCreator extends AbstractAWTMenuCreator<Menu> {

	// Unfortunately, there is no way to find out when a MenuBar is about to be
	// garbage-collected in order to remove the associated menu update listener.
	// So we just hold on to all of them indefinitely for now.
	private static Map<Menu, Object> menuUpdateListeners =
		new HashMap<Menu, Object>();

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final Menu target) {
		addLeafToMenu(shadow, target);
	}

	@Override
	protected Menu addNonLeafToTop(final ShadowMenu shadow, final Menu target) {
		return addNonLeafToMenu(shadow, target);
	}

	@Override
	protected void addSeparatorToTop(final Menu target) {
		addSeparatorToMenu(target);
	}

	@Override
	protected void removeAll(final Menu target) {
		for (int i = target.getItemCount() - 1; i >= 0; i--) {
			target.remove(i);
		}
	}

	@Override
	protected Object getMenuUpdateListener(final Menu target) {
		return menuUpdateListeners.get(target);
	}

	@Override
	protected void setMenuUpdateListener(final Menu target, final Object object) {
		menuUpdateListeners.put(target, object);
	}

}
