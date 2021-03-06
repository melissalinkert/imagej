//
// AWTApplicationFrame.java
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

package imagej.ui.awt;

import imagej.ui.ApplicationFrame;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * AWT implementation of {@link ApplicationFrame}.
 * 
 * @author Grant Harris
 */
public class AWTApplicationFrame extends Frame implements ApplicationFrame {

	public AWTApplicationFrame(final String title) throws HeadlessException {
		super(title);
	}
	
	// -- ApplicationFrame methods --

	@Override
	public int getLocationX() {
		return getLocation().x;
	}

	@Override
	public int getLocationY() {
		return getLocation().y;
	}

	@Override
	public void activate() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// NB: You might think calling requestFocus() would work, but no.
				// The following solution is from: http://bit.ly/zAXzd5
				toFront();
				repaint();
			}
		});
	}

}
