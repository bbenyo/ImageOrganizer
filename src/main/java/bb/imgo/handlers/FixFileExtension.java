package bb.imgo.handlers;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;

import bb.imgo.struct.MediaFile;

/**
 * Figure out the real file type and change the extension to match if it doesnt
 * @author Brett
 *
 */
public class FixFileExtension extends MediaHandler {
	
	static private Logger logger = Logger.getLogger(FixFileExtension.class.getName());
	static HashMap<String, String> extensionMap = new HashMap<String, String>();
	
	static {
		// Default is to use string after the /
		// So image/png = png, etc.
		extensionMap.put("text/plain", "txt");
		extensionMap.put("image/jpeg", "jpg");
		extensionMap.put("image/heic", "heic");
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		return true;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		String type = f1.getType();
		logger.info("File type: "+type);
		return false;
	}

}
