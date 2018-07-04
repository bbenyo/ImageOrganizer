package util.handlers;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import util.struct.DirectoryStats;
import util.struct.FileTypeStats;
import util.struct.MediaFile;

public class Statistics extends MediaHandler {
	static private Logger logger = Logger.getLogger(Statistics.class.getName());
	
	// Store all computed statistics
	protected HashMap<File, DirectoryStats> directoryStats;
	
	// Aggregate statistics for all directories
	FileTypeStats rootStats; 
		
	public Statistics() {
		super();
		directoryStats = new HashMap<File, DirectoryStats>();
	}
	
	@Override
	public void initialize() {
		directoryStats.clear();
	}
	
	@Override
	public void directoryInit(File directory) {
		DirectoryStats dStats = new DirectoryStats(directory);
		if (rootStats == null) {
			rootStats = dStats;
		}
		directoryStats.put(directory, dStats);
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		File f = f1.getBaseFile();
		DirectoryStats cDir = directoryStats.get(f.getParentFile());
		if (cDir == null) {
			logger.warn("Unable to find stats for "+f.getParentFile());
			cDir = new DirectoryStats(f.getParentFile());
			directoryStats.put(f.getParentFile(), cDir);
		} 
		// Statistics doesn't apply any tags to the MediaFile, so we can just get the base File
		return cDir.handleFile(f1.getBaseFile());
	}

	@Override
	public void directoryComplete(File directory) {
		DirectoryStats cDir = directoryStats.get(directory);
		if (cDir == null) {
			logger.error("Can't find stats for "+directory);
		} else {
			logger.info(cDir.report());
		}
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true;
	}

}
