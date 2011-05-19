//
// AbstractShapeROI.java
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
package imagej.data.roi;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.roi.RegionOfInterest;


/**
 * @author leek
 *
 * The shape ROI represents a region of interest with an interior.
 *
 * @param <T> - the region of interest type returned by getShapeRegionOfInterest
 */
public abstract class AbstractShapeOverlay <T extends RegionOfInterest> extends AbstractLineOverlay {

	private static final long serialVersionUID = 1L;
	
	protected Color fillColor = new Color(255,255,255);

	protected double opacity = 1.0;

	/**
	 * @return the color to be used to paint the interior of the shape
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * @param fillColor the color of the interior of the shape
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * @return the opacity of the fill (0 = transparent, 1 = opaque
	 */
	public double getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity the opacity of the fill (0 = transparent, 1 = opaque
	 */
	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}
	
	/**
	 * @return the region of interest cast to the derived type.
	 */
	abstract public T getShapeRegionOfInterest();
	
	/* (non-Javadoc)
	 * @see imagej.roi.ImageJROI#getRegionOfInterest()
	 */
	@Override
	public RegionOfInterest getRegionOfInterest() {
		return getShapeRegionOfInterest();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(fillColor);
		out.writeDouble(opacity);
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		fillColor = (Color)in.readObject();
		opacity = in.readDouble();
	}
}