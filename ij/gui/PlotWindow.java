package ij.gui;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.util.*;
import ij.*;
import ij.process.*;
import ij.util.*;


/** This class is an extended ImageWindow that displays plots. */
public class PlotWindow extends ImageWindow implements ActionListener, ClipboardOwner {

	public static final int CIRCLE = 0;

	static final int LEFT_MARGIN = 50;
	static final int RIGHT_MARGIN = 20;
	static final int TOP_MARGIN = 20;
	static final int BOTTOM_MARGIN = 30;
	static final int WIDTH = 450;
	static final int HEIGHT = 200;
	
	static final String MIN = "pp.min";
	static final String MAX = "pp.max";
	static final String OPTIONS = "pp.options";
	static final int SAVE_X_VALUES = 1;
	static final int AUTO_CLOSE = 2;

	private int frameWidth;
	private int frameHeight;
	private int xloc;
	private int yloc;
	
	private Rectangle frame = null;
	private float[] xValues, yValues;
	private int nPoints;
	private double xScale, yScale;
	private double xMin, xMax, yMin, yMax;
	private Button list, save, copy;
	private Label coordinates;
	private static String defaultDirectory = null;
	private String xLabel;
	private String yLabel;
	private FontMetrics fm;
	private Font font = new Font("Helvetica", Font.PLAIN, 12);
	private boolean fixedYScale;
 	private Graphics g;
	private static int options;
	public static boolean saveXValues;
	public static boolean autoClose;
 	
    static {
		IJ.register(PlotWindow.class); //keeps options from being reset on some JVMs
    	options = Prefs.getInt(OPTIONS, SAVE_X_VALUES);
    	saveXValues = (options&SAVE_X_VALUES)!=0;
    	autoClose = (options&AUTO_CLOSE)!=0;
    }

