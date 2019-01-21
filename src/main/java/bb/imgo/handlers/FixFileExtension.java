package bb.imgo.handlers;

import java.io.File;
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
		extensionMap.put("video/x-msvideo", "avi");
		extensionMap.put("video/quicktime", "mov");
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		if (f1.getBaseFile().getName().startsWith(".")) {
			return false;
		}
		if (f1.getType().startsWith("text")) {
			return false;
		}
		return true;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		String type = f1.getType();
		logger.info("File type: "+type);
	
		String expectedExtension = extensionMap.get(type);
		if (expectedExtension == null) {
			int pos = type.indexOf("/");
			if (pos > -1) {
				expectedExtension = type.substring(pos+1);
			}
		}
		
		if (expectedExtension != null) {
			if (f1.getExt().equalsIgnoreCase(expectedExtension)) {
				logger.debug("Correct file extension for "+f1.getBaseFile());
			} else {
				logger.info("Incorrect file extension "+f1.getExt()+" for "+f1.getBaseFile().getName());
				String fname = f1.getBaseFile().getName();
				String newname = fname;
				String newbase = fname;
				if (fname.indexOf(".") > -1) {
					newbase = fname.substring(0, fname.lastIndexOf("."));
					newname = newbase + "." + expectedExtension;
				} else {
					newname = fname + "." + expectedExtension;
				}
				
				File f2 = new File(f1.getBaseFile().getParentFile(), newname);
				int index=2;
				while (f2.exists()) {
					newname = newbase + "_" + index + "." + expectedExtension;
					f2 = new File(f1.getBaseFile().getParentFile(), newname);
					index++;
				}
				
				main.addRenameActionLog(f1.getBaseFile().getName(), f2.getName(), "Wrong Extension: "+type);
				if (main.moveFiles) {
					try {
						main.moveFile(f1.getBaseFile(), f2);
						f1.setBaseFile(f2);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

}
