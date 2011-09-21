//
// OptionsCompiler.java
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
 * Runs the Edit::Options::Compiler dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Compiler...", weight = 14) })
public class OptionsCompiler extends OptionsPlugin {

	// TODO - use enum for targetJavaVersion?

	@Parameter(label = "Target", choices = { "1.4", "1.5", "1.6", "1.7" })
	private String targetJavaVersion = "1.5";

	@Parameter(label = "Generate debugging ino (javac -g)")
	private boolean generateDebugInfo = false;

	// -- OptionsCompiler methods --

	public OptionsCompiler() {
		load(); // NB: Load persisted values *after* field initialization.
	}
	
	public String getTargetJavaVersion() {
		return targetJavaVersion;
	}

	public boolean isGenerateDebugInfo() {
		return generateDebugInfo;
	}

	public void setTargetJavaVersion(final String targetJavaVersion) {
		this.targetJavaVersion = targetJavaVersion;
	}

	public void setGenerateDebugInfo(final boolean generateDebugInfo) {
		this.generateDebugInfo = generateDebugInfo;
	}

}
