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
 *   move it to the proper YYYY-MM-DD subdirectory based on the file timestamp
 * If files are in YYYY-MM, move them to YYYY-MM-DD  
 *  
 * Do not move files that are not in a YYYY subdir (i.e. wedding or disney, etc)
 * @author Brett
 *
 */
public class MoveToDateSubdirectory extends MediaHandler {
	static private Logger logger = Logger.getLogger(MoveToDateSubdirectory.class.getName());
	
	SimpleDateFormat yyyy = new SimpleDateFormat("YYYY");
	SimpleDateFormat yyyymm = new SimpleDateFormat("YYYY-MM");
	SimpleDateFormat yyyymmdd = new SimpleDateFormat("YYYY-MM-dd");

	boolean handlingDirectory = false;
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		return handlingDirectory;
	}
	
	@Override
	public void directoryInit(File dir) {
		handlingDirectory = false;
		String dirname = dir.getName();
		if (dirname.length() > 7) {
			return;
		}
		
		try {
			yyyymm.parse(dirname);
			handlingDirectory = true;
			logger.info("Handling YYYY-MM directory: "+dir);
			return;
		} catch (ParseException e) {
		}
		
		try {
			yyyy.parse(dirname);
			handlingDirectory = true;
			logger.info("Handling YYYY directory: "+dir);
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
		long time = f.lastModified();
		Date d = new Date(time);
		logger.debug("LastMod: "+d+" for "+f.getAbsolutePath());
		
		String p1name = yyyymmdd.format(d);
		String p2name = yyyy.format(d);
		
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
		main.addRenameActionLog(f.getAbsolutePath(), correctPath.getAbsolutePath());
		
		if (main.moveFiles) {
			correctPath.mkdirs();
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
