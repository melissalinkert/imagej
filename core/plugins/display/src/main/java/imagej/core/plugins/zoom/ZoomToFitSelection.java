//
// ZoomToFitSelection.java
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

package imagej.core.plugins.zoom;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.IntCoords;
import imagej.util.RealRect;

/**
 * Zooms in on the currently selected region.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Zoom", mnemonic = 'z'),
	@Menu(label = "To Selection", weight = 5) }, headless = true)
public class ZoomToFitSelection implements ImageJPlugin {

	@Parameter(persist = false)
	private OverlayService overlayService;

	@Parameter(persist = false)
	private ImageDisplay display;

	@Override
	public void run() {
		final RealRect selection = overlayService.getSelectionBounds(display);

		final double scale = display.getCanvas().getZoomFactor();

		final int panelOX = (int) (selection.x / scale);
		final int panelOY = (int) (selection.y / scale);
		final int width = (int) (selection.width / scale);
		final int height = (int) (selection.height / scale);

		if (width > 0 && height > 0) {
			final IntCoords topLeft = new IntCoords(panelOX, panelOY);
			final IntCoords bottomRight =
				new IntCoords(panelOX + width, panelOY + height);
			display.getCanvas().zoomToFit(topLeft, bottomRight);
		}
	}

}
