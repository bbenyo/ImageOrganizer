package bb.imgo.handlers;

import java.io.IOException;
import java.util.HashMap;

import org.apache.tika.Tika;

import bb.imgo.struct.MediaFile;

/**
 * Figure out the real file type and change the extension to match if it doesnt
 * @author Brett
 *
 */
public class FixFileExtension extends MediaHandler {

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

        Tika tika = new Tika();
		try {
			String type = tika.detect(f1.getBaseFile());
			// TIKA currently thinks that HEIC files are video/quicktime
	        System.out.println("TIKA Determined "+f1.getBaseFile() + " is: " + type);
			if (type.equalsIgnoreCase("video/quicktime") && f1.getExt().equalsIgnoreCase("HEIC")) {
				type = "image/heic";
				System.out.println("\tChanging to "+type);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

}
