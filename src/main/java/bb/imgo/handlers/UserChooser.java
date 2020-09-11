package bb.imgo.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.ImageFileFilter;
import bb.imgo.struct.MediaFile;
import bb.imgo.ui.ImageGridPanel;

/**
 * Let the user mark files as delete/good, archive is the default
 * Popup a 9x9 frame with next/prev buttons
 *   
 * @author Brett
 *
 */
public class UserChooser extends MediaHandler {
	
	static private Logger logger = Logger.getLogger(UserChooser.class.getName());
	
	// File to store current progress, so we can stop and restart later and remember how far we got
	static public String currentProgressFilename = "userChooserProgress.txt";
	
	protected String currentProgressDirectory = null; // Which directory we last were working on
	// If null, we'll start at the beginning.  If not null, we'll ignore everything until we hit this directory
	protected File currentProgressFile = null;
	// pictures on a row
	protected int columns = 5;
	// number of rows
	protected int rows = 2;

	Stack<ArrayList<MediaFile>> mediaFileStack = new Stack<ArrayList<MediaFile>>();
	ArrayList<MediaFile> mediaFiles = null;
	protected FileFilter imageFilter = new ImageFileFilter();
	
	protected boolean cancelled = false;
	
	protected boolean temporaryDisableAllHandlers = true; // TODO: Make this a property
	
