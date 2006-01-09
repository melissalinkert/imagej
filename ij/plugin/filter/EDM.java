package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.gui.*;

/**
	This plugin implements the Euclidean Distance Map (EDM), Ultimate Eroded Points and
	Watershed commands in the Process/Binary submenu.
*/
public class EDM implements PlugInFilter {

	ImagePlus imp;
	String arg;
	int maxEDM;
	short[] xCoordinate, yCoordinate;
	int[] levelStart;
	int[] levelOffset;
	int[] histogram;
	int slice;
	int count;
	boolean watershed;
	ImageWindow win;
	boolean canceled;
	ImageStack movie;
	boolean debug = IJ.debugMode;
	boolean invertImage;
	static boolean whiteBackground = true;
	static boolean smoothEDM = true;	

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		this.arg = arg;
		watershed = arg.equals("watershed");
		boolean invertedLut = imp.isInvertedLut();
		invertImage = (invertedLut && Prefs.blackBackground) || (!invertedLut && !Prefs.blackBackground);
        return IJ.setupDialog(imp, DOES_8G);
	}
	
	public void run(ImageProcessor ip) {
		slice++;
		win = imp.getWindow();
		if (win!=null) win.running = true;
		ImageStatistics stats = imp.getStatistics();
		if (slice==1 && stats.histogram[0]+stats.histogram[255]!=stats.pixelCount) {
			IJ.error("8-bit binary image (0 and 255) required.");
			return;
		}
		if (invertImage)
			ip.invert();
		ImageProcessor ip2 = makeEDM(ip);
		if (arg.equals("points"))
			findUltimatePoints(ip2);
		else if (watershed) {
			findUltimatePoints(ip2);
			doWatershed(ip2);
		}
		IJ.showProgress(1.0);
		if (!canceled) {
			ip.copyBits(ip2, 0, 0, Blitter.COPY);
			if (invertImage)
				ip.invert();
		}
	}



	/**	Converts a binary image into a grayscale Euclidean Distance Map
		(EDM). Each foreground (black) pixel in the binary image is
		assigned a value equal to its distance from the nearest
		background (white) pixel.  Uses the two-pass EDM algorithm
		from the "Image Processing Handbook" by John Russ.
	*/
	public ImageProcessor makeEDM (ImageProcessor ip) {
		int  one = 41;
		int  sqrt2 = 58; // ~ 41 * sqrt(2)
		int  sqrt5 = 92; // ~ 41 * sqrt(5)
		int xmax, ymax;
		int offset, rowsize;

		IJ.showStatus("Generating EDM");
		imp.killRoi();
		int width = imp.getWidth();
		int height = imp.getHeight();
		rowsize = width;
		xmax    = width - 3;
		ymax    = height - 3;
		ImageProcessor ip16 = ip.convertToShort(false);
		ip16.multiply(one);
		short[] image16 = (short[])ip16.getPixels();
 
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				offset = x + y * rowsize;
				if (image16[offset] > 0) {
					if ((x<2) || (x>xmax) || (y<2) || (y>ymax))
						setEdgeValue(offset, rowsize, image16, x, y, xmax, ymax);
					else
 						setValue(offset, rowsize, image16);
				}
			} // for x
		} // for y

		for (int y=height-1; y>=0; y--) {
			for (int x=width-1; x>=0; x--) {
 				offset = x + y * rowsize;
				if (image16[offset] > 0) {
					if ((x<2) || (x>xmax) || (y<2) || (y>ymax))
						setEdgeValue (offset, rowsize, image16, x, y, xmax, ymax);
					else
						setValue (offset, rowsize, image16);
				}
			} // for x
 		} // for y
		//(new ImagePlus("EDM16", ip16.duplicate())).show();

		ImageProcessor ip2 = ip.createProcessor(width, height);
		byte[] image8 = (byte[])ip2.getPixels();
		convertToBytes(width, height, image16, image8);
		return ip2;
 	} // makeEDM()

	void setValue (int offset, int rowsize, short[] image16) {
 		int  one = 41;
		int  sqrt2 = 58; // ~ 41 * sqrt(2)
		int  sqrt5 = 92; // ~ 41 * sqrt(5)
		int  v;
		int r1  = offset - rowsize - rowsize - 2;
		int r2  = r1 + rowsize;
		int r3  = r2 + rowsize;
		int r4  = r3 + rowsize;
		int r5  = r4 + rowsize;
		int min = 32767;

		v = image16[r2 + 2] + one;
		if (v < min)
			min = v;
		v = image16[r3 + 1] + one;
		if (v < min)
			min = v;
		v = image16[r3 + 3] + one;
		if (v < min)
			min = v;
		v = image16[r4 + 2] + one;
		if (v < min)
			min = v;
			
		v = image16[r2 + 1] + sqrt2;
		if (v < min)
			min = v;
		v = image16[r2 + 3] + sqrt2;
		if (v < min)
			min = v;
		v = image16[r4 + 1] + sqrt2;
		if (v < min)
			min = v;
 		v = image16[r4 + 3] + sqrt2;
		if (v < min)
			min = v;

		v = image16[r1 + 1] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r1 + 3] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r2 + 4] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r4 + 4] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r5 + 3] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r5 + 1] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r4] + sqrt5;
		if (v < min)
			min = v;
		v = image16[r2] + sqrt5;
		if (v < min)
			min = v;

		image16[offset] = (short)min;

	} // setValue()

	void setEdgeValue (int offset, int rowsize, short[] image16, int x, int y, int xmax, int ymax) {
		int  one   = 41;
		int  sqrt2 = 58; // ~ 41 * sqrt(2)
		int  sqrt5 = 92; // ~ 41 * sqrt(5)
		int  v;
		int r1 = offset - rowsize - rowsize - 2;
		int r2 = r1 + rowsize;
		int r3 = r2 + rowsize;
		int r4 = r3 + rowsize;
		int r5 = r4 + rowsize;
		int min = 32767;
		int offimage = image16[r3 + 2];

		if (y<2)
			v = offimage + one;
		else
			v = image16[r2 + 2] + one;
		if (v < min)
			min = v;

		if (x<2)
			v = offimage + one;
		else
			v = image16[r3 + 1] + one;
		if (v < min)
			min = v;

		if (x>xmax)
			v = offimage + one;
		else
			v = image16[r3 + 3] + one;
		if (v < min)
			min = v;

		if (y>ymax)
			v = offimage + one;
		else
 			v = image16[r4 + 2] + one;
		if (v < min)
			min = v;

		if ((x<2) || (y<2))
			v = offimage + sqrt2;
		else
			v = image16[r2 + 1] + sqrt2;
		if (v < min)
			min = v;

		if ((x>xmax) || (y<2))
			v = offimage + sqrt2;
		else
			v = image16[r2 + 3] + sqrt2;
		if (v < min)
			min = v;

		if ((x<2) || (y>ymax))
 			v = offimage + sqrt2;
		else
			v = image16[r4 + 1] + sqrt2;
		if (v < min)
			min = v;

		if ((x>xmax) || (y>ymax))
			v = offimage + sqrt2;
		else
			v = image16[r4 + 3] + sqrt2;
		if (v < min)
			min = v;

		if ((x<2) || (y<2))
			v = offimage + sqrt5;
		else
			v = image16[r1 + 1] + sqrt5;
		if (v < min)
			min = v;

		if ((x>xmax) || (y<2))
			v = offimage + sqrt5;
		else
			v = image16[r1 + 3] + sqrt5;
		if (v < min)
			min = v;

		if ((x>xmax) || (y<2))
			v = offimage + sqrt5;
 		else
 			v = image16[r2 + 4] + sqrt5;
		if (v < min)
 			min = v;

		if ((x>xmax) || (y>ymax))
			v = offimage + sqrt5;
		else
			v = image16[r4 + 4] + sqrt5;
		if (v < min)
			min = v;

		if ((x>xmax) || (y>ymax))
			v = offimage + sqrt5;
		else
 			v = image16[r5 + 3] + sqrt5;
		if (v < min)
			min = v;

		if ((x<2) || (y>ymax))
			v = offimage + sqrt5;
		else
			v = image16[r5 + 1] + sqrt5;
		if (v < min)
 			min = v;

		if ((x<2) || (y>ymax))
			v = offimage + sqrt5;
		else
			v = image16[r4] + sqrt5;
		if (v < min)
			min = v;

		if ((x<2) || (y<2))
			v = offimage + sqrt5;
		else
			v = image16[r2] + sqrt5;
		if (v < min)
			min = v;

		image16[offset] = (short)min;
  
	} // setEdgeValue()
	
	void convertToBytes (int width, int height, short[] image16, byte[] image8) {
		int one = 41;
		int v, offset;
		int round = one / 2;
		
		maxEDM = 0;
 		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				offset = x + y * width;
				v = (image16[offset] + round) / one;
				if (v > 255)
					v = 255;
				if (v>maxEDM)
					maxEDM = v;
				image8[offset] = (byte) v;
			} //  end for x
		} // for y
	} // end ConvertToBytes()

	/**	Finds peaks in the EDM that contain pixels equal to or greater than all of their neighbors. */
	public void findUltimatePoints (ImageProcessor ip) {
		int   rowsize, offset, count, x, y;
		int CoordOffset, xmax, ymax;
		boolean setPixel;

		IJ.showStatus("Finding ultimate points");
		if (debug) {
			movie = new ImageStack(ip.getWidth(), ip.getHeight());
			movie.addSlice("EDM", ip.duplicate());
		}
		if (watershed) {
			if (smoothEDM) filterEDM(ip, true);
			filterEDM(ip, false);
		}
		if (debug)
			movie.addSlice("Filtered EDM", ip.duplicate());
		makeCoordinateArrays (ip);
		byte[] image = (byte[])ip.getPixels();
		ImageProcessor ip2 = null;
		if (watershed)
			ip2 = ip.duplicate();
		int width = ip.getWidth();
		int height = ip.getHeight();
 		rowsize = width;
		xmax = width - 1;
		ymax = height - 1;
		for (int level=maxEDM-1; level>=1; level--) {
			do {
				count = 0;
				for (int i=0; i<histogram[level]; i++) {
					CoordOffset = levelStart[level] + i;
					x = xCoordinate[CoordOffset];
					y = yCoordinate[CoordOffset];
					offset = x + y * rowsize;
					if ((image[offset]&255) != 255) {
						setPixel = false;
						if ((x>0) && (y>0) && ((image[offset-rowsize-1]&255) > level))
							setPixel = true;
						if ((y>0) && ((image[offset-rowsize]&255) > level))
							setPixel = true;
						if ((x<xmax) && (y>0) && ((image[offset-rowsize+1]&255) > level))
							setPixel = true;
						if ((x<xmax) && ((image[offset+1]&255) > level))
							setPixel = true;
						if ((x<xmax) && (y<ymax) && ((image[offset+rowsize+1]&255) > level))
							setPixel = true;
						if ((y<ymax) && ((image[offset+rowsize]&255) > level))
							setPixel = true;
						if ((x>0) && (y<ymax) && ((image[offset+rowsize-1]&255) > level))
							setPixel = true;
						if ((x>0) && ((image[offset-1]&255) > level))
							setPixel = true;
						if (setPixel) {
							image[offset] = (byte)255;
							count++;
          					}
					} // if pixel not 255 */
				} //  for i
			} while (count != 0);
		} //  for

		if (watershed) {
			byte[] image2 = (byte[])ip2.getPixels();
			for (int i=0; i<width*height; i++) {
				if (((image[i]&255)>0) && ((image[i]&255)<255))
					image2[i] = (byte)0xff;
			}
			//CopyMemory (image, image2, Info->BytesPerRow*Info->nlines);
			ip.insert(ip2, 0, 0);
		} else {
			for (int i=0; i<width*height; i++) {
				if ((image[i]&255)==255)
					image[i] = (byte)0;
			}
		}
	} // findUltimatePoints()


	void filterEDM(ImageProcessor edm, boolean smooth) {
		int rowsize, offset, sum;
		int xmax, ymax;

		byte[] image = (byte[])edm.getPixels();
		ImageProcessor ip2 = edm.duplicate();
		byte[] image2 = (byte[])ip2.getPixels();
		int width = edm.getWidth();
		int height = edm.getHeight();
		rowsize = width;
		xmax = width - 1;
		ymax = height - 1;
		int p0,p1,p2,p3,p4,p5,p6,p7,p8;
		int v;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				offset = x + y*rowsize;
				p0 = image2[offset];
				if (p0 >1) {
					sum = image2[offset] * 2;
					p1 = x>0&&y>0?image2[offset-rowsize-1]:get(x-1,y-1,image2,width,height);
					p2 = y>0?image2[offset-rowsize]:get(x,y-1,image2,width,height);
					p3 = x<xmax&&y>0?image2[offset-rowsize+1]:get(x+1,y-1,image2,width,height);
					p4 = x<xmax?image2[offset+1]:get(x+1,y,image2,width,height);
					p5 = x<xmax&&y<ymax?image2[offset+rowsize+1]:get(x+1,y+1,image2,width,height);
					p6 = y<ymax?image2[offset+rowsize]:get(x,y+1,image2,width,height);
					p7 = x>0&&y<ymax?image2[offset+rowsize-1]:get(x-1,y+1,image2,width,height);
					p8 = x>0?image2[offset-1]:get(x-1,y,image2,width,height);
					v = p0 - 1;
					if (smooth)
						image[offset] = (byte)((p0+p1+p2+p3+p4+p5+p6+p7+p8)/9);
					else {
						if (p2==v&&p4==v&&p6==v&&p8==v
						&&((p1==p0&&p3==v&&p5==v&&p7==v)
						||(p3==p0&&p1==v&&p5==v&&p7==v)
						||(p5==p0&&p1==v&&p3==v&&p7==v)
						||(p7==p0&&p1==v&&p3==v&&p5==v)))
						image[offset] = (byte)v;
					}
				} // if p0>0
			} // for x
		} // for y
	}
	
	int get(int x, int y, byte[] pixels, int width, int height) {
			if (x<=0) x = 0;
			if (x>=width) x = width-1;
			if (y<=0) y = 0;
			if (y>=height) y = height-1;
			return pixels[x+y*width];
	}

	/**
	Generates the xy coordinate arrays that allow pixels at each
	level to be accessed directly without searching through the
	entire image.  This method, suggested by Stein Roervik
	(stein@kjemi.unit.no), greatly speeds up the watershed
	segmentation routine.
	*/
	void makeCoordinateArrays (ImageProcessor edm) {
		int rowsize, offset, v, ArraySize;
 		int width = edm.getWidth();
		int height = edm.getHeight();
		histogram = edm.getHistogram();
		
		ArraySize = 0;
		for (int i=0; i<maxEDM-1; i++)
			ArraySize += histogram[i];
		xCoordinate = new short[ArraySize];
 		yCoordinate = new short[ArraySize];
		byte[] image = (byte[])edm.getPixels();
		offset = 0;
		levelStart = new int[256];
		for (int i=0; i<256; i++) {
			levelStart[i] = offset;
			if ((i>0) && (i<maxEDM))
				offset += histogram[i];
		}

		levelOffset = new int[256];
		rowsize = width;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				v = image[x + y * rowsize]&255;
				if ((v>0) && (v<maxEDM)) {
					offset = levelStart[v] + levelOffset[v];
					xCoordinate[offset] = (short) x;
					yCoordinate[offset] = (short) y;
					levelOffset[v] += 1;
				}
			}
		}
	} // makeCoordinateArrays()
	
	
	/*
	Assumes the ip1 contains an EDM and that the peaks in the EDM
	(the ultimate eroded points) have been set to 255. The EDM is dilated from
	these peaks, starting at the highest peak and working down. At each level,
	the dilation is constrained to pixels whose values are at that level and
	also constrained to prevent features from merging.
	*/
	void doWatershed(ImageProcessor ip1) {
		int[] table = makeFateTable();
		if (debug)
			movie.addSlice("EDM+UEPs", ip1.duplicate());
		IJ.showStatus("Watershed (press esc to cancel)");
		ImageProcessor ip2 = ip1.duplicate();
		for (int level=maxEDM-1; level>=1; level--) {
			IJ.showProgress(maxEDM-level, maxEDM-1);
			do {
				count = 0;
				processLevel(level, 1, ip1, ip2, table);
				processLevel(level, 3, ip1, ip2, table);
				processLevel(level, 2, ip1, ip2, table);
				processLevel(level, 4, ip1, ip2, table);
			} while (count>0);
		if (debug)
			movie.addSlice("level "+level, ip1.duplicate());
		if (win!=null && win.running!=true)
			{canceled=true; IJ.beep(); break;}
		}
		if (!canceled)
			postProcess(ip1);
		if (debug) {
			movie.addSlice("Post-processed", ip1.duplicate());
			new ImagePlus("The movie", movie).show();
		}
	}

	int[] makeFateTable() {
		// This is the lookup table used by the watershed function for dilating the UEPs.
		// There is an entry in the table for each possible 3x3 neighborhood.
		// A pixel is added on the 1st pass if bit 0 is set, on the 2nd pass if bit 1 is
		// set, on the 3rd pass if bit 2 is set, and on the 4th pass if bit 3 is set.
		// E.g. 4 = add on 3rd pass, 3 = add on either 1st or 2nd pass, 15 = add on any pass. 
		int[] table = 
			// 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1		
			 { 0, 0, 4, 4, 0, 0, 4, 4, 8, 0,12,12, 8, 0,12,12, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0,12,12, 8, 0,12,12,
			   1, 0, 0, 0, 0, 0, 0, 0, 8, 0,15,15, 8, 0,15,15, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0,15,15, 9, 0,15,15,
			   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			   1, 0, 0, 0, 0, 0, 0, 0, 9, 0,15,15, 9, 0,15,15, 1, 0, 0, 0, 0, 0, 0, 0, 9, 0,15,15, 9, 0,15,15,
			   2, 2, 6, 6, 0, 0, 6, 6, 0, 0,15,15, 0, 0,15,15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,15,15, 0, 0,15,15,
			   1, 3,15,15, 0, 0,15,15,15,15,15,15,15,15,15,15, 3, 3,15,15, 0, 0,15,15,15,15,15,15,15,15,15,15,
			   2, 2, 6, 6, 0, 0, 6, 6, 0, 0,15,15, 0, 0,15,15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,15,15, 0, 0,15,15,
			   3, 3,15,15, 0, 0,15,15,15,15,15,15,15,15,15,15, 3, 3,15,15, 0, 0,15,15,15,15,15,15,15,15,15,15};
        return table;		
	}

	void processLevel(int level, int pass, ImageProcessor ip1, ImageProcessor ip2, int[] table) {
  		int rowSize = ip1.getWidth();
  		int height = ip1.getHeight();
  		int xmax=rowSize-1;
  		int ymax = ip1.getHeight()-1;
		byte[] pixels1 = (byte[])ip1.getPixels();
		byte[] pixels2 = (byte[])ip2.getPixels();
        System.arraycopy(pixels1, 0, pixels2, 0, rowSize*height);
				
		for (int i=0; i<histogram[level]; i++) {
			int coordOffset = levelStart[level] + i;
	      	int x = xCoordinate[coordOffset];
  			int y = yCoordinate[coordOffset];
			int offset = x + y*rowSize;
			int index;
			if ((pixels2[offset]&255)!=255) {
				index = 0;
				if (x>0 && y>0 && (pixels2[offset-rowSize-1]&255)==255)
					index ^= 1;
				if (y>0 && (pixels2[offset-rowSize]&255)==255)
					index ^= 2;
				if (x<xmax && y>0 && (pixels2[offset-rowSize+1]&255)==255)
					index ^= 4;
				if (x<xmax && (pixels2[offset+1]&255)==255)
					index ^= 8;
				if (x<xmax && y<ymax && (pixels2[offset+rowSize+1]&255)==255)
					index ^= 16;
				if (y<ymax && (pixels2[offset+rowSize]&255)==255)
						index ^= 32;
				if (x>0 && y<ymax && (pixels2[offset+rowSize-1]&255)==255)
						index ^= 64;
				if (x>0 && (pixels2[offset-1]&255)==255)
						index ^= 128;
				switch (pass) {
					case 1: if ((table[index]&1)==1) { //top
								pixels1[offset] = (byte)255;
								count++;
							}
							break;
					case 2: if ((table[index]&2)==2) { //right
								pixels1[offset] = (byte)255;
								count++;
							}
							break;
					case 3: if ((table[index]&4)==4) { //bottom
								pixels1[offset] = (byte)255;
								count++;
							}
							break;
					case 4: if ((table[index]&8)==8) { //left
								pixels1[offset] = (byte)255;
								count++;
							}
							break;
				} // switch
			} // if
		} // for
		//if (debug && level==20)
		//	movie.addSlice("level "+level+" ("+pass+")", ip1.duplicate());
	}
	

	void postProcess(ImageProcessor ip) {
		byte[] pixels = (byte[])ip.getPixels();
		int size = ip.getWidth()*ip.getHeight();
		for (int i=0; i<size; i++) {
			if ((pixels[i]&255)<255)
				pixels[i] = (byte)0;
		}
	}
		
}

