package bb.imgo.struct;

import java.io.File;
import java.io.FileFilter;

public class NonDirectoryFileFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return false;
		}
		return true;
	}

}
