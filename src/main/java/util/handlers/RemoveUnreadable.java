package util.handlers;

import java.io.File;

import util.struct.MediaFile;

// Make sure the file is valid and can be read. 
public class RemoveUnreadable extends MediaHandler {

	@Override
	public boolean fileFilter(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void directoryInit(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void directoryComplete(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subDirectoryInit(File directory, File subDirectory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subDirectoryComplete(File directory, File subDirectory) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

}