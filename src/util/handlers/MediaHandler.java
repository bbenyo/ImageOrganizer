package util.handlers;

import java.io.File;

import util.struct.MediaFile;

// Handle a media file, decide what flag to apply to it
public interface MediaHandler {
	
	// Do any start up processing/bookkeeping, we will start processing the given directory next
	public void directoryInit(File directory);
	
	// Given this file, apply whatever tags you want to it
	public boolean handleFile(MediaFile f1);
	
}
