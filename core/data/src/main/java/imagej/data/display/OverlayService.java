//
// OverlayService.java
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

package imagej.data.display;

import imagej.data.overlay.Overlay;
import imagej.data.overlay.OverlaySettings;
import imagej.object.ObjectService;
import imagej.service.IService;
import imagej.util.RealRect;

import java.util.List;

/**
 * Interface for service that works with {@link Overlay}s.
 * 
 * @author Curtis Rueden
 */
public interface OverlayService extends IService {

	ObjectService getObjectService();

	/**
	 * Gets a list of all {@link Overlay}s. This method is a shortcut that
	 * delegates to {@link ObjectService}.
	 */
	List<Overlay> getOverlays();

	/**
	 * Gets a list of {@link Overlay}s linked to the given {@link ImageDisplay}.
	 */
	List<Overlay> getOverlays(final ImageDisplay display);

	/** Adds the list of {@link Overlay}s to the given {@link ImageDisplay}. */
	void addOverlays(final ImageDisplay display, final List<Overlay> overlays);

	/**
	 * Removes an {@link Overlay} from the given {@link ImageDisplay}.
	 * 
	 * @param display the {@link ImageDisplay} from which the overlay should be
	 *          removed
	 * @param overlay the {@link Overlay} to remove
	 */
	void removeOverlay(final ImageDisplay display, final Overlay overlay);

	/**
	 * Gets the bounding box for the selected overlays in the given
	 * {@link ImageDisplay}.
	 * 
	 * @param display the {@link ImageDisplay} from which the bounding box should
	 *          be computed
	 * @return the smallest bounding box encompassing all selected overlays
	 */
	RealRect getSelectionBounds(final ImageDisplay display);

	OverlaySettings getDefaultSettings();

}
