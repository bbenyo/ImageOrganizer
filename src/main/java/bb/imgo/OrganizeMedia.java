package bb.imgo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import bb.imgo.handlers.MediaHandler;
import bb.imgo.struct.ActionLog;
import bb.imgo.struct.DirectoryFileFilter;
import bb.imgo.struct.FileUtilities;
import bb.imgo.struct.ImageFileFilter;
import bb.imgo.struct.MediaFile;
import bb.imgo.struct.NonDirectoryFileFilter;
import bb.imgo.ui.OverviewFrame;

public class OrganizeMedia {

	static private Logger logger = Logger.getLogger(OrganizeMedia.class.getName());
	static public String PropertyFileName = "om.properties";
	static public String DefaultDir = "data/test/Pictures";
		
	protected Properties props = null;

	File rootDirectory;
	List<MediaHandler> handlers = new ArrayList<MediaHandler>();
	File goodDir = new File("data/test/Good");
	File trashDir = new File("data/test/ForDeletion");
	public boolean imageOnly = true;
	public boolean moveFiles = true;
	
	// True if we can use File.renameTo.  If that fails, we'll try move instead
	private boolean ableToRename = true;
	
	protected ArrayList<ActionLog> actionLog = new ArrayList<ActionLog>();
	protected String actionLogFilename = "action.log";
	
	protected boolean showUI = true;
	protected OverviewFrame ui = null;
	
	protected AtomicBoolean running = new AtomicBoolean(true);
	protected boolean abortFlag = false;
	
	// To synchronize thread wait/notify
	private Object sync = new Object();

	private NonDirectoryFileFilter noDirectories = new NonDirectoryFileFilter();
	private DirectoryFileFilter directories = new DirectoryFileFilter();

