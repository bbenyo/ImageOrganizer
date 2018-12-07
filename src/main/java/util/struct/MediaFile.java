package util.struct;

import java.io.File;
import java.util.ArrayList;

public class MediaFile {

	File baseFile;
	String ext = null;  // File extension: Null extension is unknown or no extension
	
	// Tags as a bitmask to keep the structure size down
	// These values are 1 << n
	static public int TAG_DELETE = 0x1;
	static public int TAG_GOOD = 0x2;
	// next tag = 0x4

	int tag = 0;

	static ArrayList<String> ImageFileExtensions = new ArrayList<String>();
	static ArrayList<String> VideoFileExtensions = new ArrayList<String>();
	
	static {
		ImageFileExtensions.add("gif");
		ImageFileExtensions.add("jpg");
		ImageFileExtensions.add("png");
		ImageFileExtensions.add("bmp");
		ImageFileExtensions.add("tiff");
		ImageFileExtensions.add("heic");
		
		VideoFileExtensions.add("mp4");
		VideoFileExtensions.add("mov");
	}
	
	public MediaFile(File f) {
		this.baseFile = f;
		ext = FileUtilities.getExtension(f).toLowerCase();
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
	
	public boolean isImageFile() {
		if (ext != null && ImageFileExtensions.contains(ext)) {
			return true;
		}
		return false;
	}
	
	public boolean isVideoFile() {
		if (ext != null && VideoFileExtensions.contains(ext)) {
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
