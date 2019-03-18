package bb.imgo.handlers;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.VideoFileFilter;
import bb.imgo.ui.ImageGridPanel;
import bb.imgo.ui.VideoGridPanel;

/** 
 * Prompt the user to rename a video that is named ###.avi (no letters)
 * Pop up a UI that lets the user play the video, rename, and tag (good/trash/archive)
 * 
 * Auto name short videos as livephotoX, and auto-archive
 * 
 * @author Brett
 *
 */
public class VideoRenameAndTag extends UserChooser {
	
	static private Logger logger = Logger.getLogger(UserChooser.class.getName());
	
	// Start (or restart) processing directories
	// Return true if we initialized properly, false if there was an error
	public boolean initialize(Properties props) {
		logger.info(getLabel()+" initialized");

		imageFilter = new VideoFileFilter();
		
		columns = 2;
		String cStr = props.getProperty(PropertyNames.VIDEO_RENAME_COLUMNS);
		if (cStr != null) {
			try {
				columns = Integer.parseInt(cStr);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}

		rows = 3;
		String rStr = props.getProperty(PropertyNames.VIDEO_RENAME_ROWS);
		if (rStr != null) {
			try {
				rows = Integer.parseInt(rStr);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}

		String cpFile = props.getProperty(PropertyNames.VIDEO_RENAME_PROGRESS_FILENAME);
		if (cpFile != null) {
			currentProgressFilename = cpFile;
		}

		currentProgressFile = new File(currentProgressFilename);
		if (!currentProgressFile.exists()) {
			if (currentProgressFile.getParentFile() != null) {
				currentProgressFile.getParentFile().mkdirs();
			}
		}

		String useProgressStr = props.getProperty(PropertyNames.VIDEO_RENAME_USE_PROGRESS);
		if (useProgressStr != null) {
			if (!Boolean.parseBoolean(useProgressStr)) {
				currentProgressDirectory = null;
				currentProgressFile = null;
				// Don't use any previous progress
			}
		}	
		
		boolean ret = readCurrentProgressFile();
		return ret;
	}
	
	
	protected ImageGridPanel createImageGridPanel(File directory) {
		return new VideoGridPanel(directory, mediaFiles, columns, rows);
	}
	
}
