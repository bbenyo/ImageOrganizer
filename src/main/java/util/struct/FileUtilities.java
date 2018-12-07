package util.struct;

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
	
	/**
	 * Convenience method to copy a file from a source to a destination.
	 * Overwrite is prevented, and the last modified is kept.
	 *
	 * @param sourceFile String filename
	 * @param destFile String filename
	 *
	 * @throws IOException
	 */

	static public void copyFile(String sourceFile, String destFile) throws IOException {
		copyFile(new File(sourceFile), new File(destFile), false, true);
	}

	/**
	 * Method to copy a file from a source to destination specifying if
	 * source files may overwrite newer destination files and the
	 * last modified time of <code>destFile</code> file should be made equal
	 * to the last modified time of <code>sourceFile</code>.
	 *
	 * @param sourceFile File
	 * @param destFile File
	 * @param overwrite boolean whether to overwrite the destination file if it exists
	 * @param preserveLastModified boolean whether to change the lastModifiied file value or not
	 *        if true, the dest file will have the same lastModified as the source file
	 *        if false, the dest file will have lastModified set to now.
	 *
	 * @throws IOException
	 */
	
	static public void copyFile(File sourceFile, File destFile, boolean overwrite, boolean preserveLastModified)
			throws IOException {

		if (overwrite || !destFile.exists() ||
				destFile.lastModified() < sourceFile.lastModified()) {

			if (destFile.exists() && destFile.isFile()) {
				destFile.delete();
			}

			File parent = new File(destFile.getParent());
			if (!parent.exists()) {
				parent.mkdirs();
			}

			FileInputStream in = new FileInputStream(sourceFile);
			FileOutputStream out = new FileOutputStream(destFile);

			byte[] buffer = new byte[8 * 1024];
			int count = 0;
			do {
				out.write(buffer, 0, count);
				count = in.read(buffer, 0, buffer.length);
			} while (count != -1);

			in.close();
			out.close();

			if (preserveLastModified) {
				destFile.setLastModified(sourceFile.lastModified());
			}
		}
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
}

