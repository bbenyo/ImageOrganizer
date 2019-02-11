package bb.imgo.handlers;

import java.util.Properties;

import bb.imgo.PropertyNames;
import bb.imgo.struct.ActionLog.Action;
import bb.imgo.struct.MediaFile;

/**
 * Delete all files of a specific type
 * @author Brett
 *
 */
public class DeleteFileType extends MediaHandler {
	
	protected String[] deleteExtensions;
	
	@Override
	public boolean initialize(Properties props) {
		String fe = props.getProperty(PropertyNames.DELETE_FILE_TYPE_EXTENSIONS);
		if (fe != null) {
			String[] dele = fe.split(",");
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

}
