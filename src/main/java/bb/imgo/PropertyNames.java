package bb.imgo;

public class PropertyNames {

	// List of Handler class names 
	static public String HANDLER_LIST = "handler.list";
	// Default package for handlers
	static public String HANDLER_DEFAULT_PACKAGE = "handler.package";
	// Only work with image files (not movie)
	static public String IMAGE_ONLY = "handler.imageonly";
	// Move marked files?  If false, we'll just log the marks
	static public String MOVE_FILES = "handler.moveFiles";
	// Re-count all files at the start
	static public String RECOUNT_FILES = "handler.recountFiles";
	// Action Log output name
	static public String ACTION_LOG_NAME = "handler.actionLog.name";
	// Show the UI
	static public String SHOW_UI = "handler.ui";
	// UI frame height
	static public String UI_HEIGHT = "ui.height";
	// UI frame width
	static public String UI_WIDTH = "ui.width";
	
	// Output file name for stats
	static public String STATS_OUTPUTFILENAME = "statistics.outputfile.name";
	// Output CSV filename for status
	static public String STATS_OUTPUTFILECSV = "statistics.outputfile.csv";
	// Directory to store the output files
	static public String STATS_OUTPUTFILEDIR = "statistics.outputfile.dir";
		
	// Directory to store files marked trash
	static public String TRASH_DIR = "directory.trash";
	// Directory to store files marked good
	static public String GOOD_DIR = "directory.good";
	// File to store directory files counts so we don't need to recalculate them
	static public String DIR_COUNT_FILE = "directory.count.file";

	// Video root directory
	static public String VIDEO_ROOT_DIR = "separate.video.root";
	
	// Backup directories
	static public String BACKUP_IMAGE_ROOT="backup.image.root";
	static public String BACKUP_VIDEO_ROOT="backup.video.root";

	static public String BACKUP_IMAGE_ROOT_GOOD="backup.image.root.good";
	static public String BACKUP_VIDEO_ROOT_GOOD="backup.video.root.good";
	
	// User chooser, do we want to take into account any previous progress and start where we left off?
	static public String USER_CHOOSER_USE_PROGRESS="userchooser.progress.enabled";
	// Current progress file
	static public String USER_CHOOSER_PROGRESS_FILENAME="userchooser.progress.filename";
	// Number of images on a row
	static public String USER_CHOOSER_COLUMNS="userchooser.columns";
	// Number of rows
	static public String USER_CHOOSER_ROWS="userchooser.rows";
	
	// User chooser, do we want to take into account any previous progress and start where we left off?
	static public String VIDEO_RENAME_USE_PROGRESS="videorename.progress.enabled";
	// Current progress file
	static public String VIDEO_RENAME_PROGRESS_FILENAME="videorename.progress.filename";
	// Number of images on a row
	static public String VIDEO_RENAME_COLUMNS="userchooser.columns";
	// Number of rows
	static public String VIDEO_RENAME_ROWS="userchooser.rows";
	
	// File extensions to delete, comma separated
	static public String DELETE_FILE_TYPE_EXTENSIONS="delete.extensions";
}
