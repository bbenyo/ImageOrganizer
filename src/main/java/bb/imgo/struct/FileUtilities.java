package bb.imgo.struct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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

	public static String readFileContents(File file) throws FileNotFoundException, IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuffer buf = new StringBuffer();
		String line;
		String lineSep = System.getProperty("line.separator");
		boolean first = true;

		while(true) {
			if (!first) {
				buf.append(lineSep);
			} else {
				first = false;
			}

			line = reader.readLine();
			if(line == null) {
				break;
			}
			buf.append(line);
		}

		reader.close();
		return buf.toString();
	}
	
	static public boolean deleteDirectoryContents(File dir) {
		if (!dir.isDirectory()) {
			logger.error(dir+" isn't a directory in deleteDirectoryContents!");
			return false;
		}
		
		File[] files = dir.listFiles();
		boolean allSuccessful = true;
		int count = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				deleteDirectoryContents(f);
			}
			if (!f.delete()) { 
				allSuccessful = false;
			} else {
				count++;
			}
		}
		
		logger.info("Deleted "+count+" files from "+dir);
		return allSuccessful;		
	}
	
	static public String humanReadableBytes(double bytes) {
		if (Double.isNaN(bytes)) {
			return "-";
		}
		// If less than 1K, report bytes
		if (bytes <= 0) {
			return String.format("%d B", (long)bytes);
		}
		int exponent = (int)(Math.log10(bytes) / 3.0);
		float val = (float)(bytes / Math.pow(1000, exponent));
		String num = "";
		if (val == (long)val)
			num = String.format("%d", (long)val);
		else
			num = String.format("%.3f", val);
		switch (exponent) {
		case 0 : return num+" B"; 
		case 1 : return num+" K";
		case 2 : return num+" M";
		case 3 : return num+" G";
		case 4 : return num+" T";
		case 5 : return num+" P";
		case 6 : return num+" E";
		default :
			return bytes+" (Huge!?!)";
		} 
	}
}

