package bb.imgo.handlers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
	// Instead of doing md5s, the probability of two images/videos with the exact same size in bytes is very low
	//  We'll only bother with an md5 if there's a file with the same size.
	//HashMap<String, String> md5Checksums = new HashMap<String, String>();
	
	// FileSize -> filename, for a quick check before bothering with an md5
	HashMap<Long, String> sizeToName = new HashMap<Long, String>();
	
	public RemoveDuplicates() {
		super();
	}
	
	@Override
	public boolean directoryInit(File directory) {
		super.directoryInit(directory);
		//md5Checksums.clear();
		sizeToName.clear();
		curDirectory = directory;
		return true;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		try {
			long size = f1.getBaseFile().length();
			String sameSize = sizeToName.get(size);
			if (sameSize == null) {
				// No prev file with this name
				sizeToName.put(size, f1.getBaseFile().getAbsolutePath());
				return false;				
			} 
			logger.debug("Found file "+sameSize+" with the same size as "+f1.getBaseName()+", checking md5s");
			String cs = MD5Checksum.getMD5Checksum(f1.getBaseFile().getAbsolutePath());
			String cs2 = MD5Checksum.getMD5Checksum(sameSize);
			logger.debug("MD5 for "+f1.getBaseFile().getAbsolutePath()+": "+cs);
			logger.debug("MD5 for "+sameSize+": "+cs2);
			if (cs != null && cs.equals(cs2)) {
				if (!ignoreName && !sameSize.equals(f1.getBaseFile().getAbsolutePath())) {
					logger.info("Found duplicate but file names are different: "+f1+" and "+sameSize);
					return false;
				}
				logger.info("Duplicate found: "+f1.getBaseFile().getName()+" and "+sameSize);
				f1.setDelete("Duplicate Found");
				return true;
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

	@Override
	public String getDescription() {
		return "Use file size and MD5 checksums to remove any duplicates";
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		return new HashMap<String, String>();
	}

	@Override
	public void setConfigurationOption(String key, String value) {
	}

}
