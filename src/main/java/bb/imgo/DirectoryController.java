package bb.imgo;

import java.io.File;

public interface DirectoryController {

	public void setDirectory(String id, File dir);
	
	public File getDirectory(String id);
}
