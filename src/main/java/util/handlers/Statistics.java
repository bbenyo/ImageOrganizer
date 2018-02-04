package util.handlers;

import java.io.File;

import util.struct.MediaFile;

public class Statistics extends MediaHandler {

	public Statistics() {
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

}
