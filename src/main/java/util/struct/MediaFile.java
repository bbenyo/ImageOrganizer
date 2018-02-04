package util.struct;

import java.io.File;

public class MediaFile {

	File baseFile;
	String ext = null;  // File extension: Null extension is unknown or no extension
	
	// Tags as a bitmask to keep the structure size down
	// These values are 1 << n
	static public int TAG_DELETE = 0x1;
	static public int TAG_GOOD = 0x2;
	// next tag = 0x4

	int tag = 0;
	
	public MediaFile(File f) {
		this.baseFile = f;
		try {
			int dPos = f.getName().indexOf(".");
			if (dPos > -1) {
				ext = f.getName().substring(dPos+1);
			} else {
				ext = null;
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	// Accessors
	
	public File getBaseFile() {
		return baseFile;
	}

	public void setBaseFile(File baseFile) {
		this.baseFile = baseFile;
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
	
}
