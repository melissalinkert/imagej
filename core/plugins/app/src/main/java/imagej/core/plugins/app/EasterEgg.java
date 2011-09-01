//
// EasterEgg.java
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

package imagej.core.plugins.app;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.TextDisplay;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;
import net.imglib2.RandomAccess;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Mysterious!
 * 
 * @author Curtis Rueden
 */
@Plugin(label = "| It's a secret to everyone |")
public class EasterEgg implements ImageJPlugin {

	private static final String CHARS = " .,-+o*O#";

	@Parameter
	public Dataset dataset;

	@Override
	public void run() {
		final double min = dataset.getChannelMinimum(0);
		final double max = dataset.getChannelMaximum(0);
		if (min == max) return; // no range

		final UIService uiService = ImageJ.get(UIService.class);
		final TextDisplay window =
			uiService.createOutputWindow(dataset.getName() + " *~= SPECIAL =~*");
		//window.setVisible(true);

		final ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		final int colCount = (int) imgPlus.dimension(0);
		final int rowCount = (int) imgPlus.dimension(1);

		final RandomAccess<? extends RealType<?>> access = imgPlus.randomAccess();
		final StringBuilder sb = new StringBuilder();
		for (int r = 0; r < rowCount; r++) {
			access.setPosition(r, 1);
			sb.setLength(0);
			for (int c = 0; c < colCount; c++) {
				access.setPosition(c, 0);
				final double value = access.get().getRealDouble();
				sb.append(getChar(value, min, max));
			}
			sb.append("\n");
			window.append(sb.toString());
		}
	}

	// -- Helper methods --

	private char getChar(final double value, final double min, final double max) {
		final int len = CHARS.length();
		final double norm = (value - min) / (max - min); // normalized to [0, 1]
		final int index = (int) (len * norm);
		if (index < 0) return CHARS.charAt(0);
		if (index >= len) return CHARS.charAt(len - 1);
		return CHARS.charAt(index);
	}

}
