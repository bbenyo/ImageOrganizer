package bb.imgo.handlers;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.OrganizeMedia;
import bb.imgo.struct.ActionLog;
import bb.imgo.struct.MediaFile;

// Handle a media file, decide what flag to apply to it
// TODO: Add a flag or subclass for whether we move files or not
abstract public class MediaHandler {

	static private Logger logger = Logger.getLogger(MediaHandler.class.getName());
	String label;
	OrganizeMedia main;
	
	protected MediaHandler() {
		label = this.getClass().getSimpleName();
	}
	
	public void init() {
		logger.info(label+" initializing");
	}
	
	public String getLabel() {
		return label;
	}
	
	public String printConfig(String indent) {
		return indent+"No additional configuration options";
	}
	
	public void linkOrganizeMedia(OrganizeMedia om) {
		this.main = om;
	}
	
	public void addActionLog(String filename, ActionLog.Action action, String reason) {
		if (main != null) {
			main.addActionLog(filename, action, reason);
		}
	}

	// Return true if you handle this type of file, false if not
	abstract public boolean fileFilter(MediaFile f1);
	
	// Do any start up processing/bookkeeping, we will start processing the given directory next
	// Return false if we need to stop processing this directory (because we changed its name)
	public boolean directoryInit(File directory) {
		logger.debug(label+" DirectoryInit");
		return true;
	}

	// Do any finishing up processing/bookkeeping, we have just finished processing the given directory
	public void directoryComplete(File directory) {
		logger.debug(label+" DirectoryComplete");
	}

	// Do any subdir start up processing/bookkeeping, we will start processing the given subdirectory next
	public void subDirectoryInit(File directory, File subDirectory) {
		//logger.debug(label+" SubDirectoryInit");
	}

	// Do any finishing up processing/bookkeeping, we have just finished processing the given directory
	public void subDirectoryComplete(File directory, File subDirectory) {
		//logger.debug(label+" SubDirectoryComplete");
	}
	
	// Given this file, apply whatever tags you want to it
	
	// If you've handled it, return true, this will prevent the rest of the handlers from executing it
	// If not, return false, and the next handler will operate on it.
	abstract public boolean handleFile(MediaFile f1);

	// Done processing all directories, do any final reporting or cleanup
	public void finalize() {
		logger.info(getLabel()+" finalized");
	}

	// Start (or restart) processing directories
	// Return true if we initialized propertly, false if there was an error
	public boolean initialize(Properties props) {
		logger.info(getLabel()+" initialized");
		return true;
	}
	
}
