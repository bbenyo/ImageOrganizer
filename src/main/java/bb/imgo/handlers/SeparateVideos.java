package bb.imgo.handlers;

import java.io.File;
import java.io.IOException;
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
	
	public void initialize(Properties props) {
		logger.info(getLabel()+" initialized");
		String vrname = props.getProperty(PropertyNames.VIDEO_ROOT_DIR);
		if (vrname != null) {
			videoRoot = new File(vrname);
		} else {
			videoRoot = new File("Videos");
		}
		
		logger.info("VideoRoot: "+videoRoot.getAbsolutePath());
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
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

}
