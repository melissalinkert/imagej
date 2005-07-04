package ij.process;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import ij.gui.*;

/** This is an 32-bit floating-point image and methods that operate on that image. */
public class FloatProcessor extends ImageProcessor {

	private float min, max, snapshotMin, snapshotMax;
	private float[] pixels;
	private byte[] pixels8;
	private float[] snapshotPixels = null;
	private byte[] LUT = null;
	private float fillColor =  Float.MAX_VALUE;
	//private float bgColor = Float.MIN_VALUE;
	private boolean fixedScale = false;

	/** Creates a new FloatProcessor using the specified pixel array and ColorModel.
		Set 'cm' to null to use the default grayscale LUT. */
	public FloatProcessor(int width, int height, float[] pixels, ColorModel cm) {
		this.width = width;
		this.height = height;
		this.pixels = pixels;
		this.cm = cm;
		setRoi(null);
		findMinAndMax();
	}

	/** Creates a blank FloatProcessor using the default grayscale LUT that
		displays zero as black. Call invertLut() to display zero as white. */
	public FloatProcessor(int width, int height) {
		this(width, height, new float[width*height], null);
	}

	/**
	Calculates the minimum and maximum pixel value for the entire image. 
	Returns without doing anything if fixedScale has been set true as a result
	of calling setMinAndMax(). In this case, getMin() and getMax() return the
	fixed min and max defined by setMinAndMax(), rather than the calculated min
	and max.
	@see getMin()
	@see getMin()
	*/
	public void findMinAndMax() {
		if (fixedScale)
			return;
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		for (int i=0; i < width*height; i++) {
			float value = pixels[i];
			if (value<min)
				min = value;
			if (value>max)
				max = value;
		}
		pixelsModified = true;
		hideProgress();
	}

	/**
	Sets the min and max variables that control how real
	pixel values are mapped to 0-255 screen values. Use
	resetMinAndMax() to enable auto-scaling;
	@see ij.plugin.frame.ContrastAdjuster 
	*/
	public void setMinAndMax(double min, double max) {
		if (min==0.0 && max==0.0)
			{resetMinAndMax(); return;}
		this.min = (float)min;
		this.max = (float)max;
		fixedScale = true;
		setThreshold(NO_THRESHOLD,0,0);
	}

	/** Recalculates the min and max values used to scale pixel
		values to 0-255 for display. This ensures that this 
		FloatProcessor is set up to correctly display the image. */
	public void resetMinAndMax() {
		fixedScale = false;
		findMinAndMax();
		setThreshold(NO_THRESHOLD,0,0);
	}

	/** Returns the smallest displayed pixel value. */
	public double getMin() {
		return min;
	}

	/** Returns the largest displayed pixel value. */
	public double getMax() {
		return max;
	}

	public Image createImage() {
		boolean rescale = !lutAnimation || pixels8==null;
		lutAnimation = false;
		if (pixels8==null)
			pixels8 = new byte[width*height];
		if (cm==null)
			makeDefaultColorModel();
		float value;
		int ivalue;
		float scale = 255f/(max-min);
		if (rescale)
			for (int i=0; i<width*height; i++) {
				value = pixels[i]-min;
				if (value<0f) value = 0f;
				ivalue = (int)(value*scale);
				if (ivalue>255) ivalue = 255;
				pixels8[i] = (byte)ivalue;
			}
	    ImageProducer p = new MemoryImageSource(width, height, cm, pixels8, 0, width);
	    return Toolkit.getDefaultToolkit().createImage(p);
	}
	
	/** Returns a new, blank FloatProcessor with the specified width and height. */
	public ImageProcessor createProcessor(int width, int height) {
		ImageProcessor ip2 = new FloatProcessor(width, height, new float[width*height], getColorModel());
		ip2.setMinAndMax(getMin(), getMax());
		return ip2;
	}

	public void snapshot() {
		snapshotWidth=width;
		snapshotHeight=height;
		snapshotMin=min;
		snapshotMax=max;
		if (snapshotPixels==null || (snapshotPixels!=null && snapshotPixels.length!=pixels.length))
			snapshotPixels = new float[width * height];
        System.arraycopy(pixels, 0, snapshotPixels, 0, width*height);
        pixelsModified = false;
		newSnapshot = true;
	}
	
	public void reset() {
		if (snapshotPixels==null)
			return;
	    min=snapshotMin;
		max=snapshotMax;
        System.arraycopy(snapshotPixels,0,pixels,0,width*height);
	}
	
