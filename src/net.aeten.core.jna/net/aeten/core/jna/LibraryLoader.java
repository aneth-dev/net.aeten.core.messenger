package net.aeten.core.jna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class LibraryLoader {
	static {
		for (File file: getTempDir ().listFiles ()) {
			file.delete ();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T loadFromJar(Class<T> interfaceClass,
			String libName,
			String... dependencies) {
		for (String lib : dependencies) {
			loadNativeLibraryFromJar (interfaceClass, lib);
		}
		return (T) Native.loadLibrary (libName, interfaceClass);
	}

	private static void loadNativeLibraryFromJar(Class<?> loadinClass,
			String lname) {
		String libname = System.mapLibraryName (lname);
		String arch = System.getProperty ("os.arch");
		String name = System.getProperty ("os.name");
		String resourceName = getNativeLibraryResourcePath (Platform.getOSType (), arch, name) + "/" + libname;
		URL url = loadinClass.getResource (resourceName);
		boolean unpacked = false;

		// Add an ugly hack for OpenJDK (soylatte) - JNI libs use the usual *.dylib extension 
		if (url == null && Platform.isMac () && resourceName.endsWith (".dylib")) {
			resourceName = resourceName.substring (0, resourceName.lastIndexOf (".dylib")) + ".jnilib";
			url = loadinClass.getResource (resourceName);
		}
		if (url == null) {
			throw new UnsatisfiedLinkError ("jnidispatch (" + resourceName + ") not found in resource path");
		}

		File lib = null;
		if (url.getProtocol ().toLowerCase ().equals ("file")) {
			try {
				lib = new File (new URI (url.toString ()));
			} catch (URISyntaxException e) {
				lib = new File (url.getPath ());
			}
			if (!lib.exists ()) {
				throw new Error ("File URL " + url + " could not be properly decoded");
			}
		} else {
			InputStream is = loadinClass.getResourceAsStream (resourceName);
			if (is == null) {
				throw new Error ("Can't obtain jnidispatch InputStream (" + resourceName + ")");
			}

			FileOutputStream fos = null;
			try {
				/* Suffix is required on windows, or library fails to load Let Java pick the suffix,
				 * except on windows, to avoid problems with Web Start.
				 */
				File dir = getTempDir ();
				lib = new File(dir, lname + (Platform.isWindows () ? ".dll" : ""));
				if (lib.exists ()) {
					return;
				}
				lib.deleteOnExit ();
				fos = new FileOutputStream (lib);
				int count;
				byte[] buf = new byte[1024];
				while ((count = is.read (buf, 0, buf.length)) > 0) {
					fos.write (buf, 0, count);
				}
				unpacked = true;
			} catch (IOException e) {
				throw new Error ("Failed to create temporary file for jnidispatch library: " + e);
			} finally {
				try {
					is.close ();
				} catch (IOException e) {}
				if (fos != null) {
					try {
						fos.close ();
					} catch (IOException e) {}
				}
			}
		}
		System.load (lib.getAbsolutePath ());
		/* Attempt to delete immediately once jnidispatch is successfully loaded.
		 * This avoids the complexity of trying to do so on "exit",
		 * which point can vary under different circumstances
		 * (native compilation, dynamically loaded modules, normal application, etc).
		 */
		if (unpacked) {
			deleteNativeLibrary (lib.getAbsolutePath ());
		}
	}

	/** Remove any automatically unpacked native library.

	This will fail on windows, which disallows removal of any file that is
	still in use, so an alternative is required in that case.  Mark
	the file that could not be deleted, and attempt to delete any
	temporaries on next startup.

	Do NOT force the class loader to unload the native library, since
	that introduces issues with cleaning up any extant JNA bits
	(e.g. Memory) which may still need use of the library before shutdown.
	*/
	static boolean deleteNativeLibrary(String path) {
		File flib = new File (path);
		if (flib.delete ()) {
			return true;
		}

		// Couldn't delete it, mark for later deletion
		markTemporaryFile (flib);

		return false;
	}

	/** Perform cleanup of automatically unpacked native shared library.
	 */
	static void markTemporaryFile(File file) {
		// If we can't force an unload/delete, flag the file for later 
		// deletion
		try {
			File marker = new File (file.getParentFile (), file.getName () + ".x");
			marker.createNewFile ();
		} catch (IOException e) {
			e.printStackTrace ();
		}
	}

	static File getTempDir() {
		File tmp = new File (System.getProperty ("java.io.tmpdir"));
		File jnatmp = new File (tmp, "jna-" + System.getProperty ("user.name"));
		jnatmp.mkdirs ();
		return jnatmp.exists () ? jnatmp : tmp;
	}

	static String getNativeLibraryResourcePath(int osType,
			String arch,
			String name) {
		String osPrefix;
		arch = arch.toLowerCase ();
		if ("powerpc".equals (arch)) {
			arch = "ppc";
		} else if ("powerpc64".equals (arch)) {
			arch = "ppc64";
		}
		switch (osType) {
		case Platform.WINDOWS:
			if ("i386".equals (arch)) arch = "x86";
			osPrefix = "win32-" + arch;
			break;
		case Platform.WINDOWSCE:
			osPrefix = "w32ce-" + arch;
			break;
		case Platform.MAC:
			osPrefix = "darwin";
			break;
		case Platform.LINUX:
			if ("x86".equals (arch)) {
				arch = "i386";
			} else if ("x86_64".equals (arch)) {
				arch = "amd64";
			}
			osPrefix = "linux-" + arch;
			break;
		case Platform.SOLARIS:
			osPrefix = "sunos-" + arch;
			break;
		default:
			osPrefix = name.toLowerCase ();
			if ("x86".equals (arch)) {
				arch = "i386";
			}
			if ("x86_64".equals (arch)) {
				arch = "amd64";
			}
			int space = osPrefix.indexOf (" ");
			if (space != -1) {
				osPrefix = osPrefix.substring (0, space);
			}
			osPrefix += "-" + arch;
			break;
		}
		return osPrefix;
	}

}
