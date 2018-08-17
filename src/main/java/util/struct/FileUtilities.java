package util.struct;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

/** 
 * General utility methods for handling files
 * @author Brett
 *
 */
public class FileUtilities {
	
	static private Logger logger = Logger.getLogger(FileUtilities.class.getName());

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
	
	/**
	 * Look for a file via the classloader or as a pathname if the loader fails to find it 
	 * @param fileUri
	 * @return
	 */
	static public File findFile(String fileUri) {
       URL url2 = FileUtilities.class.getClassLoader().getResource(fileUri);
       if (url2 != null) {
    	   try {
    		   URI uri = url2.toURI();
    		   logger.debug("Found "+fileUri+" via classloader");
    		   return new File(uri);
    	   } catch (IllegalArgumentException ex) {
    		   logger.warn(ex.toString());   
    		   ex.printStackTrace();
    	   } catch (URISyntaxException e) {
    		   logger.warn(e.toString());
    		   e.printStackTrace();
    	   }
       } else {
    	   File f = new File(fileUri);
    	   if (f.exists()) {
    		   logger.debug("Found "+f.getAbsolutePath()+" via pathname");
    		   return f;
    	   }
       }
       logger.warn("Unable to find "+fileUri+" via classloader or pathname");
       return null;
	}
	
}
