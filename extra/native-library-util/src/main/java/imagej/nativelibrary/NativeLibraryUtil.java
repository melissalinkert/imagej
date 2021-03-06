//
// NativeLibraryUtil.java
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

package imagej.nativelibrary;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import loci.wapmx.nativeutils.jniloader.DefaultJniExtractor;
import loci.wapmx.nativeutils.jniloader.JniExtractor;

/**
 * This class is a utility for loading native libraries.
 * <p>
 * An early approach, exemplified in the now-deprecated loadLibrary method,
 * was to find a writable directory on the classpath and expand the appropriate
 * native library there and load it.  This approach fails in Linux.
 * <p>
 * Current approach is to unpack the native library into a temporary file and
 * load from there.
 * 
 * @author Aivar Grislis
 */
public class NativeLibraryUtil {
    public static enum Architecture
        { UNKNOWN, LINUX_32, LINUX_64, WINDOWS_32, WINDOWS_64, OSX_32, OSX_64, OSX_PPC };
    private static enum Processor
        { UNKNOWN, INTEL_32, INTEL_64, PPC };
    private static Architecture s_architecture = Architecture.UNKNOWN;
    private static final String DELIM = "/";
    private static final String USER_TMPDIR = "java.library.tmpdir";
    private static final String JAVA_TMPDIR = "java.io.tmpdir";
    private static final String JAVA_PATH = "java.library.path";
    private static final String SUN_PATH = "sun.boot.library.path";
    private static final String USER_PATHS = "usr_paths";
    private static final String CURRENT_DIRECTORY = ".";
    private static boolean s_skipHack = false;
    private static String s_writableDirectory = null;

    private static final Logger LOGGER = Logger.getLogger("imagej.nativelibrary.NativeLibraryUtil");

    /**
     * Determines the underlying hardware platform and architecture.
     *
     * @return enumerated architecture value
     */
    public static Architecture getArchitecture() {
        if (Architecture.UNKNOWN == s_architecture) {
            Processor processor = getProcessor();
            if (Processor.UNKNOWN != processor) {
                String name = System.getProperty("os.name").toLowerCase();
                if (name.indexOf("nix") >= 0 || name.indexOf("nux") >= 0) {
                    if (Processor.INTEL_32 == processor) {
                        s_architecture = Architecture.LINUX_32;
                    }
                    else if (Processor.INTEL_64 == processor) {
                        s_architecture = Architecture.LINUX_64;
                    }
                }
                else if (name.indexOf("win") >= 0) {
                    if (Processor.INTEL_32 == processor) {
                        s_architecture = Architecture.WINDOWS_32;
                    }
                    else if (Processor.INTEL_64 == processor) {
                        s_architecture = Architecture.WINDOWS_64;
                    }
                }
                else if (name.indexOf("mac") >= 0) {
                    if (Processor.INTEL_32 == processor) {
                        s_architecture = Architecture.OSX_32;
                    }
                    else if (Processor.INTEL_64 == processor) {
                        s_architecture = Architecture.OSX_64;
                    }
                    else if (Processor.PPC == processor) {
                        s_architecture = Architecture.OSX_PPC;
                    }
                }
            }   
        }
        LOGGER.log(Level.INFO, "architecture is " + s_architecture + " os.name is " + System.getProperty("os.name").toLowerCase());
        return s_architecture;
    }

    /**
     * Determines what processor is in use.
     *
     * @return
     */
    private static Processor getProcessor() {
        Processor processor = Processor.UNKNOWN;
        int bits;

        // Note that this is actually the architecture of the installed JVM.
        String arch = System.getProperty("os.arch").toLowerCase();

        if (arch.indexOf("ppc") >= 0) {
            processor = Processor.PPC;
        }
        else if (arch.indexOf("86") >= 0 || arch.indexOf("amd") >= 0) {
            bits = 32;
            if (arch.indexOf("64") >= 0) {
                bits = 64;
            }
            processor = (32 == bits) ? Processor.INTEL_32 : Processor.INTEL_64;
        }
        LOGGER.log(Level.INFO, "processor is " + processor + " os.arch is " + System.getProperty("os.arch").toLowerCase());
        return processor;
    }

    /**
     * Returns the path to the native library.
     *
     * @return path
     */
    public static String getPlatformLibraryPath() {
        String path = "META-INF" + DELIM + "lib" + DELIM;
        path += getArchitecture().name().toLowerCase() + DELIM;
        LOGGER.log(Level.INFO, "platform specific path is " + path);
        return path;
    }

