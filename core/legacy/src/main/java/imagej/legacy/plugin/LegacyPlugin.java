//
// LegacyPlugin.java
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

package imagej.legacy.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.legacy.DatasetHarmonizer;
import imagej.legacy.LegacyImageMap;
import imagej.legacy.LegacyOutputTracker;
import imagej.legacy.LegacyService;
import imagej.legacy.LegacyUtils;
import imagej.object.ObjectService;
import imagej.ui.DialogPrompt;
import imagej.ui.IUserInterface;
import imagej.ui.UIService;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.imglib2.img.Axes;

/**
 * Executes an IJ1 plugin.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyPlugin implements ImageJPlugin {

	@Parameter
	private String className;

	@Parameter
	private String arg;

	@Parameter(type = ItemIO.OUTPUT)
	private List<ImageDisplay> outputs;

	private ImageDisplayService imageDisplayService;

	// -- LegacyPlugin methods --

	/** Gets the list of output {@link ImageDisplay}s. */
	public List<ImageDisplay> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}

	// -- Runnable methods --

	@Override
	public void run() {
		
		imageDisplayService = ImageJ.get(ImageDisplayService.class);
		final ImageDisplay activeDisplay =
			imageDisplayService.getActiveImageDisplay();
		
		if (!isLegacyCompatible(activeDisplay)) {
			String err = "Active dataset too large to be represented inside IJ1";
			notifyUser(err);
			Log.error(err);
			outputs = new ArrayList<ImageDisplay>();
			return;
		}

		/*
		// temp hack to test error handling
		if (new Random().nextDouble() < 0.3) {
			String err = "programmer generated error";
			notifyUser(err);
			Log.error(err);
			outputs = new ArrayList<ImageDisplay>();
			return;
		}
		*/
		
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		final LegacyImageMap map = legacyService.getImageMap();

		// sync legacy images to match existing modern displays
		final DatasetHarmonizer harmonizer =
			new DatasetHarmonizer(map.getTranslator());
		final Set<ImagePlus> outputSet = LegacyOutputTracker.getOutputImps();
		final Set<ImagePlus> closedSet = LegacyOutputTracker.getClosedImps();

		harmonizer.resetTypeTracking();

		updateImagePlusesFromDisplays(map, harmonizer);

		// must happen after updateImagePlusesFromDisplays()
		outputSet.clear();
		closedSet.clear();

		// set ImageJ1's active image
		legacyService.syncActiveImage();

		try {
			// execute the legacy plugin
			IJ.runPlugIn(className, arg);

			// sync modern displays to match existing legacy images
			outputs = updateDisplaysFromImagePluses(map, harmonizer);
		}
		catch (final Exception e) {
			String err = "ImageJ 1.x plugin threw exception";
			notifyUser(err);
			Log.error(err);
			// make sure our ImagePluses are in sync with original Datasets
			updateImagePlusesFromDisplays(map, harmonizer);
			// return no outputs
			outputs = new ArrayList<ImageDisplay>();
		}

		// close any displays that IJ1 wants closed
		for (final ImagePlus imp : closedSet) {
			final ImageDisplay disp = map.lookupDisplay(imp);
			if (disp != null) {
				// REMOVED: outputs.remove(display);
				// Now only close displays that have not been changed
				if (!outputs.contains(disp))
					disp.close();
			}
		}

		// clean up
		harmonizer.resetTypeTracking();
		outputSet.clear();
		closedSet.clear();

		// reflect any changes to globals in IJ2 options/prefs
		legacyService.updateIJ2Settings();
	}

	// -- Helper methods --

	private void updateImagePlusesFromDisplays(final LegacyImageMap map,
		final DatasetHarmonizer harmonizer)
	{
		// TODO - track events and keep a dirty bit, then only harmonize those
		// displays that have changed. See ticket #546.
		final ObjectService objectService = ImageJ.get(ObjectService.class);
		final List<ImageDisplay> imageDisplays =
			objectService.getObjects(ImageDisplay.class);
		for (final ImageDisplay display : imageDisplays) {
			ImagePlus imp = map.lookupImagePlus(display);
			if (imp == null) {
				if (isLegacyCompatible(display)) {
					imp = map.registerDisplay(display);
					harmonizer.registerType(imp);
				}
			}
			else { // imp already exists : update it
				harmonizer.updateLegacyImage(display, imp);
				harmonizer.registerType(imp);
			}
		}
	}

	private List<ImageDisplay> updateDisplaysFromImagePluses(
		final LegacyImageMap map, final DatasetHarmonizer harmonizer)
	{
		// TODO - check the changes flag for each ImagePlus that already has a
		// ImageDisplay and only harmonize those that have changed. Maybe changes
		// flag does not track everything (such as metadata changes?) and thus
		// we might still have to do some minor harmonization. Investigate.

		// the IJ1 plugin may not have any outputs but just changes current
		// ImagePlus make sure we catch any changes via harmonization
		final List<ImageDisplay> displays = new ArrayList<ImageDisplay>();
		final ImagePlus currImp = WindowManager.getCurrentImage();
		if (currImp != null) {
			ImageDisplay display = map.lookupDisplay(currImp);
			if (display != null) {
				harmonizer.updateDisplay(display, currImp);
			}
			else {
				display = map.registerLegacyImage(currImp);
				displays.add(display);
			}
		}

		// also harmonize any outputs

		final Set<ImagePlus> imps = LegacyOutputTracker.getOutputImps();
		for (final ImagePlus imp : imps) {
			if (imp.getStack().getSize() == 0) { // totally emptied by plugin
				// TODO - do we need to delete display or is it already done?
			}
			else { // image plus is not totally empty
				ImageDisplay display = map.lookupDisplay(imp);
				if (display == null) {
					if (imp.getWindow() != null) {
						display = map.registerLegacyImage(imp);
					}
					else {
						continue;
					}
				}
				else {
					if (imp == currImp) {
						// we harmonized this earlier
					}
					else harmonizer.updateDisplay(display, imp);
				}
				displays.add(display);
			}
		}

		return displays;
	}

	private boolean isLegacyCompatible(final ImageDisplay display) {
		if (display == null) return true;
		final Dataset ds = imageDisplayService.getActiveDataset(display);
		if (dimensionsIncompatible(ds)) return false;
		return true;
	}

	/**
	 * Determines if a Dataset's dimensions cannot be represented within
	 * an IJ1 ImageStack. Returns true if the Dataset does not have X or
	 * Y axes. Returns true if the XY plane size is greater than
	 * Integer.MAX_VALUE. Returns true if the number of planes is greater
	 * than Integer.MAX_VALUE. 
	 */
	private boolean dimensionsIncompatible(final Dataset ds) {
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		final long[] dims = ds.getDims();

		final long xCount = xIndex < 0 ? 1 : dims[xIndex];
		final long yCount = yIndex < 0 ? 1 : dims[yIndex];
		final long zCount = zIndex < 0 ? 1 : dims[zIndex];
		final long tCount = tIndex < 0 ? 1 : dims[tIndex];

		final long cCount = LegacyUtils.ij1ChannelCount(dims, ds.getAxes());
		final long ij1ChannelCount = ds.isRGBMerged() ? (cCount / 3) : cCount;

		// check width exists
		if (xIndex < 0) return true;

		// check height exists
		if (yIndex < 0) return true;

		// check plane size not too large
		if ((xCount * yCount) > Integer.MAX_VALUE) return true;

		// check number of planes not too large
		if (ij1ChannelCount * zCount * tCount > Integer.MAX_VALUE) return true;

		return false;
	}

	private void notifyUser(String message) {
		final IUserInterface ui = ImageJ.get(UIService.class).getUI();
		final DialogPrompt dialog =
			ui.dialogPrompt(message, "Error",
				DialogPrompt.MessageType.INFORMATION_MESSAGE,
				DialogPrompt.OptionType.DEFAULT_OPTION);
		dialog.prompt();
	}
}