	public void reset(int[] mask) {
		if (mask==null || snapshotPixels==null || mask.length!=roiWidth*roiHeight)
			return;
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				if (mask[mi++]!=BLACK)
					pixels[i] = snapshotPixels[i];
				i++;
			}
		}
        newSnapshot = true;
	}

	/** Returns a pixel value that must be converted using
		Float.intBitsToFloat(). */
	public int getPixel(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return Float.floatToIntBits(pixels[y*width + x]);
		else
			return 0;
	}

	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	public double getInterpolatedPixel(double x, double y) {
		int basex = (int)x;
		int basey = (int)y;
		if (basex>=0 && (basex+1)<width && basey>=0 && (basey+1)<height)
			return getInterpolatedPixel(x, y, pixels);
		else
			return 0f;
	}

	/** Stores the specified value at (x,y). The value is expected to be a
		float that has been converted to an int using Float.floatToIntBits(). */
	public void putPixel(int x, int y, int value) {
		if (x>=0 && x<width && y>=0 && y<height)
			pixels[y*width + x] = Float.intBitsToFloat(value);
	}

	/** Stores the specified real value at (x,y). */
	public void putPixelValue(int x, int y, double value) {
		if (x>=0 && x<width && y>=0 && y<height)
			pixels[y*width + x] = (float)value;
	}

	public float getPixelValue(int x, int y) {
		if (x>=0 && x<width && y>=0 && y<height)
			return pixels[y*width + x];
		else
			return 0f;
	}

	/** Draws a pixel in the current foreground color. */
	public void drawPixel(int x, int y) {
		putPixel(x, y, Float.floatToIntBits(fillColor));
	}

	/**	Returns a reference to the float array containing
		this image's pixel data. */
	public Object getPixels() {
		return (Object)pixels;
	}

	public Object getPixelsCopy() {
		if (newSnapshot)
			return snapshotPixels;
		else {
			float[] pixels2 = new float[width*height];
        	System.arraycopy(pixels, 0, pixels2, 0, width*height);
			return pixels2;
		}
	}

	public void setPixels(Object pixels) {
		this.pixels = (float[])pixels;
		snapshotPixels = null;
	}

	/** Copies the image contained in 'ip' to (xloc, yloc) using one of
		the transfer modes defined in the Blitter interface. */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		if (!(ip instanceof FloatProcessor))
			throw new IllegalArgumentException("32-bit (real) image required");
		new FloatBlitter(this).copyBits(ip, xloc, yloc, mode);
	}

	public void applyTable(int[] lut) {}

	private float[] getCopyOfPixels() {
		if (pixelsModified) {
			float[] pixelsCopy = new float[width * height];
	        System.arraycopy(pixels, 0, pixelsCopy, 0, width*height);
			return pixelsCopy;
		}
		else
			return snapshotPixels;
	}
	
	private void process(int op, double value) {
		float c, v1, v2;
		
		c = (float)value;
		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				v1 = pixels[i];
				switch(op) {
					case INVERT:
						v2 = max - (v1 - min);
						break;
					case FILL:
						v2 = fillColor;
						break;
					case ADD:
						v2 = v1 + c;
						break;
					case MULT:
						v2 = v1 * c;
						break;
					case GAMMA:
						if (v1<=0f)
							v2 = 0f;
						else
							v2 = (float)Math.exp(c*Math.log(v1));
						break;
					case LOG:
						if (v1<=0f)
							v2 = 0f;
						else
							v2 = (float)Math.log(v1);
						break;
					case MINIMUM:
						if (v1<value)
							v2 = (int)value;
						else
							v2 = v1;
						break;
					case MAXIMUM:
						if (v1>value)
							v2 = (int)value;
						else
							v2 = v1;
						break;
					 default:
					 	v2 = v1;
				}
				pixels[i++] = v2;
			}
			if (y%20==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		findMinAndMax();
    }

	public void invert() {process(INVERT, 0.0);}
	public void add(int value) {process(ADD, value);}
	public void add(double value) {process(ADD, value);}
	public void multiply(double value) {process(MULT, value);}
	public void and(int value) {}
	public void or(int value) {}
	public void xor(int value) {}
	public void gamma(double value) {process(GAMMA, value);}
	public void log() {process(LOG, 0.0);}
	public void min(double value) {process(MINIMUM, value);}
	public void max(double value) {process(MAXIMUM, value);}



	/** Fills the current rectangular ROI. */
	public void fill() {process(FILL, 0.0);}

	/** Fills pixels that are within roi and part of the mask. */
	public void fill(int[] mask) {
		for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
			int i = y * width + roiX;
			int mi = my * roiWidth;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				if (mask[mi++]==BLACK)
					pixels[i] = fillColor;
				i++;
			}
		}
	}

	/** 3x3 convolution contributed by Glynne Casteel. */
	public void convolve3x3(int[] kernel) {
		float p1, p2, p3, p4, p5, p6, p7, p8, p9;
		float k1=kernel[0], k2=kernel[1], k3=kernel[2],
		      k4=kernel[3], k5=kernel[4], k6=kernel[5],
		      k7=kernel[6], k8=kernel[7], k9=kernel[8];

		float scale = 0f;
		for (int i=0; i<kernel.length; i++)
			scale += kernel[i];
		if (scale==0) scale = 1f;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		float[] pixels2 = (float[])getPixelsCopy();
		int offset;
		float sum;
        int rowOffset = width;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p1 = 0f;
			p2 = pixels2[offset-rowOffset-1];
			p3 = pixels2[offset-rowOffset];
			p4 = 0f;
			p5 = pixels2[offset-1];
			p6 = pixels2[offset];
			p7 = 0f;
			p8 = pixels2[offset+rowOffset-1];
			p9 = pixels2[offset+rowOffset];

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1];
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1];
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1];
				sum = k1*p1 + k2*p2 + k3*p3
				    + k4*p4 + k5*p5 + k6*p6
				    + k7*p7 + k8*p8 + k9*p9;
				sum /= scale;
				pixels[offset++] = sum;
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		hideProgress();
	}

	/** Filters using a 3x3 neighborhood. */
	public void filter(int type) {
		float p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int inc = roiHeight/25;
		if (inc<1) inc = 1;
		
		float[] pixels2 = (float[])getPixelsCopy();
		int offset;
		float sum1, sum2;
        int rowOffset = width;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p1 = 0f;
			p2 = pixels2[offset-rowOffset-1];
			p3 = pixels2[offset-rowOffset];
			p4 = 0f;
			p5 = pixels2[offset-1];
			p6 = pixels2[offset];
			p7 = 0f;
			p8 = pixels2[offset+rowOffset-1];
			p9 = pixels2[offset+rowOffset];

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1];
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1];
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1];

				switch (type) {
					case BLUR_MORE:
						pixels[offset++] = (p1+p2+p3+p4+p5+p6+p7+p8+p9)/9f;
						break;
					case FIND_EDGES:
	        			sum1 = p1 + 2*p2 + p3 - p7 - 2*p8 - p9;
	        			sum2 = p1  + 2*p4 + p7 - p3 - 2*p6 - p9;
	        			pixels[offset++] = (float)Math.sqrt(sum1*sum1 + sum2*sum2);
	        			break;
				}
			}
			if (y%inc==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		if (type==BLUR_MORE)
			hideProgress();
		else
			findMinAndMax();
	}

	/** Rotates the image or ROI 'angle' degrees clockwise.
		@see ImageProcessor#setInterpolate
	*/
	public void rotate(double angle) {
		float[] pixels2 = (float[])getPixelsCopy();
		double centerX = roiX + roiWidth/2.0;
		double centerY = roiY + roiHeight/2.0;
		int xMax = roiX + this.roiWidth - 1;
		
		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index, xsi, ysi;
		
		for (int y=roiY; y<(roiY + roiHeight); y++) {
			index = y*width + roiX;
			tmp3 = tmp1 - y*sa + centerX;
			tmp4 = tmp2 + y*ca + centerY;
			for (int x=roiX; x<=xMax; x++) {
				xs = x*ca + tmp3;
				ys = x*sa + tmp4;
				if ((xs>=0.0) && (xs<width) && (ys>=0.0) && (ys<height)) {
					if (interpolate)
				  		pixels[index++] = (float)(getInterpolatedPixel(xs, ys, pixels2)+0.5);
				  	else
						pixels[index++] = pixels2[width*(int)ys+(int)xs];
    			} else
					pixels[index++] = 0;
			}
			if (y%20==0)
			showProgress((double)(y-roiY)/roiHeight);
		}
		hideProgress();
	}

    public void noise(double range) {
		Random rnd=new Random();

		for (int y=roiY; y<(roiY+roiHeight); y++) {
			int i = y * width + roiX;
			for (int x=roiX; x<(roiX+roiWidth); x++) {
				float RandomBrightness = (float)(rnd.nextGaussian()*range);
				pixels[i] = pixels[i] + RandomBrightness;
				i++;
			}
			if (y%20==0)
				showProgress((double)(y-roiY)/roiHeight);
		}
		findMinAndMax();
    }

	public ImageProcessor crop() {
		ImageProcessor ip2 = createProcessor(roiWidth, roiHeight);
		float[] pixels2 = (float[])ip2.getPixels();
		for (int ys=roiY; ys<roiY+roiHeight; ys++) {
			int offset1 = (ys-roiY)*roiWidth;
			int offset2 = ys*width+roiX;
			for (int xs=0; xs<roiWidth; xs++)
				pixels2[offset1++] = pixels[offset2++];
		}
        return ip2;
	}
	
	/** Scales the image or selection using the specified scale factors.
		@see ImageProcessor#setInterpolate
	*/
	public void scale(double xScale, double yScale) {
		double xCenter = roiX + roiWidth/2.0;
		double yCenter = roiY + roiHeight/2.0;
		int xmin, xmax, ymin, ymax;
		
		if ((xScale>1.0) && (yScale>1.0)) {
			//expand roi
			xmin = (int)(xCenter-(xCenter-roiX)*xScale);
			if (xmin<0) xmin = 0;
			xmax = xmin + (int)(roiWidth*xScale) - 1;
			if (xmax>=width) xmax = width - 1;
			ymin = (int)(yCenter-(yCenter-roiY)*yScale);
			if (ymin<0) ymin = 0;
			ymax = ymin + (int)(roiHeight*yScale) - 1;
			if (ymax>=height) ymax = height - 1;
		} else {
			xmin = roiX;
			xmax = roiX + roiWidth - 1;
			ymin = roiY;
			ymax = roiY + roiHeight - 1;
		}
		float[] pixels2 = (float[])getPixelsCopy();
		boolean checkCoordinates = (xScale < 1.0) || (yScale < 1.0);
		int index1, index2, xsi, ysi;
		double ys, xs;
		for (int y=ymin; y<=ymax; y++) {
			ys = (y-yCenter)/yScale + yCenter;
			ysi = (int)ys;
			index1 = y*width + xmin;
			index2 = width*(int)ys;
			for (int x=xmin; x<=xmax; x++) {
				xs = (x-xCenter)/xScale + xCenter;
				xsi = (int)xs;
				if (checkCoordinates && ((xsi<xmin) || (xsi>xmax) || (ysi<ymin) || (ys>ymax)))
					pixels[index1++] = (float)min;
				else {
					if (interpolate)
						pixels[index1++] = (float)getInterpolatedPixel(xs, ys, pixels2);
					else
						pixels[index1++] = pixels2[index2+xsi];
				}
			}
			if (y%20==0)
			showProgress((double)(y-ymin)/height);
		}
		hideProgress();
	}

	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
	private final double getInterpolatedPixel(double x, double y, float[] pixels) {
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		int offset = ybase * width + xbase;
		double lowerLeft = pixels[offset];
		if ((xbase>=(width-1))||(ybase>=(height-1)))
			return lowerLeft;
		double lowerRight = pixels[offset + 1];
		double upperRight = pixels[offset + width + 1];
		double upperLeft = pixels[offset + width];
		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}

	/** Creates a new FloatProcessor containing a scaled copy of this image or selection. */
	public ImageProcessor resize(int dstWidth, int dstHeight) {
		double srcCenterX = roiX + roiWidth/2.0;
		double srcCenterY = roiY + roiHeight/2.0;
		double dstCenterX = dstWidth/2.0;
		double dstCenterY = dstHeight/2.0;
		double xScale = (double)dstWidth/roiWidth;
		double yScale = (double)dstHeight/roiHeight;
		ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
		float[] pixels2 = (float[])ip2.getPixels();
		double xs, ys=0.0;
		int index1, index2;
		for (int y=0; y<=dstHeight-1; y++) {
			index1 = width*(int)ys;
			index2 = y*dstWidth;
			ys = (y-dstCenterY)/yScale + srcCenterY;
			for (int x=0; x<=dstWidth-1; x++) {
				xs = (x-dstCenterX)/xScale + srcCenterX;
				if (interpolate)
					pixels2[index2++] = (float)getInterpolatedPixel(xs, ys, pixels);
				else
		  			pixels2[index2++] = pixels[index1+(int)xs];
			}
			if (y%20==0)
			showProgress((double)y/dstHeight);
		}
		hideProgress();
		return ip2;
	}

	/** Sets the foreground fill/draw color. */
	public void setColor(Color color) {
		int bestIndex = getBestIndex(color);
		if (bestIndex>0 && getMin()==0.0 && getMax()==0.0) {
			fillColor = bestIndex;
			setMinAndMax(0.0,255.0);
		} else if (bestIndex==0 && getMin()>0.0 && (color.getRGB()&0xffffff)==0)
			fillColor = 0f;
		else
			fillColor = (float)(min + (max-min)*(bestIndex/255.0));
	}
	
	/** Sets the default fill/draw value. */
	public void setValue(double value) {
		fillColor = (float)value;
	}

	public void setThreshold(double minThreshold, double maxThreshold, int lutUpdate) {
		if (minThreshold!=NO_THRESHOLD && max>min) {
			double minT = ((minThreshold-min)/(max-min))*255.0;
			double maxT = ((maxThreshold-min)/(max-min))*255.0;
			super.setThreshold(minT, maxT, lutUpdate);
			this.minThreshold = minThreshold;
			this.maxThreshold = maxThreshold;
		} else
			super.setThreshold(NO_THRESHOLD, 0, 0);
	}

	public void threshold(int level) {}
	public void autoThreshold() {}
	public void medianFilter() {}
	public int[] getHistogram() {return null;}
	public void erode() {}
	public void dilate() {}

}

