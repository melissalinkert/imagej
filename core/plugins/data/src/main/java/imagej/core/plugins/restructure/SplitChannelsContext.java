//
// SplitChannelsContext.java
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

package imagej.core.plugins.restructure;

import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;

import java.util.HashMap;

/**
 * Context menu plugin for Split Channels legacy command.
 * 
 * @author Curtis Rueden
 */
@Plugin(menu = { @Menu(label = "Split Channels", mnemonic = 's') },
	menuRoot = Plugin.CONTEXT_MENU_ROOT, headless = true)
public class SplitChannelsContext implements ImageJPlugin {

	// -- Plugin parameters --

	@Parameter(persist = false)
	private PluginService pluginService;

	// -- RunnablePlugin methods --

	@Override
	public void run() {
		// TODO: Figure out why the parameter order is messed up and this fails:
//		pluginService.run("imagej.legacy.plugin.LegacyPlugin",
//			"ij.plugin.ChannelSplitter");
		final HashMap<String, Object> inputValues = new HashMap<String, Object>();
		inputValues.put("className", "ij.plugin.ChannelSplitter");
		pluginService.run("imagej.legacy.plugin.LegacyPlugin", inputValues);
	}

}