	// Start (or restart) processing directories
	// Return true if we initialized properly, false if there was an error
	public boolean initialize(Properties props) {
		logger.info(getLabel()+" initialized");

		String cStr = props.getProperty(PropertyNames.USER_CHOOSER_COLUMNS);
		if (cStr != null) {
			try {
				columns = Integer.parseInt(cStr);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		
		String rStr = props.getProperty(PropertyNames.USER_CHOOSER_ROWS);
		if (rStr != null) {
			try {
				rows = Integer.parseInt(rStr);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}

		String cpFile = props.getProperty(PropertyNames.USER_CHOOSER_PROGRESS_FILENAME);
		if (cpFile != null) {
			currentProgressFilename = cpFile;
		}
		
		currentProgressFile = new File(currentProgressFilename);
		if (!currentProgressFile.exists()) {
			if (currentProgressFile.getParentFile() != null) {
				currentProgressFile.getParentFile().mkdirs();
			}
		}
		
		String useProgressStr = props.getProperty(PropertyNames.USER_CHOOSER_USE_PROGRESS);
		if (useProgressStr != null) {
			if (!Boolean.parseBoolean(useProgressStr)) {
				currentProgressDirectory = null;
				currentProgressFile = null;
				// Don't use any previous progress
			}
		}		
		
		boolean ret = readCurrentProgressFile();
		return ret;
	}
	
	protected boolean readCurrentProgressFile() {
		if (currentProgressFile.exists()) {
			try {
				logger.info("Trying to read progress from "+currentProgressFile.getAbsolutePath());
				BufferedReader bread = new BufferedReader(new FileReader(currentProgressFile));
				String dLine = bread.readLine();
				bread.close();
				currentProgressDirectory = dLine;
				File cpDir = new File(currentProgressDirectory);
				if (!cpDir.exists()) {
					logger.error("Current Progress read from "+currentProgressFile+" doesn't exist: "+cpDir.getAbsolutePath());
					logger.error("Delete or fix "+currentProgressFile);
					return false;
				} else {
					logger.info("Resuming UserChooser at "+currentProgressDirectory);
				}				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
		return true;
	}
	
	public String getCurrentProgressDirectory() {
		return currentProgressDirectory;
	}
	
	public void setCurrentProgressDirectory(String cpd) {
		currentProgressDirectory = cpd;
	}
	
	public boolean directoryInit(File directory) {
		logger.debug(label+" DirectoryInit");
		this.setTemporarilyDisabled(false);
		mediaFiles = new ArrayList<MediaFile>();
		
		if (currentProgressDirectory != null) {
			if (directory.getAbsolutePath().equalsIgnoreCase(currentProgressDirectory)) {
				logger.info("Resuming at "+directory);
				currentProgressDirectory = null;

				if (this.temporaryDisableAllHandlers) {
					main.setTemporarilyDisableAllHandlers(false);
				}

			} else {
				logger.info("UserChooser waiting to resume progress at "+currentProgressDirectory+", Cur directory is: "+directory.getAbsolutePath());
				this.setTemporarilyDisabled(true);
				if (this.temporaryDisableAllHandlers) {
					main.setTemporarilyDisableAllHandlers(true);
				}
				return true;
			}			 
		}		
		return true;		
	}
	
	protected ImageGridPanel createImageGridPanel(File directory) {
		return new ImageGridPanel(directory, mediaFiles, columns, rows);
	}
	
	@Override
	public void directoryComplete(File directory) {
		if (this.isTemporarilyDisabled()) {
			this.setTemporarilyDisabled(false);
			this.mediaFiles.clear();
			return;
		}
		
		if (cancelled) {
			return;
		}
		
		if (currentProgressFile != null) {
			try {
				BufferedWriter bwrite = new BufferedWriter(new FileWriter(currentProgressFile, false));
				bwrite.write(directory.getAbsolutePath());
				bwrite.close();
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

		if (mediaFiles.size() == 0) {
			logger.info("No media files found in "+directory+", skipping it");
			return;
		}		
		
		logger.info("Displaying ImageGridPanel for "+directory);
		ImageGridPanel ig = createImageGridPanel(directory);
		ig.setLocationRelativeTo(null);
		ig.setVisible(true);
		ig.loadImages();
		
		while (ig.isVisible()) {
			try {
				synchronized(ig) {
					if (ig.isVisible()) {
						ig.wait();
					}
				}
				if (ig.isVisible()) {
					logger.warn("Notified, but ImageGridPanel is still visible!");
				} else {
					logger.info("Closed ImageGridPanel");	
					if (ig.wasCancelled()) {
						logger.info("ImageGridPanel was cancelled by the user");
						cancelled = true;
						main.abort();
					} else {
						for (MediaFile mf : mediaFiles) {
							main.completeMediaFileHandling(mf);
						}
					}

					mediaFiles.clear();
					return;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		if (imageFilter.accept(f1.getBaseFile())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		
		mediaFiles.add(f1);
		return false;
	}
	
	@Override
	public void subDirectoryInit(File dir, File subDir) {
		// Push the parent's files
		mediaFileStack.push(mediaFiles);
	}
	
	@Override
	public void subDirectoryComplete(File dir, File subDir) {
		// Pop, get the parent's files back
		mediaFiles = mediaFileStack.pop();
	}

	@Override
	public String getDescription() {
		return "Let the user specify whether an image/video is Good or Trash";
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		HashMap<String, String> configs = new HashMap<String, String>();
		configs.put(PropertyNames.USER_CHOOSER_COLUMNS, Integer.toString(columns));
		configs.put(PropertyNames.USER_CHOOSER_ROWS, Integer.toString(rows));
		if (currentProgressFile != null) {
			configs.put(PropertyNames.USER_CHOOSER_PROGRESS_FILENAME, currentProgressFile.getAbsolutePath());
		} else {
			configs.put(PropertyNames.USER_CHOOSER_PROGRESS_FILENAME, "");
		}
		return configs; 
	}

	@Override
	public void setConfigurationOption(String key, String value) {
		if (key.equalsIgnoreCase(PropertyNames.USER_CHOOSER_COLUMNS)) {
			try {
				columns = Integer.valueOf(value);
			} catch (NumberFormatException ex) {
				logger.error(ex.toString(), ex);
			}
		} else if (key.equalsIgnoreCase(PropertyNames.USER_CHOOSER_ROWS)) {
			try {
				rows = Integer.valueOf(value);
			} catch (NumberFormatException ex) {
				logger.error(ex.toString(), ex);
			}
		} if (key.equalsIgnoreCase(PropertyNames.USER_CHOOSER_PROGRESS_FILENAME)) {
			currentProgressFile = new File(value);
			logger.info("Writing User Chooser current progress to file: "+currentProgressFile.getAbsolutePath());
		} else {
			logger.error("Unknown config option: "+key);
		}		
	}

}
