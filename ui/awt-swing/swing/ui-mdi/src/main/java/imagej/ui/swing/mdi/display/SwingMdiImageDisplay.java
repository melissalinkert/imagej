//
// SwingMdiImageDisplay.java
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

package imagej.ui.swing.mdi.display;

import imagej.data.display.ImageDisplay;
import imagej.ext.plugin.Plugin;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.swing.display.AbstractSwingImageDisplay;
import imagej.ui.swing.mdi.InternalFrameEventDispatcher;

import javax.swing.JInternalFrame;

/**
 * Multiple Document Interface implementation of Swing image display plugin. The
 * MDI display is housed in a {@link JInternalFrame}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @see AbstractSwingImageDisplay
 */
@Plugin(type = ImageDisplay.class)
public class SwingMdiImageDisplay extends AbstractSwingImageDisplay {

	public SwingMdiImageDisplay() {
		super(new SwingMdiDisplayWindow());
		final SwingMdiDisplayWindow mdiWindow = (SwingMdiDisplayWindow) window;

		getPanel()
			.addEventDispatcher(new AWTKeyEventDispatcher(this, eventService));
		mdiWindow.addEventDispatcher(new InternalFrameEventDispatcher(this,
			eventService));
	}

}