    /**
     * Returns the full file name (without path) of the native library.
     *
     * @param libName
     * @return file name
     */
    public static String getPlatformLibraryName(String libName) {
        libName = getVersionedLibraryName(libName);
        String name = null;
        switch (getArchitecture()) {
            case LINUX_32:
            case LINUX_64:
                name = libName + ".so";
                break;
            case WINDOWS_32:
            case WINDOWS_64:
                name = libName + ".dll";
                break;
            case OSX_32:
            case OSX_64:
                name = "lib" + libName + ".dylib";
                break;
        }
        LOGGER.log(Level.INFO, "native library name " + name);
        return name;
    }

    /**
     * Returns the Maven-versioned file name of the native library.
     *
     * Note: With the Nar Plugin the class NarSystem.java is built for the
     * client of this native library and takes care of this versioning
     * hardcoding.  If the client is "-1.0-SNAPSHOT" so should the native
     * library be the same version.
     *
     * @param libName
     * @return
     */
    //TODO This shouldn't be hardcoded in the general-purpose utility class.
    //TODO Couldn't we just get rid of this version label altogether?
    public static String getVersionedLibraryName(String libName) {
        return libName + "-1.0-SNAPSHOT";
    }

    /**
     * Loads the native library.
     *
     * @param libraryJarClass any class within the library-containing jar
     * @param libName name of library
     * @return whether or not successful
     */
    public static boolean loadNativeLibrary(Class libraryJarClass, String libName) {
        boolean success = false;

        if (Architecture.UNKNOWN == getArchitecture()) {
            LOGGER.log(Level.INFO, "No native library available for this platform.");
        }
        else {
            try
            {
                // will extract library to temporary directory
                String tmpDirectory = System.getProperty(JAVA_TMPDIR);
                JniExtractor jniExtractor =
                    new DefaultJniExtractor(libraryJarClass, tmpDirectory);

                // do extraction
                File extractedFile = jniExtractor.extractJni
                        (getPlatformLibraryPath(), getVersionedLibraryName(libName));

                // load extracted library from temporary directory
                System.load(extractedFile.getPath());

                success = true;
            }
            catch (IOException e)
            {
                LOGGER.log(Level.INFO, "IOException creating DefaultJniExtractor", e);
            }
            catch (SecurityException e)
            {
                LOGGER.log(Level.INFO, "Can't load dynamic library", e);
            }
            catch (UnsatisfiedLinkError e)
            {
                LOGGER.log(Level.INFO, "Problem with library", e);
            }

        }
        return success;
    }

    /**
     * Loads the native library specified by the libname argument.
     * Can be used in place of System.loadLibrary().
     * Extracts
     */
    @Deprecated
    public static void loadLibrary(Class libraryJarClass, String libname) {
        extractNativeLibraryToPath(libraryJarClass, libname);
        System.loadLibrary(libname);
    }

    /**
     * Extracts the native library specified by the libname argument from the
     * resources of the jar file that contains the given libraryJarClass class.
     * Puts it on the library path.
     *
     * @param libraryJarClass
     * @param libname
     * @return
     */
    @Deprecated
    public static boolean extractNativeLibraryToPath(Class libraryJarClass, String libname) {
        boolean success = false;

        try {
            // get a temporary directory
            boolean userSuppliedDirectory = true;
            String directory = System.getProperty(USER_TMPDIR);
            if (null == directory) {
                userSuppliedDirectory = false;

                directory = System.getProperty(JAVA_TMPDIR);
            }

            // if we should try the hack
            if (!s_skipHack) {
                // is it necessary? already on path?
                if (!isOnLibraryPath(directory)) {

                    // try the hack
                    if (!addToLibraryPath(directory)) {
                        // fails, don't try again
                        s_skipHack = true;
                    }
                }
            }

            // if hack doesn't work
            if (s_skipHack) {
                // go with user supplied directory
                if (!userSuppliedDirectory) {
                    // otherwise, find a directory on the path to extract to
                    directory = findWritableDirectoryOnPath();
                }
            }

            // extract library to directory
            if (null != directory) {
                try {
                    JniExtractor jniExtractor =
                            new DefaultJniExtractor(libraryJarClass, directory);

                    File extractedFile = jniExtractor.extractJni("", libname); //TODO pass in libary path or get rid of this method

                    success = true;
                }
                catch (IOException e) {
                    System.out.println("IOException creating DefaultJniExtractor " + e.getMessage());
                }
            }
        }
        catch (SecurityException e) {
            // a security manager exists and its checkPropertyAccess method
            // doesn't allow access to the specified system property.
        }

        return success;
    }

