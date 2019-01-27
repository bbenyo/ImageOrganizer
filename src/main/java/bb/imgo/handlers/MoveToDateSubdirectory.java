package bb.imgo.handlers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import bb.imgo.struct.MediaFile;

/**
 * If a file is NOT in a date subdirectory (named with YYYY-MM-DD), and is instead in a YYYY only directory
 *   move it to the proper YYYY-MM-DD subdirectory based on the creation date or the last mod time
 *   
 * If files are in YYYY-MM, move them to YYYY-MM-DD  
 *  
 * Do not move files that are not in a YYYY subdir (i.e. wedding or disney, etc), regardless of creation date
 * @author Brett
 *
 */
public class MoveToDateSubdirectory extends MediaHandler {
	static private Logger logger = Logger.getLogger(MoveToDateSubdirectory.class.getName());
	
	SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
	SimpleDateFormat yyyymm = new SimpleDateFormat("yyyy-MM");
	SimpleDateFormat yyyymmUnderscore = new SimpleDateFormat("yyyy_MM");
	SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat yyyymmddUnderscore = new SimpleDateFormat("yyyy_MM_dd");

	boolean handlingDirectory = false;
	
	// TODO: Add this as a init parameter
	boolean convertUnderscoreToDash = true;
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		if (handlingDirectory) {
			if (f1.isImageFile() || f1.isVideoFile()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void directoryInit(File dir) {
		handlingDirectory = false;
		String dirname = dir.getName();
		if (dirname.length() > 10) {
			return;
		}
		
		if (convertUnderscoreToDash) {
			// Rename underscore directories to dash directories
			try {
				yyyymmUnderscore.parse(dirname);
				handlingDirectory = true;
				logger.info("Handling yyyy_mm directory: "+dir);
				return;				
			} catch (ParseException e) {
			}

			try {
				yyyymmddUnderscore.parse(dirname);
				handlingDirectory = true;
				logger.info("Handling yyyy_mm_dd directory: "+dir);
				return;
			} catch (ParseException e) {
			}
		}
		
		try {
			yyyymm.parse(dirname);
			handlingDirectory = true;
			logger.info("Handling yyyy-MM directory: "+dir);
			return;
		} catch (ParseException e) {
		}
		
		try {
			yyyy.parse(dirname);
			handlingDirectory = true;
			logger.info("Handling yyyy directory: "+dir);
			return;
		} catch (ParseException e) {
		}
		
		try {
			yyyymmdd.parse(dirname);
			handlingDirectory = true;
			logger.info("Handling yyyy-MM-DD directory: "+dir);
			return;
		} catch (ParseException e) {
		}	
		
	}
	
	@Override
	public void directoryComplete(File dir) {
		handlingDirectory = false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// Where should this file go?
		File f = f1.getBaseFile();
		long time = f1.getOriginalTimestamp();
		if (time <= 0 || time < 347155200000l) { // Before 1981 means we probably have the wrong creation date (camera time was not set)
			//  So in that case, we'll use last mod
			if (time > 0) {
				logger.info("Creation time was before 1981: "+time+", so we're using the last mod time instead");
			}
			logger.debug("Using lastmodified time for "+f.getName());
			time = f.lastModified();
		}
		Date d = new Date(time);
		logger.debug("Timestamp "+d+" for "+f.getAbsolutePath());
		
		String p1name = yyyymmdd.format(d);
		String p2name = yyyy.format(d);
		
		File pf = f.getParentFile();
		if (pf != null && pf.getName().equals(p1name)) {
			logger.debug("Correct parent directory: "+p1name);
			pf = pf.getParentFile();
			if (pf != null && pf.getName().equals(p2name)) {
				logger.debug("Correct year parent directory: "+p2name);
				return false;
			} else {
				logger.info("Incorrect year parent directory: "+pf);
			}
		} else {
			logger.info("Incorrect parent directory: "+pf);
		}
		
		File cp1 = f;
		boolean moveUp = true;
		
		// Looking for proper root directory
		while (cp1 != null && moveUp) {
			cp1 = cp1.getParentFile();
			moveUp = false;
			String cpname = cp1.getName();
			// cp1 should be yyyy
			try {
				yyyy.parse(cpname);
				moveUp = true;
			} catch (ParseException e) {
			}
			
			try {
				yyyymm.parse(cpname);
				moveUp = true;
			} catch (ParseException e) {
			}
		}	
		
		File correctPath = null;
		String cDirs = p2name+"/"+p1name+"/"+f.getName();
		if (cp1 != null) {
			correctPath = new File(cp1, cDirs);
		} else {
			correctPath = new File(cDirs);
		}
		logger.debug("Correct Path: "+correctPath);
		main.addRenameActionLog(f.getAbsolutePath(), correctPath.getAbsolutePath(), "MoveToDateDir");
		
		if (main.moveFiles) {
			correctPath.getParentFile().mkdirs();
			if (main.moveFiles) {
				try {
					main.moveFile(f, correctPath);
					f1.setBaseFile(correctPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}

}
