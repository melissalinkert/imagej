//
// LineAdapter.java
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

import imagej.data.display.OverlayView;
import imagej.data.overlay.LineOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.tools.FreehandTool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import net.imglib2.RealPoint;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.LineFigure;
import org.jhotdraw.geom.BezierPath.Node;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Plugin(type = Tool.class, name = "Line",
	description = "Straight line overlays", iconPath = "/icons/tools/line.png",
	priority = LineAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = LineAdapter.PRIORITY)
public class LineAdapter extends AbstractJHotDrawOverlayAdapter<LineOverlay> {

	public static final int PRIORITY = FreehandTool.PRIORITY - 1;

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if (!(overlay instanceof LineOverlay)) return false;
		return (figure == null) || (figure instanceof LineFigure);
	}

	@Override
	public LineOverlay createNewOverlay() {
		return new LineOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		final LineFigure figure = new LocalLineFigure();
		// Unlike some other figures this one will draw one pixel wide when width is
		// 0. This is correct behavior.
		figure.set(AttributeKeys.STROKE_WIDTH, new Double(0));
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView overlayView, final Figure figure) {
		super.updateFigure(overlayView, figure);
		assert figure instanceof LineFigure;
		final LineFigure line = (LineFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof LineOverlay;
		final LineOverlay lineOverlay = (LineOverlay) overlay;
		line.setStartPoint(new Point2D.Double(lineOverlay.getLineStart()
			.getDoublePosition(0), lineOverlay.getLineStart().getDoublePosition(1)));
		line.setEndPoint(new Point2D.Double(lineOverlay.getLineEnd()
			.getDoublePosition(0), lineOverlay.getLineEnd().getDoublePosition(1)));
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlayView)
	{
		super.updateOverlay(figure, overlayView);
		assert figure instanceof LineFigure;
		final LineFigure line = (LineFigure) figure;
		final Overlay overlay = overlayView.getData();
		assert overlay instanceof LineOverlay;
		final LineOverlay lineOverlay = (LineOverlay) overlay;
		final Node startNode = line.getNode(0);
		lineOverlay.setLineStart(new RealPoint(new double[] {
			startNode.getControlPoint(0).x, startNode.getControlPoint(0).y }));
		final Node endNode = line.getNode(1);
		lineOverlay.setLineEnd(new RealPoint(new double[] {
			endNode.getControlPoint(0).x, endNode.getControlPoint(0).y }));
	}

	/* temp workaround of HotDraw bug
	 * stroke width of 0 with stock EllipseFigure draws nothing when unselected
	 */
	private class LocalLineFigure extends LineFigure {
		@Override
		public void draw(Graphics2D g) {
			Stroke origS = g.getStroke(); 
			Color origC = g.getColor();
			// 1 pixel wide outline
			Stroke stroke = new BasicStroke((float)(1/g.getTransform().getScaleX()));
			g.setStroke(stroke);
			g.setColor(get(AttributeKeys.STROKE_COLOR));
			g.draw(this.path);
			g.setStroke(origS);
			g.setColor(origC);
		}
	}
}
