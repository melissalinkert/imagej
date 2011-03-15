//
// PluginModuleInfo.java
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

package imagej.plugin;

import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ModuleInfo} class for querying metadata of a {@link BasePlugin}.
 *
 * @author Curtis Rueden
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public class PluginModuleInfo<T extends BasePlugin> implements ModuleInfo {

	/** The plugin entry associated with this module info. */
	private final PluginEntry<T> pluginEntry;

	/** Class object of this plugin. */
	private final Class<?> pluginClass;

	private final Map<String, ModuleItem> inputs =
		new HashMap<String, ModuleItem>();

	private final Map<String, ModuleItem> outputs =
		new HashMap<String, ModuleItem>();

	/** Creates module info for the given plugin entry. */
	public PluginModuleInfo(final PluginEntry<T> pluginEntry)
		throws PluginException
	{
		this(pluginEntry, null);
	}

	/**
	 * Creates module info for the given plugin entry, using
	 * the given instance to access and assign parameter values.
	 *
	 * This constructor is used by {@link PluginModule} to ensure that
	 * the {@link PluginModuleItem}s report the correct default values,
	 * and that the {@link PluginEntry}'s presets are assigned correctly.
	 */
	PluginModuleInfo(final PluginEntry<T> pluginEntry, final T plugin)
		throws PluginException
	{
		this.pluginEntry = pluginEntry;
		pluginClass = pluginEntry.loadClass();
		checkFields(pluginClass, plugin, true);
	}

	public PluginEntry<T> getPluginEntry() {
		return pluginEntry;
	}

	// -- ModuleInfo methods --

	@Override
	public String getName() {
		return pluginEntry.getName();
	}

	@Override
	public String getLabel() {
		return pluginEntry.getLabel();
	}

	@Override
	public String getDescription() {
		return pluginEntry.getDescription();
	}

	@Override
	public PluginModuleItem getInput(final String name) {
		return (PluginModuleItem) inputs.get(name);
	}

	@Override
	public PluginModuleItem getOutput(final String name) {
		return (PluginModuleItem) outputs.get(name);
	}

	@Override
	public Iterable<ModuleItem> inputs() {
		return inputs.values();
	}

	@Override
	public Iterable<ModuleItem> outputs() {
		return outputs.values();
	}

	// -- Helper methods --

	/**
	 * Recursively parses the given class's declared fields for
	 * {@link Parameter} annotations.
	 *
	 * This method (rather than {@link Class#getFields()}) is used to check all
	 * fields of the given type and ancestor types, including non-public fields.
	 */
	private void checkFields(final Class<?> type, final T plugin,
		final boolean includePrivateFields)
	{
		if (type == null) return;

		final Map<String, Object> presets = pluginEntry.getPresets();

		for (final Field f : type.getDeclaredFields()) {
			final boolean isPrivate = Modifier.isPrivate(f.getModifiers());
			if (isPrivate && !includePrivateFields) continue;
			f.setAccessible(true); // expose private fields

			final Parameter param = f.getAnnotation(Parameter.class);
			if (param == null) continue; // not a parameter

			final String name = f.getName();
			if (presets.containsKey(name)) {
				// assign preset value to field, and exclude from the list of inputs
				PluginModule.setValue(f, plugin, presets.get(name));
			}
			else {
				// add item to the relevant list (inputs or outputs)
				final Object defaultValue = PluginModule.getValue(f, plugin);
				final ModuleItem item = new PluginModuleItem(f, defaultValue);
				if (param.output()) outputs.put(name, item);
				else inputs.put(name, item);
			}
		}

		// check super-types for annotated fields as well
		checkFields(type.getSuperclass(), plugin, false);
		for (final Class<?> c : type.getInterfaces()) {
			checkFields(c, plugin, false);
		}
	}

}