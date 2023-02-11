package bb.imgo.handlers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.MediaFile;

/**
 * Move video files to a different root directory if any are present here
 * @author Brett
 *
 */
public class SeparateVideos extends MediaHandler {
	static private Logger logger = Logger.getLogger(SeparateVideos.class.getName());
	
	File videoRoot = null;
	String ignoreDir = null;
	
	public boolean initialize(Properties props) {
		logger.info(getLabel()+" initialized");
		String vrname = props.getProperty(PropertyNames.VIDEO_ROOT_DIR);
		if (vrname != null) {
			videoRoot = new File(vrname);
		} else {
			videoRoot = new File("Videos");
		}
		
		logger.info("VideoRoot: "+videoRoot.getAbsolutePath());
		if (videoRoot.exists()) {
			return true;
		} else {
			logger.error(videoRoot+" doesn't exist!");
		}
		
		// Ignore livephoto subdir
		String livePhotoDir = props.getProperty(PropertyNames.LIVEPHOTO_SUB_DIR);
		if (livePhotoDir != null) {
			ignoreDir = livePhotoDir;
		}
		
		return false;
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		if (f1.getBaseFile().getParentFile().getName().equalsIgnoreCase(ignoreDir)) {
			logger.debug("In ignored subdir: "+ignoreDir+": skipping SeparateVideos");
			return false;
		}
		if (f1.isVideoFile()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// Video file, move it
		File moveTo = f1.getNewFilePath(main.getRootDirectory(), videoRoot);
		main.addRenameActionLog(f1.getBaseFile().getAbsolutePath(), moveTo.getAbsolutePath(), "Video File");
		if (main.moveFiles) {
			try {
				main.moveFile(f1.getBaseFile(), moveTo);
				f1.setBaseFile(moveTo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public String getDescription() {
		return "Move any Video files to "+videoRoot;
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		HashMap<String, String> configs = new HashMap<String, String>();
		if (videoRoot != null) {
			configs.put(PropertyNames.VIDEO_ROOT_DIR, videoRoot.getAbsolutePath());
		} else {
			configs.put(PropertyNames.VIDEO_ROOT_DIR, "");
		}
		return configs;
	}

	@Override
	public void setConfigurationOption(String key, String value) {
		if (key.equalsIgnoreCase(PropertyNames.VIDEO_ROOT_DIR)) {
			File v1 = new File(value);
			if (!v1.exists()) {
				logger.error("New video root doesn't exist: "+value);
				return;
			}			
			logger.info("Setting VideoRoot to "+v1.getAbsolutePath());
		}		
	}

}
