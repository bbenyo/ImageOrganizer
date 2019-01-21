package bb.imgo.handlers;

import bb.imgo.struct.MediaFile;

// Verify that a defined archive directory has a copy of every file here
public class VerifyArchive extends MediaHandler {

	public VerifyArchive() {
		super();
	}
	
	@Override
	public boolean handleFile(MediaFile f1) {
		return false;
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true; 
	}

}
