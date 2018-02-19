package util.handlers;

import java.io.File;
import java.util.ArrayList;

import util.struct.DirectoryStats;
import util.struct.FileTypeStats;
import util.struct.MediaFile;

public class Statistics extends MediaHandler {

	// Store all computed statistics
	protected ArrayList<DirectoryStats> directoryStats;
	
	// Aggregate statistics for all directories
	FileTypeStats rootStats; 
	
	// Directories we're currently working on
	
	
	public Statistics() {
		super();
		directoryStats = new ArrayList<DirectoryStats>();
	}
	
	@Override
	public void initialize() {
		directoryStats.clear();
	}
	
	@Override
	public void directoryInit(File directory) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void directoryComplete(File directory) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true;
	}

}
