//
// FloodFillTool.java
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

package imagej.core.tools;

import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.display.ImageDisplay;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsColors;
import imagej.util.ColorRGB;

/**
 * Tool implementation for flood fill.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "FloodFill", label = "Flood Fill",
	description = "Flood Fill Tool", iconPath = "/icons/tools/flood-fill.png",
	priority = FloodFillTool.PRIORITY)
public class FloodFillTool extends AbstractTool {

	// -- instance variables --

	public static final int PRIORITY = -304;

	enum Connectivity {
		EIGHT, FOUR
	}

	private Connectivity connectivity = Connectivity.EIGHT;

	private boolean altKeyDown = false;

	// -- public interface --

	/** Specify whether this flood fill operation should be 4 or 8 connected. */
	public void setConnectivity(final Connectivity c) {
		connectivity = c;
	}

	/** Gets this flood fill's current connectivity (4 or 8 connected). */
	public Connectivity getConnectivity() {
		return connectivity;
	}

	/** Implements the configuration of this tool. */
	@Override
	public void configure() {
		final PluginService pluginService =
			getContext().getService(PluginService.class);
		pluginService.run(FloodFillToolConfigPlugin.class, this);
	}

	/** Run flood fill when mouse clicked */
	@Override
	public void onMouseClick(final MsClickedEvent evt) {
		if (evt.getButton() == MsButtonEvent.LEFT_BUTTON) {
			final ImageDisplay imageDisplay = (ImageDisplay) evt.getDisplay();
			if (imageDisplay != null) {
				final PixelHelper helper = new PixelHelper();
				if (helper.recordEvent(evt)) {
					final DrawingTool drawingTool = initDrawingTool(helper.getDataset());
					final long[] currPos = getCurrPosition(imageDisplay);
					floodFill(helper.getCX(), helper.getCY(), currPos, connectivity, drawingTool);
					evt.getDisplay().getPanel().redraw();
					evt.getDisplay().update();
				}
			}
			evt.consume();
		}
	}

	/** Tracks ALT key status. Changes color of fill between FG/BG. */
	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		altKeyDown =
			evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();
	}

	/** Tracks ALT key status. Changes color of fill between FG/BG. */
	@Override
	public void onKeyUp(final KyReleasedEvent evt) {
		altKeyDown =
			evt.getModifiers().isAltDown() || evt.getModifiers().isAltGrDown();
	}

	// -- private helpers --

	/** Returns an initialized DrawingTool. */
	private DrawingTool initDrawingTool(final Dataset ds) {
		final DrawingTool tool = new DrawingTool(ds);
		// TODO - change here to support arbitrary UV axes
		tool.setUAxis(0);
		tool.setVAxis(1);
		tool.setLineWidth(1);
		if (ds.isRGBMerged()) tool.setColorValue(getFillColor());
		else tool.setGrayValue(getFillValue());
		return tool;
	}

	/** Returns the current position shown in the associated ImageDisplay. */
	private long[] getCurrPosition(final ImageDisplay imageDisplay) {
		// set the position of tool to current display's position
		// FIXME - this will break when the view axes are different than the
		// dataset's axes. this could happen from a display that combines multiple
		// datasets. Or perhaps a display that ignores some axes from a dataset.
		final long[] currPos = new long[imageDisplay.numDimensions()];
		for (int i = 0; i < currPos.length; i++)
			currPos[i] = imageDisplay.getLongPosition(i);
		return currPos;
	}

	/**
	 * Returns the fill value to use for gray datasets. It will be either the
	 * foreground or background gray value depending upon whether the ALT key is
	 * currently down.
	 */
	private double getFillValue() {
		final OptionsColors opts = getColorOptions();
		if (altKeyDown) return opts.getBgGray();
		return opts.getFgGray();
	}

	/**
	 * Returns the fill color to use for color datasets. It will be either the
	 * foreground or background color depending upon whether the ALT key is
	 * currently down.
	 */
	private ColorRGB getFillColor() {
		final OptionsColors opts = getColorOptions();
		if (altKeyDown) return opts.getBgColor();
		return opts.getFgColor();
	}

	/** Returns an OptionsColor instance */
	private OptionsColors getColorOptions() {
		final OptionsService oSrv = getContext().getService(OptionsService.class);
		return oSrv.getOptions(OptionsColors.class);
	}

	/** Actually does the flood fill. */
	private void floodFill(final long u, final long v, final long[] position,
		final Connectivity c, final DrawingTool dTool)
	{
		dTool.setPosition(position);
		final FloodFiller filler = new FloodFiller(dTool);
		if (c == Connectivity.FOUR) filler.fill4(u, v, position);
		else filler.fill8(u, v, position);
	}

}
