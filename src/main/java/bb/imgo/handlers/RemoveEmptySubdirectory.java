package bb.imgo.handlers;

import java.io.File;

import org.apache.log4j.Logger;

import bb.imgo.struct.ActionLog;
import bb.imgo.struct.MediaFile;

/**
 * As it says, if a subdirectory has no files, remove it
 * @author Brett
 *
 */
public class RemoveEmptySubdirectory extends MediaHandler {
	static private Logger logger = Logger.getLogger(RemoveDuplicates.class.getName());
	
	@Override
	public void directoryComplete(File directory) {
		if (directory.listFiles().length == 0) {
			logger.info("Removing empty directory: "+directory.getAbsolutePath());
			try {
				directory.delete();
				addActionLog(directory.getAbsolutePath(), ActionLog.Action.DELETE);
			} catch (Exception ex) {
				ex.printStackTrace();
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
