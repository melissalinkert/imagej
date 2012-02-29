//
// RectangleAdapter.java
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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.RectangleOverlay;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.imglib2.roi.RectangleRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 * @author Grant Harris
 */
@Plugin(type = Tool.class, name = "Rectangle",
	description = "Rectangular overlays",
	iconPath = "/icons/tools/rectangle.png",
	priority = RectangleAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = RectangleAdapter.PRIORITY)
public class RectangleAdapter extends
	AbstractJHotDrawOverlayAdapter<RectangleOverlay>
{

	public static final int PRIORITY = 100;

	// initial mouse down point is recorded for status bar updates
	private final Point anchor = new Point();

	protected static RectangleOverlay downcastOverlay(final Overlay roi) {
		assert (roi instanceof RectangleOverlay);
		return (RectangleOverlay) roi;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if ((figure != null) && (!(figure instanceof RectangleFigure))) return false;
		if (overlay instanceof RectangleOverlay) return true;
		return false;
	}

	@Override
	public Overlay createNewOverlay() {
		return new RectangleOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final RectangleFigure figure = new LocalRectangleFigure();
		figure.set(AttributeKeys.FILL_COLOR, getDefaultFillColor());
		// Unlike some other figures this one will draw one pixel wide when width is
		// 0. This is correct behavior.
		figure.set(AttributeKeys.STROKE_WIDTH, new Double(0));
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlay, final Figure f) {
		super.updateFigure(overlay, f);
		final RectangleOverlay rectangleOverlay =
			downcastOverlay(overlay.getData());
		final RectangleRegionOfInterest roi =
			rectangleOverlay.getRegionOfInterest();
		final double x0 = roi.getOrigin(0);
		final double w = roi.getExtent(0);
		final double y0 = roi.getOrigin(1);
		final double h = roi.getExtent(1);
		final Point2D.Double anch = new Point2D.Double(x0, y0);
		final Point2D.Double lead = new Point2D.Double(x0 + w, y0 + h);
		f.setBounds(anch, lead);
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlay) {
		super.updateOverlay(figure, overlay);
		final RectangleOverlay rOverlay = downcastOverlay(overlay.getData());
		final RectangleRegionOfInterest roi = rOverlay.getRegionOfInterest();
		final Rectangle2D.Double bounds = figure.getBounds();
		roi.setOrigin(bounds.getMinX(), 0);
		roi.setOrigin(bounds.getMinY(), 1);
		roi.setExtent(bounds.getWidth(), 0);
		roi.setExtent(bounds.getHeight(), 1);
	}

	// NB - show x,y,w,h of rectangle in StatusBar on click-drag

	// click - record start point
	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		anchor.x = evt.getX();
		anchor.y = evt.getY();
		// NB: Prevent PixelProbe from overwriting the status bar.
		evt.consume();
	}

	// drag - publish rectangle dimensions in status bar
	@Override
	public void onMouseDrag(final MsDraggedEvent evt) {
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		final EventService eventService =
			evt.getContext().getService(EventService.class);
		final ImageDisplayService imgService =
			evt.getContext().getService(ImageDisplayService.class);
		final ImageDisplay imgDisp = imgService.getActiveImageDisplay();
		final IntCoords startPt = new IntCoords(anchor.x, anchor.y);
		final IntCoords endPt =
			new IntCoords(evt.getX() - anchor.x, evt.getY() - anchor.y);
		final RealCoords startPtModelSpace =
			imgDisp.getCanvas().panelToImageCoords(startPt);
		final RealCoords endPtModelSpace =
			imgDisp.getCanvas().panelToImageCoords(endPt);
		final int x = (int) startPtModelSpace.x;
		final int y = (int) startPtModelSpace.y;
		final int w = (int) endPtModelSpace.x;
		final int h = (int) endPtModelSpace.y;
		final String message = String.format("x=%d, y=%d, w=%d, h=%d", x, y, w, h);
		eventService.publish(new StatusEvent(message));
		// NB: Prevent PixelProbe from overwriting the status bar.
		evt.consume();
	}

	/* temp workaround of HotDraw bug
	 * stroke width of 0 with stock RectangleFigure draws nothing when unselected
	 */
	private class LocalRectangleFigure extends RectangleFigure {

		@Override
		public void draw(final Graphics2D g) {
			final Stroke origS = g.getStroke();
			final Color origC = g.getColor();
			// 1 pixel wide outline
			final Stroke stroke =
				new BasicStroke((float) (1 / g.getTransform().getScaleX()));
			g.setStroke(stroke);
			g.setColor(get(AttributeKeys.STROKE_COLOR));
			g.draw(this.rectangle);
			g.setStroke(origS);
			g.setColor(origC);
		}
	}
}
