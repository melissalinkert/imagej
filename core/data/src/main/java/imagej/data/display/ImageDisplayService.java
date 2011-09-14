//
// ImageDisplayService.java
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

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.Dataset;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayService;
import imagej.ext.display.event.window.WinClosedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for working with {@link ImageDisplay}s.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Service
public final class ImageDisplayService extends AbstractService {

	private final EventService eventService;
	private final DisplayService displayService;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	public ImageDisplayService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public ImageDisplayService(final ImageJ context,
		final EventService eventService, final DisplayService displayService)
	{
		super(context);
		this.eventService = eventService;
		this.displayService = displayService;
	}

	// -- ImageDisplayService methods --

	public EventService getEventService() {
		return eventService;
	}

	public DisplayService getDisplayService() {
		return displayService;
	}

	// -- DisplayService methods - display discovery --

	/** Gets the currently active {@link ImageDisplay}. */
	public ImageDisplay getActiveImageDisplay() {
		final Display<?> activeDisplay = displayService.getActiveDisplay();
		if (activeDisplay == null || !(activeDisplay instanceof ImageDisplay)) {
			return null;
		}
		return (ImageDisplay) activeDisplay;
	}

	/**
	 * Gets the active {@link Dataset}, if any, of the currently active
	 * {@link ImageDisplay}.
	 */
	public Dataset getActiveDataset() {
		return getActiveDataset(getActiveImageDisplay());
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the currently active
	 * {@link ImageDisplay}.
	 */
	public DatasetView getActiveDatasetView() {
		return getActiveDatasetView(getActiveImageDisplay());
	}

	/**
	 * Gets the active {@link Dataset}, if any, of the given {@link ImageDisplay}.
	 */
	public Dataset getActiveDataset(final ImageDisplay display) {
		final DatasetView activeDatasetView = getActiveDatasetView(display);
		return activeDatasetView == null ? null : activeDatasetView.getDataObject();
	}

	/**
	 * Gets the active {@link DatasetView}, if any, of the given
	 * {@link ImageDisplay}.
	 */
	public DatasetView getActiveDatasetView(final ImageDisplay display) {
		if (display == null) return null;
		final DisplayView activeView = display.getActiveView();
		if (activeView instanceof DatasetView) {
			return (DatasetView) activeView;
		}
		return null;
	}

	/** Gets a list of all available {@link ImageDisplay}s. */
	public List<ImageDisplay> getImageDisplays() {
		return displayService.getDisplaysOfType(ImageDisplay.class);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		subscribeToEvents();
	}

	// -- Helper methods --

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		// dispose views when display window is closed
		final EventSubscriber<WinClosedEvent> winClosedSubscriber =
			new EventSubscriber<WinClosedEvent>() {

				// CTR FIXME display views should not be disposed here!
				// This is the job of the display itself when display.dispose()
				// and/or display.close() gets called.

				@Override
				public void onEvent(final WinClosedEvent event) {
					final Display<?> display = event.getDisplay();
					if (!(display instanceof ImageDisplay)) return;
					final ImageDisplay imageDisplay = (ImageDisplay) display;
					final ArrayList<DisplayView> views =
						new ArrayList<DisplayView>(imageDisplay);
					for (final DisplayView view : views) {
						view.dispose();
					}
				}

			};
		subscribers.add(winClosedSubscriber);
		eventService.subscribe(WinClosedEvent.class, winClosedSubscriber);
	}

}