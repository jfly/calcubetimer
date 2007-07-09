package net.gnehzr.cct.miscUtils;

import java.io.File;
import javax.swing.filechooser.*;

/* ImageFilter.java is a 1.4 example used by FileChooserDemo2.java. */
public class ImageFilter extends FileFilter {
	public static final String[] IMG_EXTS = { "png", "jpg", "jpeg", "gif", "tif", "tiff" };

	/* Accept all directories and all gif, jpg, tiff, or png files. */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			for(int i = 0; i < IMG_EXTS.length; i++){
				if(extension.equals(IMG_EXTS[i])) return true;
			}
		}

		return false;
	}

	/* The description of this filter */
	public String getDescription() {
		return "Image Files";
	}

	/* Get the extension of a file. */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
}
