package bb.imgo.handlers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import bb.imgo.struct.MediaFile;

public class DirectoryNameUnderscoreToDash extends MediaHandler {
	static private Logger logger = Logger.getLogger(DirectoryNameUnderscoreToDash.class.getName());
	
	SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat yyyymmddUnderscore = new SimpleDateFormat("yyyy_MM_dd");
	
	// TODO: Need to run this alone, or restart processing back in main.  Once we rename, we can't iterate through the old directory
	@Override
	public boolean directoryInit(File directory) {
		logger.debug(label+" DirectoryInit");
		String name = directory.getName();
		try {
			Date d1 = yyyymmddUnderscore.parse(name);
			logger.info(name+" is in underscore format, changing to dash");
			String newname = yyyymmdd.format(d1);
			main.addRenameActionLog(name, newname, "Underscore to Dash");
			if (main.moveFiles) {
				try {
					main.moveFile(directory, new File(directory.getParentFile(), newname));
					return false;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		} catch (ParseException ex) {
		}
		return true;
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		return false;
	}

}
