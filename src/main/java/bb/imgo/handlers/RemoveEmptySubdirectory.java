package bb.imgo.handlers;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.ActionLog;
import bb.imgo.struct.MediaFile;

/**
 * As it says, if a subdirectory has no files, remove it
 * @author Brett
 *
 */
public class RemoveEmptySubdirectory extends MediaHandler {
	static private Logger logger = Logger.getLogger(RemoveEmptySubdirectory.class.getName());
	boolean logOnly = true;
	
	@Override
	public boolean initialize(Properties props) {
		logOnly = !main.moveFiles;
		logger.info(getLabel()+" initialized, logOnly: "+logOnly);
		return true;
	}
		
	@Override
	public void directoryComplete(File directory) {
		logger.info("Checking for files in "+directory);
		if (directory.listFiles().length == 0) {
			logger.info("Removing empty directory: "+directory.getAbsolutePath());
			try {
				if (!logOnly) {
					directory.delete();
				}
				addDeleteActionLog(directory.getAbsolutePath(), "Empty Dir");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			File[] files = directory.listFiles();
			if (files.length > 2) {
				return; // Definitely not empty
			}
			// If the only files are Thumbs.db, and Thumbnail.info, delete it
			for (File f : files) {
				if (!f.getName().equals("Thumbs.db") && !f.getName().equals("ZbThumbnail.info")) {
					return;
				}
			}
			logger.debug("Only thumbnail files remain, deleting directory");
			addDeleteActionLog(directory.getAbsolutePath(), "Empty Dir");
			if (!logOnly) {
				files = directory.listFiles();
				for (File f : files) {
					f.delete();
				}
				directory.delete();
			}
		}
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		return false;
	}

}
