//
// PluginIndex.java
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

package imagej.ext.plugin;

import imagej.object.SortedObjectIndex;

/**
 * Data structure for managing registered plugins.
 * 
 * @author Curtis Rueden
 */
public class PluginIndex extends SortedObjectIndex<PluginInfo<?>> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PluginIndex() {
		// NB: See: http://stackoverflow.com/questions/4765520/
		super((Class) PluginInfo.class);
	}

	// -- Internal methods --

	@Override
	protected boolean add(final PluginInfo<?> info, final boolean batch) {
		return add(info, info.getPluginType(), batch);
	}

	@Override
	protected boolean remove(final Object o, final boolean batch) {
		if (!(o instanceof PluginInfo)) return false;
		final PluginInfo<?> info = (PluginInfo<?>) o;
		return remove(info, info.getPluginType(), batch);
	}

}
