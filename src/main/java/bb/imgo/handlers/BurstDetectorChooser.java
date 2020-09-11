package bb.imgo.handlers;

import java.util.Map;

import bb.imgo.struct.MediaFile;

/**
 * Detect a burst of photos (similar date/time), maybe similar contents?
 *   Flag them as a burst, then pop up a chooser that lets the user pick the best subset.  
 *   Mark the best subset as good, rest as archive
 *   
 * @author Brett
 *
 */
public class BurstDetectorChooser extends MediaHandler {

	@Override
	public boolean fileFilter(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfigurationOption(String key, String value) {
		// TODO Auto-generated method stub
		
	}

}