	public OrganizeMedia(String pFileName, String rootDir) {
		rootDirectory = new File(rootDir);
		if (!rootDirectory.exists()) {
			error("Root Directory: "+rootDir+" does not exist!");
		} else if (!rootDirectory.isDirectory()) {
			error("Root Directory: "+rootDir+" is not a directory!");
		}
		
		props = new Properties();
		File pFile = FileUtilities.findFile(pFileName);
		if (pFile == null || !pFile.exists()) {
			logger.warn("Unable to find properties at "+pFileName);
		} else {
			try {
				props.load(new FileReader(pFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		initProperties(props);
		printConfig();
	}

	// TODO: refactor using a ConfigItem or Option system
	protected void initProperties(Properties props) {
		// This is the default package for handlers
		String pDir = props.getProperty(PropertyNames.HANDLER_DEFAULT_PACKAGE, "util.handlers");
		// This is the list of handlers, comma separated
		// Handlers do something to a file or directory, and can mark files as either Good, move to archive, or delete
		String hList = props.getProperty(PropertyNames.HANDLER_LIST);
		if (hList == null) {
			error("No Handlers defined in handler.list");
		}
		String[] hArray = hList.split(",");
		// For each handler in the list, generate its full class name, instantiate it, and add it 
		for (String h : hArray) {
			String hName = pDir+"."+h.trim();
			try {
				Class<?> cls = Class.forName(hName);
				Object cInst = cls.newInstance();
				MediaHandler newHandler = (MediaHandler)cInst;
				addHandler(newHandler);
			} catch (Exception ex) {
				ex.printStackTrace();
				error(ex.toString());
			}
		}
		
		// Boolean flag for whether we're only handling images, not video or other files
		String ionly = props.getProperty(PropertyNames.IMAGE_ONLY);
		if (ionly != null) {
			imageOnly = Boolean.parseBoolean(ionly);
		}
		
		// Boolean flag for whether we're actually moving files or not
		String moveFilesStr = props.getProperty(PropertyNames.MOVE_FILES);
		if (moveFilesStr != null) {
			moveFiles = Boolean.parseBoolean(moveFilesStr);
		}
				
		// Directory to store files for deleteion
		String tDir = props.getProperty(PropertyNames.TRASH_DIR);
		if (tDir != null) {
			this.trashDir = new File(tDir);
		}
		if (!this.trashDir.exists()) {
			this.trashDir.mkdirs();
		}
		
		String gDir = props.getProperty(PropertyNames.GOOD_DIR);
		if (gDir != null) {
			this.goodDir = new File(gDir);
		}
		if (!this.goodDir.exists()) {
			this.goodDir.mkdirs();
		}
		
		String alf = props.getProperty(PropertyNames.ACTION_LOG_NAME);
		if (alf != null) {
			this.actionLogFilename = alf;
		}
		
		String sui = props.getProperty(PropertyNames.SHOW_UI);
		if (sui != null) {
			showUI = Boolean.parseBoolean(sui);
		}
	}
	
	private void printConfig() {
		String ls = System.lineSeparator();
		StringBuffer sb = new StringBuffer("OrganizeMedia Configuration: "+ls);
		sb.append("  Working Directory: "+rootDirectory+ls);
		sb.append("  Good Storage Directory: "+goodDir+ls);
		sb.append("  Trash Directory: "+trashDir+ls);
		sb.append("  Image Only? "+imageOnly+ls);
		sb.append("  Move Files? "+moveFiles+ls);
		sb.append("  Action Log Filename: "+actionLogFilename+ls);
		sb.append("  Show UI? "+showUI+ls);
		sb.append("  Handlers: "+ls);
		for (MediaHandler handler : handlers) {
			sb.append("    "+handler.getLabel()+" "+handler.getClass().getName());
			sb.append(ls);
			sb.append(handler.printConfig("      "));
			sb.append(ls);
		}
		logger.info(sb.toString());
	}
		
	/**
	 * Main method to organize a directory.  Fire each handler on all directories and files
	 */
	public void organize() {
		actionLog.clear();
		fireHandlerInitialize();
		running.set(true);
		int totalFiles = countFiles(rootDirectory);
		if (ui != null) {
			ui.initializeUI(totalFiles);
		}
		organize(rootDirectory);
		fireHandlerFinalize();
		writeActionLog();
	}
	
	private boolean checkPause() {
		if (!running.get()) {
			if (abortFlag) {
				logger.info("ABORT");
				return false;
			}
			synchronized(sync) {
				try {
					logger.info("PAUSED");
					sync.wait();
					logger.info("RESUMED");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}
		
	public int countFiles(File dir) {
		logger.debug("Counting files under "+dir);
		File[] dFiles = dir.listFiles(noDirectories);
		int count = dFiles.length;
		dFiles = dir.listFiles(directories);
		for (File f : dFiles) {
			count += countFiles(f);
		}
		logger.debug("Counted "+count+" files under "+dir);
		return count;
	}
	
	protected void organize(File dir) {
		if (!checkPause()) {
			return;
		}
		logger.info("START Organizing directory: "+dir);
		File[] dFiles = dir.listFiles(noDirectories);
		fireHandlerDirectoryStart(dir);
		for (File f : dFiles) {
			if (!checkPause()) {
				return;
			}
			fireHandlerFile(f);
		}
		logger.info("PROGRESS Organized files, starting on subdirectories: "+dir);
		
		dFiles = dir.listFiles(directories);
		for (File f : dFiles) {
			if (f.isDirectory()) {
				fireHandlerSubDirectoryStart(dir, f);
				organize(f);
				fireHandlerSubDirectoryComplete(dir, f);
			}
		}

		logger.info("COMPLETE Organizing directory: "+dir);
		fireHandlerDirectoryComplete(dir);
	}
	
	private void uiStatus(String status) {
		if (ui != null) {
			ui.updateStatus(status);
		}
		logger.debug(status);
	}
		
	protected void fireHandlerDirectoryStart(File dir) {
		for (MediaHandler handler : handlers) {
			logger.debug("Firing "+handler.getLabel()+" for directory start: "+dir.getName());
			handler.directoryInit(dir);
		}
		if (ui != null) {
			ui.changeCurrentDirectory(dir.getAbsolutePath());
		}
	}
	
	protected void fireHandlerDirectoryComplete(File dir) {
		for (MediaHandler handler : handlers) {
			logger.debug("Firing "+handler.getLabel()+" for directory complete: "+dir.getName());
			handler.directoryComplete(dir);
		}
		uiStatus("COMPLETE directory "+dir.getAbsolutePath());
	}
	
	protected void fireHandlerSubDirectoryStart(File dir, File subDir) {
		for (MediaHandler handler : handlers) {
			logger.debug("Firing "+handler.getLabel()+" for subdirectory start: "+dir.getName()+" -> "+subDir.getName());
			handler.subDirectoryInit(dir, subDir);
		}
	}
	
	protected void fireHandlerSubDirectoryComplete(File dir, File subDir) {
		for (MediaHandler handler : handlers) {
			logger.debug("Firing "+handler.getLabel()+" for subdirectory complete: "+dir.getName()+" -> "+subDir.getName());
			handler.subDirectoryComplete(dir, subDir);
		}
	}

	protected void fireHandlerFinalize() {
		for (MediaHandler handler : handlers) {
			uiStatus("Firing finalize for "+handler.getLabel());
			handler.finalize();
		}
	}
	
	protected void fireHandlerInitialize() {
		for (MediaHandler handler : handlers) {
			uiStatus("Firing Initialize for "+handler.getLabel());
			handler.initialize(props);
		}
	}
	
	protected void fireHandlerFile(File f) {
		MediaFile mFile = new MediaFile(f);
		if (imageOnly && !mFile.isImageFile()) {
			logger.debug("Ignoring non image file: "+f);
			if (ui != null) {
				ui.incrementProgress();
			}
			return;
		}
		for (MediaHandler handler : handlers) {
			if (handler.fileFilter(mFile)) {
				uiStatus(handler.getLabel()+" for file "+f.getName());
				boolean handled = handler.handleFile(mFile);
				if (handled) {
					logger.debug("File "+f.getName()+" handled by "+handler.getLabel());
					break;
				}
			} else {
				logger.debug("Handler "+handler.getLabel()+" won't handle "+f.getName());
			}
		}
		completeMediaFileHandling(mFile);
	}
	
	public void setMoveFiles(boolean f) {
		logger.info("Setting MoveFiles flag to "+f);
		moveFiles = f;
	}
	
	public void moveFile(File p1, File p2) throws IOException {
		if (!p2.getParentFile().exists()) {
			p2.getParentFile().mkdirs();
		}
		
		if (p2.exists()) {
			logger.warn("Trying to move to "+p2.getAbsolutePath()+", but it already exists!");
			String fname = p2.getName();
			int pos = fname.lastIndexOf(".");
			String basename = fname;
			String ext = "";
			if (pos > -1) {
				basename = fname.substring(0, pos);
				ext = fname.substring(pos+1);
			}
			int index = 2;
			while (p2.exists()) {
				if (ext.length() > 0) {
					p2 = new File(p2.getParentFile(), basename+"_"+index+"."+ext);
				} else {
					p2 = new File(p2.getParentFile(), basename+"_"+index);
				}
				logger.warn("Trying: "+p2.getAbsolutePath());
				index++;
			}
		}
		
		if (ableToRename) {
			try {
				boolean success = p1.renameTo(p2);
				if (success) {
					logger.debug("Renamed to "+p2.getAbsolutePath());
					return;
				} else {
					logger.warn("Unable to rename file: "+p1+" to "+p2);
					ableToRename = false;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				ableToRename = false;
			}
		}				
		Files.move(p1.toPath(), p2.toPath());
		uiStatus("Moved to "+p2.getAbsolutePath());
	}
	
	public void addActionLog(String fname, ActionLog.Action act) {
		ActionLog al = new ActionLog(fname, act);
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
	}
	
	public void addRenameActionLog(String oldFileName, String newFileName) {
		ActionLog al = new ActionLog(oldFileName, ActionLog.Action.RENAME, newFileName);
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
	}
	
	protected void completeMediaFileHandling(MediaFile mFile) {
		String mFileName = mFile.getBaseFile().getName();
		uiStatus("Complete handling for "+mFileName);
		// If the file is marked delete, remove it my moving it to trash
		if (mFile.isDelete()) {
			if (ui != null) {
				ui.handleFile(mFileName, false, true);
			}
			logger.debug("Marked for deletion");
			addActionLog(mFile.getBaseFile().getAbsolutePath(), ActionLog.Action.DELETE);
			if (moveFiles) {
				try {
					File p1 = mFile.getBaseFile();
					File p2 = mFile.getNewFilePath(rootDirectory, trashDir);
					moveFile(p1, p2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (mFile.isGood()) {
			// If the file is marked good, move it to the good dir
			logger.debug("Marked GOOD");
			if (ui != null) {
				ui.handleFile(mFileName, true, false);
			}
			addActionLog(mFile.getBaseFile().getAbsolutePath(), ActionLog.Action.GOOD);
			if (moveFiles) {
				try {
					File p1 = mFile.getBaseFile();
					File p2 = mFile.getNewFilePath(rootDirectory, goodDir);
					moveFile(p1, p2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			// The file is neither good nor delete, so keep it here
			logger.debug("Marked ARCHIVE");
			if (ui != null) {
				ui.handleFile(mFileName, false, false);
			}
		}
	}
	
	public void writeActionLog() {
		File alf = new File(actionLogFilename);
		try {
			BufferedWriter bwrite = new BufferedWriter(new FileWriter(alf));
			for (ActionLog al : actionLog) {
				bwrite.write(al.toString()+System.lineSeparator());
			}
			bwrite.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public ArrayList<ActionLog> getActionLog() {
		return actionLog;
	}
	
	public File getActionLogFile() {
		File alf = new File(actionLogFilename);
		return alf;
	}
	
	static public void error(String msg) {
		logger.error(msg);
		System.exit(-1);
	}
	
	public void addHandler(MediaHandler handler) {
		if (handler == null) {
			logger.warn("addHandler null parameter!");
			return;
		}
		logger.info("Registering "+handler.getClass());
		handlers.add(handler);
		handler.linkOrganizeMedia(this);
	}
	
	public void removeHandler(MediaHandler handler) {
		if (handlers.remove(handler)) {
			logger.info("Unregistered "+handler.getClass());
		} else {
			logger.warn("Attempted to unregister "+handler.getClass()+" which was not registered!");
		}
	}
	
	public File getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public File getGoodDir() {
		return goodDir;
	}

	public void setGoodDir(File goodDir) {
		this.goodDir = goodDir;
	}

	public File getTrashDir() {
		return trashDir;
	}

	public void setTrashDir(File trashDir) {
		this.trashDir = trashDir;
	}
	
	public void startUI() {
		ui = new OverviewFrame();
		ui.init(this);
		ui.setLocationRelativeTo(null);
		ui.setVisible(true);
	}
	
	public void pause() {
		running.set(false);
	}
	
	public void abort() {
		abortFlag = true;
		running.set(false);
	}

	public void resume() {
		running.set(true);
		synchronized(sync) {
			sync.notifyAll();
		}
	}
	
	public void exit() {
		abort();
		while (running.get()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(1);
	}
	
	public List<MediaHandler> getHandlers() {
		return handlers;
	}
	
	static public void main(String[] args) {
		Options options = new Options();
		options.addOption("p", "properties", true, "Properties file name");
		options.addOption("d", "dir", true, "Root directory to organize");

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cLine = parser.parse(options, args);
			String pFileName = cLine.getOptionValue("p", PropertyFileName);
			String rDir = cLine.getOptionValue("d", DefaultDir);
			OrganizeMedia oMedia = new OrganizeMedia(pFileName, rDir);
			if (oMedia.showUI) {
				oMedia.startUI();
			} else {
				oMedia.organize();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}

