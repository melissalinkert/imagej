//
// PivotInputHarvester.java
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

package imagej.plugin.ui.pivot;

import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.ui.AbstractInputHarvester;
import imagej.plugin.ui.InputPanel;
import imagej.plugin.process.PluginPreprocessor;

import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Sheet;

/**
 * PivotInputHarvester is a plugin preprocessor that collects input parameter
 * values from the user using a {@link PivotInputPanel} dialog box.
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PluginPreprocessor.class)
public class PivotInputHarvester extends AbstractInputHarvester {

	@Override
	public PivotInputPanel createInputPanel() {
		return new PivotInputPanel();
	}

	@Override
	public boolean showDialog(final InputPanel inputPanel,
		final PluginModule<?> module)
	{
		final Sheet dialog = new Sheet();
		dialog.setTitle(module.getInfo().getLabel());
		dialog.add(((PivotInputPanel) inputPanel).getPanel());
		dialog.open((Display) null);//FIXME
		final boolean success = dialog.getResult();
		return success;
	}

}