package bb.imgo.struct;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;

/**
 * Here we assume that the file extensions have been fixed already, so we'll check that the file is an image 
 *   by only looking at the extension
 * Checking via TIKA here would be a bit too slow, an unnecessary, since we can just run FixFileExtension first. 
 * @author Brett
 *
 */
public class ImageFileFilter implements FileFilter {
		
	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return false;
		}
		
		try {
			String type = Files.probeContentType(pathname.toPath());
			if (type != null && type.startsWith("image") && !type.equalsIgnoreCase("image/heic")) {
				return true;
			}
		} catch (Exception ex) {			
		}
		
		return false;
	}

}
