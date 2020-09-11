package bb.imgo.handlers;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.MediaFile;

/**
 * Delete all files of a specific type
 * @author Brett
 *
 */
public class DeleteFileType extends MediaHandler {
	static private Logger logger = Logger.getLogger(DeleteFileType.class.getName());
	
	protected String[] deleteExtensions;
	
	@Override
	public boolean initialize(Properties props) {
		deleteExtensions = new String[0];
		String fe = props.getProperty(PropertyNames.DELETE_FILE_TYPE_EXTENSIONS);
		if (fe != null) {
			setConfigurationOption(PropertyNames.DELETE_FILE_TYPE_EXTENSIONS, fe);
		}
		return true;
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		if (deleteExtensions != null) {
			String fext = f1.getExt();
			for (String de : deleteExtensions) {
				if (fext.equalsIgnoreCase(de)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		f1.setDelete("Delete Extension: "+f1.getExt());
		return true;
	}

	@Override
	public String getDescription() {
		return "Delete all files of type: "+Arrays.toString(deleteExtensions);
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		Map<String, String> configs = new TreeMap<String, String>();
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		if (deleteExtensions != null) {
			for (String de : deleteExtensions) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(de);
			}
		}
		configs.put(PropertyNames.DELETE_FILE_TYPE_EXTENSIONS, sb.toString());
		return configs;
	}

	@Override
	public void setConfigurationOption(String key, String value) {
		if (key.equalsIgnoreCase(PropertyNames.DELETE_FILE_TYPE_EXTENSIONS)) {
			if (value != null) {
				String[] dele = value.split(",");
				if (dele.length > 0) {
					deleteExtensions = new String[dele.length];
					for (int i=0; i<dele.length; ++i) {
						String del = dele[i].trim();
						deleteExtensions[i] = del;
					}
				}			
			} else {
				deleteExtensions = new String[0];
			}
		} else {
			logger.warn("Trying to set a non-existant option: "+key);
		}
	}

}
