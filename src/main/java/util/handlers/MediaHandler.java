package util.handlers;

import java.io.File;
import java.util.logging.Logger;

import util.struct.MediaFile;

// Handle a media file, decide what flag to apply to it
abstract public class MediaHandler {

	static private Logger logger = Logger.getLogger(MediaHandler.class.getName());
	String label;
	
	protected MediaHandler() {
		label = this.getClass().getSimpleName();
	}
	
	public void init() {
		logger.info(label+" initializing");
	}
	
	public String getLabel() {
		return label;
	}

	// Return true if you handle this type of file, false if not
	abstract public boolean fileFilter(MediaFile f1);
	
	// Do any start up processing/bookkeeping, we will start processing the given directory next
	abstract public void directoryInit(File directory);

	// Do any finishing up processing/bookkeeping, we have just finished processing the given directory
	abstract public void directoryComplete(File directory);
	
	// Given this file, apply whatever tags you want to it
	// If you've handled it, return true, this will prevent the rest of the handlers from executing it
	// If not, return false, and the next handler will operate on it.
	abstract public boolean handleFile(MediaFile f1);
	
	// Done processing all directories, do any final reporting or cleanup
	public void finalize() {
		logger.info(getLabel()+" finalized");
	}
	
}
