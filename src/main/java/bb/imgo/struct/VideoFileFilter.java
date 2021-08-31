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
public class VideoFileFilter implements FileFilter {
		
	String ignoreDir;
	
	// Ignore any directory with this name
	//   Can update this to be a set of directories to ignore if necessary
	//   For now it's just a single one since that's all we need
	//   Used for ignoring LivePhotos
	public void setIgnoreDir(String id) {
		this.ignoreDir = id;
	}
	
	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return false;
		}
		
		if (ignoreDir != null && ignoreDir.equals(pathname.getParentFile().getName())) {
			return false;
		}
		
		try {
			String type = Files.probeContentType(pathname.toPath());
			if (type != null && type.startsWith("video")) {
				return true;
			}
		} catch (Exception ex) {			
		}
		
		return false;
	}

}
