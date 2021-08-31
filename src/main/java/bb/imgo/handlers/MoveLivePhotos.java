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
 * Figure out if this is a Live Photo (short mov) and if so, move it to a LivePhotos subdirectory
 *
 * @author Brett
 *
 */
public class MoveLivePhotos extends MediaHandler {
	static private Logger logger = Logger.getLogger(MoveLivePhotos.class.getName());
	
	String livePhotoSubdir = "LivePhotos";
	long livePhotoMaxSize = 6000000; // 6 mb
	
	public boolean initialize(Properties props) {
		logger.info(getLabel()+" initialized");
		return true;
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		if (f1.getBaseFile().getParentFile().getName().equalsIgnoreCase(livePhotoSubdir)) {
			logger.debug("In LivePhoto subdir: "+livePhotoSubdir+": skipping MoveLivePhotos");
			return false;
		}
		String type = f1.getType();
		logger.info("type: "+type);
		if (f1.isVideoFile() && f1.getExt().equals("mov")) {
			if (f1.getBaseFile().length() < livePhotoMaxSize) { 
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// Video file, move it
		File liveDir = new File(f1.getBaseFile().getParentFile(), livePhotoSubdir);
		File moveTo = new File(liveDir, f1.getBaseFile().getName());
		main.addRenameActionLog(f1.getBaseFile().getAbsolutePath(), moveTo.getAbsolutePath(), "Live Photo");
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
		return "Move any Live Photos to subdir "+livePhotoSubdir;
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		HashMap<String, String> configs = new HashMap<String, String>();
		if (livePhotoSubdir != null) {
			configs.put(PropertyNames.LIVEPHOTO_SUB_DIR, livePhotoSubdir);
		} else {
			configs.put(PropertyNames.LIVEPHOTO_SUB_DIR, "");
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
