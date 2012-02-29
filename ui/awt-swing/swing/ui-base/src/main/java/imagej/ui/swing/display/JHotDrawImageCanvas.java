//
// JHotDrawImageCanvas.java
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

package imagej.ui.swing.display;

import imagej.ImageJ;
import imagej.data.display.CanvasHelper;
import imagej.data.display.DataView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.display.event.DataViewDeselectedEvent;
import imagej.data.display.event.DataViewSelectedEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.MouseCursor;
import imagej.ext.tool.Tool;
import imagej.ext.tool.ToolService;
import imagej.ext.tool.event.ToolActivatedEvent;
import imagej.ui.common.awt.AWTCursors;
import imagej.ui.common.awt.AWTKeyEventDispatcher;
import imagej.ui.common.awt.AWTMouseEventDispatcher;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.IJCreationTool.FigureCreatedEvent;
import imagej.ui.swing.overlay.IJHotDrawOverlayAdapter;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureSelectionEvent;
import org.jhotdraw.draw.event.FigureSelectionListener;

/**
 * A Swing implementation of {@link ImageCanvas}, which uses JHotDraw's
 * {@link DefaultDrawingView} component to do most of the work.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public class JHotDrawImageCanvas extends JPanel implements ImageCanvas,
	AdjustmentListener
{

	private static final long serialVersionUID = 1L;

	private final ImageDisplay display;

	private final CanvasHelper canvasHelper;

	private final Drawing drawing;
	private final DefaultDrawingView drawingView;
	private final DrawingEditor drawingEditor;
	private final ToolDelegator toolDelegator;

	private final JScrollPane scrollPane;

	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	public JHotDrawImageCanvas(final ImageDisplay display) {
		this.display = display;
		canvasHelper = new CanvasHelper(this);

		drawing = new DefaultDrawing(); // or QuadTreeDrawing?

		drawingView = new DefaultDrawingView();
		drawingView.setDrawing(drawing);

		drawingEditor = new DefaultDrawingEditor();
		drawingEditor.add(drawingView);
		toolDelegator = new ToolDelegator();
		drawingEditor.setTool(toolDelegator);

		scrollPane = new JScrollPane(drawingView);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

		final Tool activeTool = ImageJ.get(ToolService.class).getActiveTool();
		activateTool(activeTool);
		final EventService eventService = ImageJ.get(EventService.class);
		subscribers = eventService.subscribe(this);

		drawingView.addFigureSelectionListener(new FigureSelectionListener() {

			@Override
			public void selectionChanged(final FigureSelectionEvent event) {
				onFigureSelectionChanged(event);
			}
		});
	}

	/**
	 * Respond to the JHotDraw figure selection event by selecting and deselecting
	 * views whose state has changed
	 * 
	 * @param event - event indicating that the figure selections have changed
	 */
	protected void onFigureSelectionChanged(final FigureSelectionEvent event) {
		final Set<Figure> newSelection = event.getNewSelection();
		final Set<Figure> oldSelection = event.getOldSelection();
		for (final DataView view : display) {
			if (view instanceof FigureView) {
				final Figure figure = ((FigureView) view).getFigure();
				if (newSelection.contains(figure)) {
					// BDZ removed next line 10-12-11
					// Fixes drawing of multiple overlays (#817). Lee had this code
					// here in anticipation of avoiding infinite event loops.
					// Inspection seems to bear out that this possibility doesn't
					// happen.
					// if (!oldSelection.contains(figure))
					view.setSelected(true);
				}
				else if (oldSelection.contains(figure)) {
					view.setSelected(false);
				}
			}
		}
	}

	@EventHandler
	protected void onViewSelected(final DataViewSelectedEvent event) {
		final DataView view = event.getView();
		if (display.contains(view) && view instanceof FigureView) {
			final Figure figure = ((FigureView) view).getFigure();
			if (!drawingView.getSelectedFigures().contains(figure)) {
				drawingView.addToSelection(figure);
			}
		}
	}

	@EventHandler
	protected void onViewDeselected(final DataViewDeselectedEvent event) {
		final DataView view = event.getView();
		if (display.contains(view) && view instanceof FigureView) {
			final Figure figure = ((FigureView) view).getFigure();
			if (drawingView.getSelectedFigures().contains(figure)) {
				drawingView.removeFromSelection(figure);
			}
		}
	}

	@EventHandler
	protected void onToolActivatedEvent(final ToolActivatedEvent event) {
		final Tool iTool = event.getTool();
		activateTool(iTool);
	}

	protected void activateTool(final Tool iTool) {
		if (iTool instanceof IJHotDrawOverlayAdapter) {
			final IJHotDrawOverlayAdapter adapter = (IJHotDrawOverlayAdapter) iTool;
			final IJCreationTool creationTool = new IJCreationTool(display, adapter);

			// When the tool creates an overlay, add the
			// overlay/figure combo to a SwingOverlayView.
			creationTool
				.addOverlayCreatedListener(new IJCreationTool.OverlayCreatedListener() {

					@SuppressWarnings("synthetic-access")
					@Override
					public void overlayCreated(final FigureCreatedEvent e) {
						final OverlayView overlay = e.getOverlay();
						for (int i = 0; i < display.numDimensions(); i++) {
							final AxisType axis = display.axis(i);
							if (Axes.isXY(axis)) continue;
							if (overlay.getData().getAxisIndex(axis) < 0) {
								overlay.setPosition(display.getLongPosition(axis), axis);
							}
						}
						display.add(overlay);
						display.update();
						if (drawingView.getSelectedFigures().contains(e.getFigure())) {
							overlay.setSelected(true);
						}
					}
				});
			toolDelegator.setCreationTool(creationTool);
		}
		else {
			toolDelegator.setCreationTool(null);
		}
	}

	// -- JHotDrawImageCanvas methods --

	public Drawing getDrawing() {
		return drawing;
	}

	public DefaultDrawingView getDrawingView() {
		return drawingView;
	}

	public DrawingEditor getDrawingEditor() {
		return drawingEditor;
	}

	public void addEventDispatcher(final AWTKeyEventDispatcher dispatcher) {
		drawingView.addKeyListener(dispatcher);
	}

	public void addEventDispatcher(final AWTMouseEventDispatcher dispatcher) {
		drawingView.addMouseListener(dispatcher);
		drawingView.addMouseMotionListener(dispatcher);
		drawingView.addMouseWheelListener(dispatcher);
	}

	// -- ImageCanvas methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public Dimension getPreferredSize() {
		// HACK: Size the canvas a few pixels larger. This is a workaround to an
		// apparent bug in JHotDraw, where an ImageFigure is initially drawn as a
		// large X until it is finished being rendered. Unfortunately, the X is
		// slightly smaller than the image after being rendered.
		final int w = drawingView.getPreferredSize().width + 5;
		final int h = drawingView.getPreferredSize().height + 5;
		return new Dimension(w, h);
	}

	@Override
	public int getCanvasWidth() {
		// NB: Return *unscaled* canvas width.
		return display.getActiveView().getPreferredWidth();
	}

	@Override
	public int getCanvasHeight() {
		// NB: Return *unscaled* canvas height.
		return display.getActiveView().getPreferredHeight();
	}

	@Override
	public int getViewportWidth() {
		return drawingView.getWidth();
	}

	@Override
	public int getViewportHeight() {
		return drawingView.getHeight();
	}

	@Override
	public boolean isInImage(final IntCoords point) {
		return canvasHelper.isInImage(point);
	}

	@Override
	public RealCoords panelToImageCoords(final IntCoords panelCoords) {
		return canvasHelper.panelToImageCoords(panelCoords);
	}

	@Override
	public IntCoords imageToPanelCoords(final RealCoords imageCoords) {
		final Point2D.Double drawCoords =
			new Point2D.Double(imageCoords.x, imageCoords.y);
		final Point viewCoords = drawingView.drawingToView(drawCoords);
		return new IntCoords(viewCoords.x, viewCoords.y);
	}

	@Override
	public void setCursor(final MouseCursor cursor) {
		drawingView.setCursor(AWTCursors.getCursor(cursor));
	}

	// -- Pannable methods --

	@Override
	public void pan(final IntCoords delta) {
		canvasHelper.pan(delta);
		syncPan();
	}

	@Override
	public void setPan(final IntCoords origin) {
		canvasHelper.setPan(origin);
		syncPan();
	}

	@Override
	public void panReset() {
		canvasHelper.panReset();
	}

	@Override
	public IntCoords getPanOrigin() {
		return canvasHelper.getPanOrigin();
	}

	// -- Zoomable methods --

	@Override
	public void setZoom(final double factor) {
		canvasHelper.setZoom(factor);
	}

	@Override
	public void setZoom(final double factor, final IntCoords center) {
		canvasHelper.setZoom(factor, center);
		syncZoom();
		syncPan();
	}

	@Override
	public void zoomIn() {
		canvasHelper.zoomIn();
	}

	@Override
	public void zoomIn(final IntCoords center) {
		canvasHelper.zoomIn(center);
	}

	@Override
	public void zoomOut() {
		canvasHelper.zoomOut();
	}

	@Override
	public void zoomOut(final IntCoords center) {
		canvasHelper.zoomOut(center);
	}

	@Override
	public void zoomToFit(final IntCoords topLeft, final IntCoords bottomRight) {
		canvasHelper.zoomToFit(topLeft, bottomRight);
	}

	void setInitialScale(final double value) {
		canvasHelper.setInitialScale(value);
	}

	@Override
	public double getZoomFactor() {
		return canvasHelper.getZoomFactor();
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		final Point viewPos = scrollPane.getViewport().getViewPosition();
		canvasHelper.setPan(new IntCoords(viewPos.x, viewPos.y));
	}

	// -- Helper methods --

	private void syncPan() {
		final Point viewPos = scrollPane.getViewport().getViewPosition();
		final IntCoords origin = canvasHelper.getPanOrigin();
		if (viewPos.x == origin.x && viewPos.y == origin.y) return; // no change
		constrainOrigin(origin);
		scrollPane.getViewport().setViewPosition(new Point(origin.x, origin.y));
	}

	private void syncZoom() {
		final double startScale = drawingView.getScaleFactor();
		final double endScale = canvasHelper.getZoomFactor();
		final IntCoords origin = canvasHelper.getPanOrigin();
		drawingView.setScaleFactor(endScale);
		scrollPane.validate();
		canvasHelper.setPan(origin);
		maybeResizeWindow(startScale, endScale);
	}

	private void constrainOrigin(final IntCoords origin) {
		if (origin.x < 0) origin.x = 0;
		if (origin.y < 0) origin.y = 0;
		final Dimension viewportSize = scrollPane.getViewport().getSize();
		final Dimension canvasSize = drawingView.getSize();
		final int xMax = canvasSize.width - viewportSize.width;
		final int yMax = canvasSize.height - viewportSize.height;
		if (origin.x > xMax) origin.x = xMax;
		if (origin.y > yMax) origin.y = yMax;
	}

	private void
		maybeResizeWindow(final double startScale, final double endScale)
	{
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final double nextWidth = drawingView.getWidth() * endScale / startScale;
		final double nextHeight = drawingView.getHeight() * endScale / startScale;
		if (nextWidth > screenSize.width - 64) return;
		if (nextHeight > screenSize.height - 64) return;
		display.getPanel().getWindow().pack();
	}
}
