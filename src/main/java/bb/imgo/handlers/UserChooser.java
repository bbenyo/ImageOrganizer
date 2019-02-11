package bb.imgo.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.MediaFile;
import bb.imgo.ui.ImageGridPanel;

/**
 * Let the user mark files as delete/good, archive is the default
 * Popup a 9x9 frame with next/prev buttons
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
		
	// Start (or restart) processing directories
	// Return true if we initialized propertly, false if there was an error
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
	
	public boolean directoryInit(File directory) {
		logger.debug(label+" DirectoryInit");
		this.setTemporarilyDisabled(false);
		
		if (currentProgressDirectory != null) {
			if (directory.getAbsolutePath().equalsIgnoreCase(currentProgressDirectory)) {
				logger.info("Resuming at "+directory);
				currentProgressDirectory = null;
			} else {
				logger.info("UserChooser waiting to resume progress at "+currentProgressDirectory+", Cur directory is: "+directory.getAbsolutePath());
				this.setTemporarilyDisabled(true);
				return true;
			}			 
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

		File[] files = directory.listFiles(ImageGridPanel.imageFilter);
		if (files == null || files.length == 0) {
			logger.info("No images found in "+directory+", skipping it");
			return true;
		}		
		
		logger.info("Displaying ImageGridPanel for "+directory);
		ImageGridPanel ig = new ImageGridPanel(directory, columns, rows);
		ig.setLocationRelativeTo(null);
		ig.setVisible(true);
		
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
					return true;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return true;
			}
		}
		return true;
	}
	
	@Override
	public void directoryComplete(File directory) {
		this.setTemporarilyDisabled(false);
	}
	
	// We only handle the entire directory
	@Override
	public boolean fileFilter(MediaFile f1) {
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		return false;
	}

}