    /**
     * Is the given directory on java.library.path?
     *
     * @param directory
     * @return whether or not on path
     */
    public static boolean isOnLibraryPath(String directory) {
        return checkLibraryPath(JAVA_PATH, directory)
                || checkLibraryPath(SUN_PATH, directory);
    }

    /**
     * Helper routine, checks path for a given property name.
     *
     * @param propertyName
     * @param directory
     * @return whether or not on path
     */
    private static boolean checkLibraryPath(String propertyName, String directory) {
        String paths[] = getPaths(propertyName);
        for (String path : paths) {
            System.out.println(path);
            if (directory.equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper routine, gets list of paths for a given property name.
     *
     * @param propertyName
     * @return list of paths
     */
    private static String[] getPaths(String propertyName) {
        String paths[] = null;
        try {
            paths = System.getProperty(propertyName).split(File.pathSeparator);
        }
        catch (SecurityException e) {
            // unable to get list of paths
            paths = new String[0];
        }
        return paths;
    }

    /**
     * Adds a given folder to the java.library.path.
     *
     * From {@link http://nicklothian.com/blog/2008/11/19/modify-javalibrarypath-at-runtime/}
     *
     * "This enables the java.library.path to be modified at runtime. From a
     * Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176" (link
     * is dead)
     *
     * See also {@link http://forums.java.net/node/703790}
     *
     * "So here's what I found. I decompiled the RV library, and used that to
     * step into the Java classloader code. There I found two variables being
     * used to look for native libraries: sys_paths and usr_paths. Usr_paths
     * *appears* to be loaded from the environment variable 'java.library.path'
     * and sys_paths *appears* to be loaded from the environment variable
     * 'sun.boot.library.path'."
     *
     * See also {@link http://safcp.googlecode.com/svn/trunk/SAFCP/src/main/java/ufrj/safcp/util/JavaLibraryPath.java}
     *
     * Uses similar approach, GPL3 license: "Will not work if JVM security
     * policy gets in the way (like in an applet). Will not work if Sun changes
     * the private members. This really shouldn't be used at all."
     *
     * @param directory folder to add, should be absolute path
     * @return whether successful
     */
    public static boolean addToLibraryPath(String directory) {
        boolean success = false;

        try {
            // get user paths
            Field field = ClassLoader.class.getDeclaredField(USER_PATHS);
            field.setAccessible(true);
            String[] paths = (String[])field.get(null);

            // already in paths?
            for (int i = 0; i < paths.length; i++) {
                if (directory.equals(paths[i])) {
                    return true;
                }
            }

            // add to user paths
            String[] tmp = new String[paths.length+1];
            System.arraycopy(paths,0,tmp,0,paths.length);
            tmp[paths.length] = directory;
            field.set(null,tmp);

            System.setProperty(JAVA_PATH,
                    System.getProperty(JAVA_PATH) + File.pathSeparator + directory); //TODO why bother?

            success = true;
        }
        catch (IllegalAccessException e) {
            // Failed to get permissions to set library path
        }
        catch (NoSuchFieldException e) {
            // Failed to get field handle to set library path
	}
        catch (Exception e) {
            // play it safe
        }

        return success;
    }

    public static String findWritableDirectoryOnPath() {
        // if we haven't found this already
        if (null == s_writableDirectory) {
            // try the current directory first
            if (isOnLibraryPath(CURRENT_DIRECTORY)) {
                // is on path, is it writable?
               if (isWritableDirectory(CURRENT_DIRECTORY)) {
                   // yes, use it
                   s_writableDirectory = CURRENT_DIRECTORY;
               }
            }
            // still looking?
            if (null == s_writableDirectory) {
                // look on java library path
                s_writableDirectory = findWritableDirectory(JAVA_PATH);
                // still looking?
                if (null == s_writableDirectory) {
                    // look on Sun library path
                    s_writableDirectory = findWritableDirectory(SUN_PATH);
                }
            }
        }
        return s_writableDirectory;
    }

    /**
     * Helper routine, checks path for a given property name.
     *
     * @param propertyName
     * @param directory
     * @return whether or not on path
     */
    private static String findWritableDirectory(String propertyName) {
        String paths[] = getPaths(propertyName);
        for (String path : paths) {
            if (isWritableDirectory(path)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Do we have write access to the given directory?
     *
     * @param directory
     * @return whether or not writable
     */
    public static boolean isWritableDirectory(String directory) {
        boolean success = false;
        try {
            File tempFile = File.createTempFile("dummy", null, new File(directory));
            tempFile.deleteOnExit();
            success = true;
        }
        catch (IOException e) {
            // file could not be created
        }
        catch (SecurityException e) {
            // security manager exists and checkWrite method does not allow a file to be created

        }
        return success;
    }
}

