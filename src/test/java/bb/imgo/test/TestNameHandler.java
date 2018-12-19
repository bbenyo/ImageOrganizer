package bb.imgo.test;

import bb.imgo.handlers.MediaHandler;
import bb.imgo.struct.MediaFile;

public class TestNameHandler extends MediaHandler {

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		String name = f1.getBaseFile().getName();
		if (name.equals("Pic_0.jpg")) {
			f1.setGood();			
		} else if (name.equals("Pic_1.jpg")) {
			f1.setDelete();
		}
		return true;
	}

}
