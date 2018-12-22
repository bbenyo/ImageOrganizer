package bb.imgo.handlers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import bb.imgo.MD5Checksum;
import bb.imgo.struct.MediaFile;

/**
 * Remove duplicate files that are in the same directory
 * @author Brett
 *
 */
public class RemoveDuplicates extends MediaHandler {
	static private Logger logger = Logger.getLogger(RemoveDuplicates.class.getName());
	
	boolean sameDirectoryOnly = true;
	boolean ignoreName = true;  // Remove duplicates even if they're named differently
	
	File curDirectory = null;
	
	// MD5 -> filename
	HashMap<String, String> md5Checksums = new HashMap<String, String>();
	
	public RemoveDuplicates() {
		super();
	}
	
	@Override
	public void directoryInit(File directory) {
		super.directoryInit(directory);
		md5Checksums.clear();
		curDirectory = directory;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		try {
			String cs = MD5Checksum.getMD5Checksum(f1.getBaseFile().getAbsolutePath());
			logger.debug("MD5 for "+f1.getBaseFile().getAbsolutePath()+": "+cs);
			if (md5Checksums.containsKey(cs)) {
				String dName = md5Checksums.get(cs);
				if (!ignoreName && dName.equals(f1.getBaseFile().getName())) {
					logger.info("Found duplicate but file names are different: "+f1+" and "+dName);
					return false;
				}
				logger.info("Duplicate found: "+f1.getBaseFile().getName()+" and "+dName);
				f1.setDelete();
				return true;
			} else {
				md5Checksums.put(cs, f1.getBaseFile().getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return false;
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true; 
	}

}
