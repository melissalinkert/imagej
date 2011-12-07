//
// OptionsOverlay.java
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

package imagej.options.plugins;

import imagej.data.display.OverlayService;
import imagej.data.overlay.OverlaySettings;
import imagej.data.overlay.Overlay.ArrowStyle;
import imagej.data.overlay.Overlay.LineStyle;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.util.ColorRGB;
import imagej.util.Colors;

/**
 * Runs the Edit::Options::Overlay... dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Overlay", mnemonic = 'o'),
	@Menu(label = "Overlay Options...")}, label="Default Overlay Settings")
public class OptionsOverlay extends OptionsPlugin {

	// -- public statics --
	
	static final public String solidLineStyle = "Solid";
	static final public String dashLineStyle = "Dash";
	static final public String dotLineStyle = "Dot";
	static final public String dotDashLineStyle = "Dot-dash";
	static final public String noLineStyle = "None";
	static final public String arrowLineDecoration = "Arrow";
	static final public String noLineDecoration = "None";

	// -- private statics --
	
	@Parameter(required = true, persist = false)
	private OverlayService overlayService;

	@Parameter(label = "Line color")
	private ColorRGB lineColor = Colors.YELLOW;

	@Parameter(label = "Line width", min = "0.1")
	private double lineWidth = 1;

	@Parameter(label = "Line style", choices = { solidLineStyle,
		dashLineStyle, dotLineStyle, dotDashLineStyle, noLineStyle })
	private String lineStyle = solidLineStyle;

	@Parameter(label = "Fill color")
	private ColorRGB fillColor = Colors.YELLOW;

	@Parameter(label = "Fill opacity", description = "The opacity or alpha of the "
		+ "interior of the overlay (0=transparent, 255=opaque)",
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = "255")
	private int alpha = 0;

	@Parameter(
		label = "Line start arrow style",
		description = "The arrow style at the starting point of a line or other path",
		choices = { noLineDecoration, arrowLineDecoration })
	private String startLineArrowStyle = noLineDecoration;

	@Parameter(label = "Line end arrow style",
		description = "The arrow style at the end point of a line or other path",
		choices = { noLineDecoration, arrowLineDecoration })
	private String endLineArrowStyle = noLineDecoration;

	// -- public interface --

	public OptionsOverlay() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	@Override
	public void run() {
		final OverlaySettings settings = overlayService.getDefaultSettings();
		settings.setLineColor(getLineColor());
		settings.setLineWidth(getLineWidth());
		settings.setLineStyle(getLineStyle());
		settings.setFillColor(getFillColor());
		settings.setAlpha(getAlpha());
		settings.setStartArrowStyle(getStartArrowStyle());
		settings.setEndArrowStyle(getEndArrowStyle());
		super.run();
	}
	
	public void setLineColor(ColorRGB color) {
		lineColor = color;
	}

	public ColorRGB getLineColor() {
		return lineColor;
	}
	
	public void setLineWidth(double width) {
		this.lineWidth = width;
	}
	
	public double getLineWidth() {
		return lineWidth;
	}
	
	public void setLineStyle(LineStyle style) {
		switch (style) {
			case DASH: lineStyle = dashLineStyle; break;
			case DOT: lineStyle = dotLineStyle; break;
			case DOT_DASH: lineStyle = dotDashLineStyle; break;
			case SOLID: lineStyle = solidLineStyle; break;
			default: lineStyle = noLineStyle; break;
		}
	}
	
	public LineStyle getLineStyle() {
		if (lineStyle.equals(dashLineStyle)) return LineStyle.DASH;
		if (lineStyle.equals(dotLineStyle)) return LineStyle.DOT;
		if (lineStyle.equals(dotDashLineStyle)) return LineStyle.DOT_DASH;
		if (lineStyle.equals(solidLineStyle)) return LineStyle.SOLID;
		return LineStyle.NONE;
	}

	public void setFillColor(ColorRGB color) {
		fillColor = color;
	}
	
	public ColorRGB getFillColor() {
		return fillColor;
	}
	
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	public void setStartArrowStyle(ArrowStyle style) {
		if (style == ArrowStyle.ARROW)
			startLineArrowStyle = arrowLineDecoration;
		else
			startLineArrowStyle = noLineDecoration;
	}
	
	public ArrowStyle getStartArrowStyle() {
		if (startLineArrowStyle.equals(arrowLineDecoration))
			return ArrowStyle.ARROW;
		return ArrowStyle.NONE;
	}
	
	public void setEndArrowStyle(ArrowStyle style) {
		if (style == ArrowStyle.ARROW)
			endLineArrowStyle = arrowLineDecoration;
		else
			endLineArrowStyle = noLineDecoration;
	}
	
	public ArrowStyle getEndArrowStyle() {
		if (endLineArrowStyle.equals(arrowLineDecoration))
			return ArrowStyle.ARROW;
		return ArrowStyle.NONE;
	}
}
