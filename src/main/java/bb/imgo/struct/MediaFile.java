package bb.imgo.struct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class MediaFile {

	static private Logger logger = Logger.getLogger(MediaFile.class.getName());
	
	File baseFile;
	String ext = null;  // File extension: Null extension is unknown or no extension
	private String type = null;
	
	// TODO: Since there are only 2 options, we don't really need a bitmask...  Replace with 2 flags
	// Tags as a bitmask to keep the structure size down
	// These values are 1 << n
	static public int TAG_DELETE = 0x1;
	static public int TAG_GOOD = 0x2;
	// next tag = 0x4

	int tag = 0;
	
	Tika tika = new Tika();

	static public SimpleDateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	static public SimpleDateFormat tikaDate = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	
	private String deleteReason;
	private String goodReason;
	
	private long originalTimestamp = -1;
	
	public MediaFile(File f) {
		this.baseFile = f;
		ext = FileUtilities.getExtension(f).toLowerCase();		
	}
		
	// Accessors
	
	public File getBaseFile() {
		return baseFile;
	}
	
	public String getDeleteReason() {
		return deleteReason;
	}

	public void setDeleteReason(String deleteReason) {
		this.deleteReason = deleteReason;
	}

	public String getGoodReason() {
		return goodReason;
	}

	public void setGoodReason(String goodReason) {
		this.goodReason = goodReason;
	}

	public URL getURL() {
		try {
			return baseFile.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getType() {
		if (type == null) {
			// Figure out the real type with apache tika the first time it's needed.
			try {
				type = tika.detect(baseFile);
				// TIKA currently thinks that HEIC files are video/quicktime
				logger.info("TIKA Determined "+baseFile+ " is: " + type);
				if (type.equalsIgnoreCase("video/quicktime") && ext.equalsIgnoreCase("HEIC")) {
					type = "image/heic";
					logger.info("\tChanging to "+type);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (type == null) {
				type = "unknown";
			}
		}
		return type;
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
	
	// File name without the extension
	public String getBaseName() {
		int pos = baseFile.getName().lastIndexOf(".");
		if (pos > -1) {
			return baseFile.getName().substring(0, pos);
		}
		return baseFile.getName();
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
	
	public void setGood(String reason) {
		setTag(TAG_GOOD);
		this.goodReason = reason;
	}
	
	public boolean isGood() {
		return isTag(TAG_GOOD);
	}
	
	public void setDelete(String reason) {
		setTag(TAG_DELETE);
		this.deleteReason = reason;
	}
	
	public boolean isDelete() {
		return isTag(TAG_DELETE);
	}
	
	public void clearGood() {
		clearTag(TAG_GOOD);
		goodReason = null;
	}
	
	public void clearDelete() {
		clearTag(TAG_DELETE);
		deleteReason = null;
	}
	
	public boolean isImageFile() {
		if (type == null) {
			getType();
		}
		if (type.startsWith("image")) {
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
	
	/**
	 * Get the Original creation date for a file by using TIKA to get the Date/Time Original metadata field
	 * Use tika the first time this is called, if we fail to find it, return 0
	 * 
	 * A value of -1 means we haven't tried to get the orig date yet
	 * A value of 0 means we tried and didn't find it
	 * Any other value should be the timestamp for the orig creation date
	 * 
	 * @return
	 */
	public long getOriginalTimestamp() {
		if (originalTimestamp == -1) {
			Parser parser = new AutoDetectParser();
			BodyContentHandler handler = new BodyContentHandler();
			Metadata metadata = new Metadata();   //empty metadata object 
			ParseContext context = new ParseContext();
			FileInputStream inputstream = null;
			try {
				inputstream = new FileInputStream(baseFile);
				parser.parse(inputstream, handler, metadata, context);				
				String oDate = metadata.get("Date/Time Original");
				if (oDate == null) {
					originalTimestamp = 0; // don't bother trying again
					return originalTimestamp;
				}
				Date d1 = tikaDate.parse(oDate);
				logger.info("Original Date: "+d1);
				originalTimestamp = d1.getTime();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (TikaException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} finally {
				if (inputstream != null) {
					try {
						inputstream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return originalTimestamp;
	}
		
}
