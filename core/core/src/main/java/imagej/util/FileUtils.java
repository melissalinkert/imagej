//
// FileUtils.java
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

// File path shortening code adapted from:
// from: http://www.rgagnon.com/javadetails/java-0661.html

package imagej.util;

import java.io.File;

/**
 * Useful methods for working with file paths.
 * 
 * @author Johannes Schindelin
 * @author Grant Harris
 */
public final class FileUtils {

	public static final int DEFAULT_SHORTENER_THRESHOLD = 4;
	public static final String SHORTENER_BACKSLASH_REGEX = "\\\\";
	public static final String SHORTENER_SLASH_REGEX = "/";
	public static final String SHORTENER_BACKSLASH = "\\";
	public static final String SHORTENER_SLASH = "/";
	public static final String SHORTENER_ELLIPSE = "...";

	private FileUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Extracts the file extension from a file.
	 * 
	 * @param file the file object
	 * @return the file extension, or the empty string when the file name does not
	 *         contain dots
	 */
	public static String getExtension(final File file) {
		return getExtension(file.getPath());
	}

	/**
	 * Extracts the file extension from a file name.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return the file extension, or the empty string when the file name does not
	 *         contain dots
	 */
	public static String getExtension(final String path) {
		final int dot = path.lastIndexOf('.');
		if (dot < 0) {
			return "";
		}
		return path.substring(dot + 1);
	}

	/**
	 * Shortens the path to a maximum of 4 path elements.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @return shortened path
	 */
	public static String shortenPath(final String path) {
		return shortenPath(path, DEFAULT_SHORTENER_THRESHOLD);
	}

	/**
	 * Shortens the path based on the given maximum number of path elements. E.g.,
	 * "C:/1/2/test.txt" returns "C:/1/.../test.txt" if threshold is 1.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @param threshold the number of directories to keep unshortened
	 * @return shortened path
	 */
	public static String shortenPath(final String path, final int threshold) {
		String regex = SHORTENER_BACKSLASH_REGEX;
		String sep = SHORTENER_BACKSLASH;

		if (path.indexOf("/") > 0) {
			regex = SHORTENER_SLASH_REGEX;
			sep = SHORTENER_SLASH;
		}

		String pathtemp[] = path.split(regex);
		// remove empty elements
		int elem = 0;
		{
			final String newtemp[] = new String[pathtemp.length];
			int j = 0;
			for (int i = 0; i < pathtemp.length; i++) {
				if (!pathtemp[i].equals("")) {
					newtemp[j++] = pathtemp[i];
					elem++;
				}
			}
			pathtemp = newtemp;
		}

		if (elem > threshold) {
			final StringBuilder sb = new StringBuilder();
			int index = 0;

			// drive or protocol
			final int pos2dots = path.indexOf(":");
			if (pos2dots > 0) {
				// case c:\ c:/ etc.
				sb.append(path.substring(0, pos2dots + 2));
				index++;
				// case http:// ftp:// etc.
				if (path.indexOf(":/") > 0 && pathtemp[0].length() > 2) {
					sb.append(SHORTENER_SLASH);
				}
			}
			else {
				final boolean isUNC =
					path.substring(0, 2).equals(SHORTENER_BACKSLASH_REGEX);
				if (isUNC) {
					sb.append(SHORTENER_BACKSLASH).append(SHORTENER_BACKSLASH);
				}
			}

			for (; index <= threshold; index++) {
				sb.append(pathtemp[index]).append(sep);
			}

			if (index == (elem - 1)) {
				sb.append(pathtemp[elem - 1]);
			}
			else {
				sb.append(SHORTENER_ELLIPSE).append(sep).append(pathtemp[elem - 1]);
			}
			return sb.toString();
		}
		return path;
	}

	/**
	 * Compacts a path into a given number of characters. The result is similar to
	 * the Win32 API PathCompactPathExA.
	 * 
	 * @param path the path to the file (relative or absolute)
	 * @param limit the number of characters to which the path should be limited
	 * @return shortened path
	 */
	public static String limitPath(final String path, final int limit) {
		if (path.length() <= limit) return path;

		final char shortPathArray[] = new char[limit];
		final char pathArray[] = path.toCharArray();
		final char ellipseArray[] = SHORTENER_ELLIPSE.toCharArray();

		final int pathindex = pathArray.length - 1;
		final int shortpathindex = limit - 1;

		// fill the array from the end
		int i = 0;
		for (; i < limit; i++) {
			if (pathArray[pathindex - i] != '/' && pathArray[pathindex - i] != '\\') {
				shortPathArray[shortpathindex - i] = pathArray[pathindex - i];
			}
			else {
				break;
			}
		}
		// check how much space is left
		final int free = limit - i;

		if (free < SHORTENER_ELLIPSE.length()) {
			// fill the beginning with ellipse
			for (int j = 0; j < ellipseArray.length; j++) {
				shortPathArray[j] = ellipseArray[j];
			}
		}
		else {
			// fill the beginning with path and leave room for the ellipse
			int j = 0;
			for (; j + ellipseArray.length < free; j++) {
				shortPathArray[j] = pathArray[j];
			}
			// ... add the ellipse
			for (int k = 0; j + k < free; k++) {
				shortPathArray[j + k] = ellipseArray[k];
			}
		}
		return new String(shortPathArray);
	}

}
