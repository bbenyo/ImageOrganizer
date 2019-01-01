package bb.imgo.struct;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;

public class MediaFile {

	static private Logger logger = Logger.getLogger(MediaFile.class.getName());
	
	File baseFile;
	String ext = null;  // File extension: Null extension is unknown or no extension
	String type = null;
	
	// Tags as a bitmask to keep the structure size down
	// These values are 1 << n
	static public int TAG_DELETE = 0x1;
	static public int TAG_GOOD = 0x2;
	// next tag = 0x4

	int tag = 0;
	
	Tika tika = new Tika();
	
	static public SimpleDateFormat ymdhm = new SimpleDateFormat("YYYY-MM-DD HH:mm");
	
	public MediaFile(File f) {
		this.baseFile = f;
		ext = FileUtilities.getExtension(f).toLowerCase();
		// Figure out the real type with apache tika
		try {
			type = tika.detect(f);
			// TIKA currently thinks that HEIC files are video/quicktime
			logger.info("TIKA Determined "+f+ " is: " + type);
			if (type.equalsIgnoreCase("video/quicktime") && ext.equalsIgnoreCase("HEIC")) {
				type = "image/heic";
				logger.info("\tChanging to "+type);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Accessors
	
	public File getBaseFile() {
		return baseFile;
	}
	
	public URL getURL() {
		try {
			return baseFile.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setBaseFile(File baseFile) {
		this.baseFile = baseFile;
	}
	
	public String getDateTime() {
		long time = baseFile.lastModified();
		Date d = new Date(time);
		return ymdhm.format(d);
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}
	
	public void setTag(int t) {
		tag |= t;
	}
	
	public void clearTag(int t) {
		tag &= ~t;
	}
	
	public boolean isTag(int t) {
		int v = tag & t;
		if (v == 0) {
			return false;
		}
		return true;
	}
	
	public void setGood() {
		setTag(TAG_GOOD);
	}
	
	public boolean isGood() {
		return isTag(TAG_GOOD);
	}
	
	public void setDelete() {
		setTag(TAG_DELETE);
	}
	
	public boolean isDelete() {
		return isTag(TAG_DELETE);
	}
	
	public void clearGood() {
		clearTag(TAG_GOOD);
	}
	
	public void clearDelete() {
		clearTag(TAG_DELETE);
	}
	
	public boolean isImageFile() {
		if (type != null && type.startsWith("image")) {
			return true;
		}
		return false;
	}
	
	public boolean isVideoFile() {
		if (type != null && type.startsWith("video")) {
			return true;
		}
		return false;
	}
	
	// If we were going to move this file from oldroot dir to newRoot dir, what's the new absolute pathname?
	public File getNewFilePath(File oldRoot, File newRoot) {
		File pFile = baseFile;
		String relativePath = "";
		while (pFile != null && !pFile.equals(oldRoot)) {
			relativePath = pFile.getName() + "/" + relativePath;
			pFile = pFile.getParentFile();
		}
		
		if (relativePath == "") {
			return newRoot;
		}
		return new File(newRoot, relativePath);
	}
	
}
