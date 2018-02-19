package util.struct;

import java.io.File;
import java.util.HashMap;

/**
 * File type statistics for all files in a directory
 * Contains aggregate statistics, then a breakdown per file type
 * @author Brett
 *
 */
public class DirectoryStats extends FileTypeStats {

	HashMap<String, FileTypeStats> typeStatistics;
	
	public DirectoryStats(File dir, String type) {
		super(dir, type);
	}
	
	
}
