//
// InternalFrameEventDispatcher.java
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

package imagej.ui.swing.mdi;

import imagej.event.EventService;
import imagej.event.ImageJEvent;
import imagej.ext.display.Display;
import imagej.ext.display.event.window.WinActivatedEvent;
import imagej.ext.display.event.window.WinClosedEvent;
import imagej.ext.display.event.window.WinClosingEvent;
import imagej.ext.display.event.window.WinDeactivatedEvent;
import imagej.ext.display.event.window.WinDeiconifiedEvent;
import imagej.ext.display.event.window.WinIconifiedEvent;
import imagej.ext.display.event.window.WinOpenedEvent;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Rebroadcasts AWT internal frame events as {@link ImageJEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class InternalFrameEventDispatcher implements InternalFrameListener {

	private final Display<?> display;
	private final EventService eventService;

	/** Creates an AWT event dispatcher for the given display. */
	public InternalFrameEventDispatcher(final Display<?> display,
		final EventService eventService)
	{
		this.eventService = eventService;
		this.display = display;
	}

	// -- InternalFrameListener methods --

	@Override
	public void internalFrameActivated(final InternalFrameEvent e) {
		eventService.publish(new WinActivatedEvent(display));
	}

	@Override
	public void internalFrameClosed(final InternalFrameEvent e) {
		eventService.publish(new WinClosedEvent(display));
	}

	@Override
	public void internalFrameClosing(final InternalFrameEvent e) {
		eventService.publish(new WinClosingEvent(display));
	}

	@Override
	public void internalFrameDeactivated(final InternalFrameEvent e) {
		eventService.publish(new WinDeactivatedEvent(display));
	}

	@Override
	public void internalFrameDeiconified(final InternalFrameEvent e) {
		eventService.publish(new WinDeiconifiedEvent(display));
	}

	@Override
	public void internalFrameIconified(final InternalFrameEvent e) {
		eventService.publish(new WinIconifiedEvent(display));
	}

	@Override
	public void internalFrameOpened(final InternalFrameEvent e) {
		eventService.publish(new WinOpenedEvent(display));
	}

}
