package bb.imgo.test;

import java.util.Map;

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
			f1.setGood("Test");			
		} else if (name.equals("Pic_1.jpg")) {
			f1.setDelete("Test");
		}
		return true;
	}

	@Override
	public String getDescription() {
		return "Test";
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		return null;
	}

	@Override
	public void setConfigurationOption(String key, String value) {		
	}

}