	public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		super(new ImagePlus(title,
			GUI.createBlankImage(WIDTH+LEFT_MARGIN+RIGHT_MARGIN, HEIGHT+TOP_MARGIN+BOTTOM_MARGIN)));
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.xValues = xValues;
		this.yValues = yValues;
		double[] a = Tools.getMinMax(xValues);
		xMin=a[0]; xMax=a[1];
		a = Tools.getMinMax(yValues);
		yMin=a[0]; yMax=a[1];
		fixedYScale = false;
		nPoints = xValues.length;
	}

	public void setLimits(double xMin, double xMax, double yMin, double yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		fixedYScale = true;
	}

	public void addPoints(float[] x, float[] y, int shape) {
		setup();
		for (int i=0; i<x.length; i++) {
			int xt = LEFT_MARGIN + (int)((x[i]-xMin)*xScale);
			int yt = TOP_MARGIN + frameHeight - (int)((y[i]-yMin)*yScale);
			g.drawOval(xt-2, yt-2, 5, 5);
		}
	}

	public void addPoints(double[] x, double[] y, int shape) {
		addPoints(Tools.toFloat(x), Tools.toFloat(y), shape);
	}

	public void addLabel(double x, double y, String label) {
		setup();
		int xt = LEFT_MARGIN + (int)(x*frameWidth);
		int yt = TOP_MARGIN + (int)(y*frameHeight);
		g.drawString(label, xt, yt);
	}

	public void draw() {
		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		list = new Button(" List ");
		list.addActionListener(this);
		buttons.add(list);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		copy = new Button("Copy...");
		copy.addActionListener(this);
		buttons.add(copy);
		coordinates = new Label("                     ");
		coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		buttons.add(coordinates);
		add(buttons);
		pack();
		drawPlot();
		g.dispose();
		g = null;
		imp.setProcessor(null, new ColorProcessor(imp.getImage()));
		imp.updateAndDraw();
	}

	void setup() {
		if (g!=null)
			return;
		g = imp.getImage().getGraphics();
		g.setColor(Color.black);
		g.setFont(font);
		if (frameWidth==0) {
			frameWidth = WIDTH;
			frameHeight = HEIGHT;
		}
		frame = new Rectangle(LEFT_MARGIN, TOP_MARGIN, frameWidth, frameHeight);
		if ((xMax-xMin)==0.0)
			xScale = 1.0;
		else
			xScale = frame.width/(xMax-xMin);
		if ((yMax-yMin)==0.0)
			yScale = 1.0;
		else
			yScale = frame.height/(yMax-yMin);
	}

	String d2s(double n) {
		if (Math.round(n)==n)
			return(IJ.d2s(n,0));
		else
			return(IJ.d2s(n));
	}

    public void mouseMoved(int x, int y) {
		if (frame==null || coordinates==null)
			return;
		if (frame.contains(x, y)) {
			x -= frame.x; y -= frame.y;
			coordinates.setText("X=" + d2s(x/xScale+xMin)+", Y=" + d2s((frameHeight-y)/yScale+yMin));
		} else
			coordinates.setText("");
	}
	   
	void drawPlot() {
		int x, y;
		double v;
		
		setup();
		g.drawRect(frame.x, frame.y, frame.width, frame.height);
					
		int xpoints[] = new int[nPoints];
		int ypoints[] = new int[nPoints];
		double value;
		for (int i=0; i<nPoints; i++) {
			value = yValues[i];
			if (value<yMin) value=yMin;
			if (value>yMax) value=yMax;
			xpoints[i] = LEFT_MARGIN + (int)((xValues[i]-xMin)*xScale);
			ypoints[i] = TOP_MARGIN + frame.height - (int)((value-yMin)*yScale);
		}
		g.drawPolyline(xpoints, ypoints, nPoints); 
		
		String s = d2s(yMax);
		int sw = getWidth(s,g);
		if ((sw+4)>LEFT_MARGIN)
			g.drawString(s, 4, TOP_MARGIN-4);
		else
			g.drawString(s, LEFT_MARGIN-getWidth(s,g)-4, TOP_MARGIN+10);
		s = d2s(yMin);
		sw = getWidth(s,g);
		if ((sw+4)>LEFT_MARGIN)
			g.drawString(s, 4, TOP_MARGIN+frame.height);
		else
			g.drawString(s, LEFT_MARGIN-getWidth(s,g)-4, TOP_MARGIN+frame.height);
		x = LEFT_MARGIN;
		y = TOP_MARGIN + frame.height + 15;
		g.drawString(d2s(xMin), x, y);
		s = d2s(xMax);
		g.drawString(s, x + frame.width-getWidth(s,g)+6, y);
		g.drawString(xLabel, LEFT_MARGIN+(frame.width-getWidth(xLabel,g))/2, y+3);
		drawYLabel(g,yLabel,LEFT_MARGIN,TOP_MARGIN,frame.height);
	}
		
	void drawYLabel(Graphics g, String yLabel, int x, int y, int height) {
		if(fm==null)
			fm = g.getFontMetrics();
		int w =  fm.stringWidth(yLabel);
		int h =  fm.getHeight();
		Image label = createImage(w,h);
		Graphics g2 = label.getGraphics();
		g2.setFont(font);
		int descent = fm.getDescent();
		g2.drawString(yLabel, 0, h-descent);
		g2.dispose();
		ImageProcessor ip = new ColorProcessor(label);
		ip = ip.rotateLeft();
		label = ip.createImage();
		int y2 = y+(height-getWidth(yLabel,g))/2;
		if (y2<y) y2 = y;
		int x2 = x-h-5;
		g.drawImage(label, x2, y2, null);
	}

	int getWidth(String s, Graphics g) {
		if(fm==null)
			fm = g.getFontMetrics();
		return fm.stringWidth(s);
	}
	
	void showList() {
		if (saveXValues)
			IJ.setColumnHeadings("X\tY");
		else
			IJ.setColumnHeadings("Y");
		for (int i=0; i<nPoints; i++) {
			if (saveXValues)
				IJ.write(d2s(xValues[i])+"\t"+d2s(yValues[i]));
			else
				IJ.write(d2s(yValues[i]));
		}
		if (autoClose)
			{imp.changes=false; close();}
	}

	void saveAsText() {
		FileDialog fd = new FileDialog(this, "Save as Text...", FileDialog.SAVE);
		if (defaultDirectory!=null)
			fd.setDirectory(defaultDirectory);
		fd.show();
		String name = fd.getFile();
		String directory = fd.getDirectory();
		defaultDirectory = directory;
		fd.dispose();
		PrintWriter pw = null;
		try {
			FileOutputStream fos = new FileOutputStream(directory+name);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		}
		catch (IOException e) {
			IJ.error("" + e);
			return;
		}
		IJ.wait(250);  // give system time to redraw ImageJ window
		IJ.showStatus("Saving plot values...");
		for (int i=0; i<nPoints; i++) {
			if (saveXValues)
				pw.println(d2s(xValues[i])+"\t"+d2s(yValues[i]));
			else
				pw.println(d2s(yValues[i]));
		}
		pw.close();
		if (autoClose)
			{imp.changes=false; close();}
	}
		
	void copyToClipboard() {
		Clipboard systemClipboard = null;
		try {systemClipboard = getToolkit().getSystemClipboard();}
		catch (Exception e) {systemClipboard = null; }
		if (systemClipboard==null)
			{IJ.error("Unable to copy to Clipboard."); return;}
		IJ.showStatus("Copying plot values...");
		CharArrayWriter aw = new CharArrayWriter(nPoints*4);
		PrintWriter pw = new PrintWriter(aw);
		for (int i=0; i<nPoints; i++) {
			if (saveXValues)
				pw.print(d2s(xValues[i])+"\t"+d2s(yValues[i])+"\n");
			else
				pw.print(d2s(yValues[i])+"\n");
		}
		String text = aw.toString();
		pw.close();
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
		if (autoClose)
			{imp.changes=false; close();}
	}
		
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==list)
			showList();
		else if (b==save)
			saveAsText();
		else
			copyToClipboard();
	}
	
	public static void savePreferences(Properties prefs) {
		double min = ProfilePlot.getFixedMin();
		double max = ProfilePlot.getFixedMax();
		if (!(min==0.0&&max==0.0) && min<max) {
			prefs.put(MIN, Double.toString(min));
			prefs.put(MAX, Double.toString(max));
		}
		int options = 0;
		if (saveXValues)
			options |= SAVE_X_VALUES;
		if (autoClose)
			options |= AUTO_CLOSE;
		prefs.put(OPTIONS, Integer.toString(options));
	}
}


