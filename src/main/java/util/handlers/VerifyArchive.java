package util.handlers;

import util.struct.MediaFile;

// Verify that a defined archive directory has a copy of every file here
public class VerifyArchive extends MediaHandler {

	public VerifyArchive() {
		super();
	}
	
	@Override
	public boolean handleFile(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true; 
	}

}
