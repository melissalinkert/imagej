//
// AbstractLineTool.java
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

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.tool.AbstractTool;

/**
 * Abstract class that is used by PencilTool, PaintBrushTool, and their erase
 * modes to draw lines into a dataset using fg/bg values.
 * 
 * @author Barry DeZonia
 *
 */
public abstract class AbstractLineTool extends AbstractTool {

	private DrawingTool drawingTool;
	
	private long width = 1;

	public void setWidth(long w) {
		if (w <= 0)
			width = 1;
		else
			width = w;
	}
	
	@Override
	public void onMouseDown(MsPressedEvent evt) {
		initDrawingTool(evt);
		if (drawingTool != null) {
			drawingTool.moveTo(evt.getX(), evt.getY());
		}
		super.onMouseDown(evt);
	}

	@Override
	public void onMouseDrag(MsDraggedEvent evt) {
		if (drawingTool != null) {
			drawingTool.lineTo(evt.getX(), evt.getY());
			evt.getDisplay().getPanel().redraw();
			evt.getDisplay().update();
		}
		super.onMouseDrag(evt);
	}
	
	/*
	
	// FIXME
	// Nulling out drawingTool is not really what we want here. But we do want to
	// listen for Dataset deleted kinds of events to null out drawingTool when
	// necessary and cleanup dangling ref to Dataset.
	
	@Override
	public void onMouseUp(MsReleasedEvent evt) {
		drawingTool = null;
		super.onMouseUp(evt);
	}
	
	*/

	// -- private helpers --
	
	private void initDrawingTool(MsPressedEvent evt) {
		
		// lookup display info where mouse down event happened
		final ImageJ context = evt.getContext();
		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);
		final ImageDisplay imageDisplay = (ImageDisplay)evt.getDisplay();
		if (imageDisplay == null) return;

		// get dataset associated with mouse down event
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		// try to avoid allocating objects when possible
		if ((drawingTool == null) || (drawingTool.getDataset() != dataset))
			drawingTool = new DrawingTool(dataset);
		
		// TODO : support arbitrary u/v axes, and arbitrary ortho slices
		/*
		drawingTool.setUAxis(zAxisIndex);
		drawingTool.setVAxis(xAxisIndex);
		drawingTool.setPlanePosition(viewsCurrPlanePosition);
		*/

		// set line width of drawingTool
		drawingTool.setLineWidth(width);

		/*  FIXME enable this when support code fully in place
		if (dataset.isRGBMerged()) {
			if (altKeyDown())  // in erase mode?
				drawingTool.setColorValue(currBgColor);
			else
				drawingTool.setColorValue(currFgColor);
		}
		else {
			if (altKeyDown())  // in erase mode?
				drawingTool.setGrayValue(currBgGrayValue);
			else
				drawingTool.setGrayValue(currFgGrayValue);
		}
		*/
	}
}
