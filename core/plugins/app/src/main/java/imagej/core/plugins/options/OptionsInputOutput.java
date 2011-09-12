//
// OptionsInputOutput.java
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

package imagej.core.plugins.options;

import imagej.ext.options.OptionsPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

/**
 * Runs the Edit::Options::Input/Output dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Input/Output...", weight = 2) })
public class OptionsInputOutput extends OptionsPlugin {

	@Parameter(label = "JPEG quality (0-100)")
	private int jpegQuality = 85;

	@Parameter(label = "GIF and PNG transparent index")
	private int transparentIndex = -1;

	@Parameter(label = "File extension for tables")
	private String tableFileExtension = ".txt";

	@Parameter(label = "Use JFileChooser to open/save")
	private boolean useJFileChooser = false;

	@Parameter(label = "Save TIFF and raw in Intel byte order")
	private boolean saveOrderIntel = false;

	// TODO - in IJ1 these were grouped visually. How is this now done?

	@Parameter(label = "Result Table: Copy column headers")
	private boolean copyColumnHeaders = false;

	@Parameter(label = "Result Table: Copy row numbers")
	private boolean copyRowNumbers = true;

	@Parameter(label = "Result Table: Save column headers")
	private boolean saveColumnHeaders = true;

	@Parameter(label = "Result Table: Save row numbers")
	private boolean saveRowNumbers = true;

}
