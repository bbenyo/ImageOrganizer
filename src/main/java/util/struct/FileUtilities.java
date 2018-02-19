package util.struct;

import java.io.File;

/** 
 * General utility methods for handling files
 * @author Brett
 *
 */
public class FileUtilities {

	static public String getExtension(File f) {
		if (f != null) {
			int pos = f.getName().lastIndexOf(".");
			if (pos > -1) {
				return f.getName().substring(pos+1);
			} else {
				return "";
			}
		} 
		return null;
	}
	
}
