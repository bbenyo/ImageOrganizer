package util.handlers;

import java.io.File;

import util.struct.MediaFile;

// Verify that a defined archive directory has a copy of every file here
public class VerifyArchive extends MediaHandler {

	public VerifyArchive() {
		super();
	}
	
	@Override
	public void directoryInit(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void directoryComplete(File directory) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true; 
	}

	@Override
	public void subDirectoryInit(File directory, File subDirectory) {
		// TODO Auto-generated method stub
	}

	@Override
	public void subDirectoryComplete(File directory, File subDirectory) {
		// TODO Auto-generated method stub
	}

}
