package util.handlers;

import util.struct.MediaFile;

public class RemoveDuplicates extends MediaHandler {

	public RemoveDuplicates() {
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
