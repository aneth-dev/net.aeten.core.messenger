package net.aeten.core.jna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.sun.jna.Platform;

/** TODO Remove JNA depedencies */
public class JarTmpExtractor {

	public static File extract (	Class <?> loadinClass,
											String file) {
		String arch = System.getProperty ("os.arch");
		String name = System.getProperty ("os.name");
		String resourceName = LibraryLoader.getNativeLibraryResourcePath (Platform.getOSType (), arch, name) + "/" + file;
		URL url = loadinClass.getResource (resourceName);
		if (url == null) {
			throw new UnsatisfiedLinkError ("JarTmpExtractor (" + resourceName + ") not found in resource path");
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
				File dir = getTempDir (loadinClass.getName ());
				lib = new File (dir, file);
				if (lib.exists ()) {
					return lib;
				}
				fos = new FileOutputStream (lib);
				int count;
				byte[] buf = new byte[1024];
				while ( (count = is.read (buf, 0, buf.length)) > 0) {
					fos.write (buf, 0, count);
				}
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
		return lib;
	}

	static File getTempDir (String fileName) {
		File tmpdir = new File (System.getProperty ("java.io.tmpdir"));
		File filetmp = new File (tmpdir, fileName + "-" + System.getProperty ("user.name"));
		filetmp.mkdirs ();
		return filetmp.exists ()? filetmp: tmpdir;
	}

}
