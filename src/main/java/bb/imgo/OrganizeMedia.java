package bb.imgo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
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
import bb.imgo.handlers.VerifyBackup;
import bb.imgo.struct.ActionLog;
import bb.imgo.struct.DirectoryFileFilter;
import bb.imgo.struct.FileUtilities;
import bb.imgo.struct.MediaFile;
import bb.imgo.struct.NonDirectoryFileFilter;
import bb.imgo.ui.OverviewFrame;

public class OrganizeMedia {

	static private Logger logger = Logger.getLogger(OrganizeMedia.class.getName());
	static public String PropertyFileName = "om.properties";
	static public String DefaultDir = "data/test/Pictures";
		
	protected Properties props = null;

	File rootDirectory;
	File startSubdir = null;
	List<MediaHandler> handlers = new ArrayList<MediaHandler>();
	VerifyBackup backupHandler = null;
	File goodDir = new File("data/test/Good");
	File trashDir = new File("data/test/ForDeletion");
	public boolean imageOnly = true;
	public boolean moveFiles = false;
	public boolean recountFiles = true;
	File countFilesSave = new File("fileCounts.txt");
	
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
	
	private List<String> ignoreSubdirNames = new ArrayList<String>();
	
	int uiHeight = 800;
	int uiWidth = 900;
	
	private Thread runningThread = null;
	
	public OrganizeMedia(String pFileName, String rootDir) {
		this(pFileName, rootDir, null);
	}
	
	public OrganizeMedia(String pFileName, String rootDir, String subDir) {
		rootDirectory = new File(rootDir);
		if (!rootDirectory.exists()) {
			error("Root Directory: "+rootDir+" does not exist!");
		} else if (!rootDirectory.isDirectory()) {
			error("Root Directory: "+rootDir+" is not a directory!");
		}
		
		startSubdir = rootDirectory;
		if (subDir != null) {
			startSubdir = new File(rootDirectory, subDir);
			if (startSubdir.exists()) {
				logger.info("Handling only subdirectory: "+subDir);
			} else {
				error("Handle Only Subdirectory: "+subDir+" doesn't exist!");
			}
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
		
		ignoreSubdirNames.add(".svn");
		ignoreSubdirNames.add(".git");
		
		initProperties(props);
		fireHandlerInitialize();
		printConfig();
	}
	
	public boolean isRecountFiles() {
		return recountFiles;
	}

	public void setRecountFiles(boolean recountFiles) {
		this.recountFiles = recountFiles;
	}
	
	public void addIgnoreSubdirName(String sname) {
		if (!ignoreSubdirNames.contains(sname)) {
			ignoreSubdirNames.add(sname);
		}
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
		
		String uih = props.getProperty(PropertyNames.UI_HEIGHT);
		if (uih != null) {
			try {
				uiHeight = Integer.parseInt(uih);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		
		String uiw = props.getProperty(PropertyNames.UI_WIDTH);
		if (uiw != null) {
			try {
				uiWidth = Integer.parseInt(uiw);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		
		String fcSave = props.getProperty(PropertyNames.DIR_COUNT_FILE);
		if (fcSave != null) {
			countFilesSave = new File(fcSave);
			if (!countFilesSave.exists()) {
				if (countFilesSave.getParentFile() != null) {
					countFilesSave.getParentFile().mkdirs();
				}
			}
		}
	}
	
	public File getStartSubdir() {
		return startSubdir;
	}
	
	public void setStartSubdir(File startSubdir) {
		this.startSubdir = startSubdir;
	}

	private void printConfig() {
		String ls = System.lineSeparator();
		StringBuffer sb = new StringBuffer("OrganizeMedia Configuration: "+ls);
		sb.append("  Working Directory: "+rootDirectory+ls);
		sb.append("  Start Subdir: "+startSubdir+ls);
		sb.append("  Directory Count Save File: "+countFilesSave+ls);
		sb.append("  Good Storage Directory: "+goodDir+ls);
		sb.append("  Trash Directory: "+trashDir+ls);
		sb.append("  Image Only? "+imageOnly+ls);
		sb.append("  Do actions? (or just log)? "+moveFiles+ls);
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
		
	public void startThread() {
		if (runningThread != null && runningThread.isAlive()) {
			abort();
			int retries = 10;
			while (runningThread.isAlive() && retries > 0) {
				try {
					Thread.sleep(1000);
					logger.info("Waiting for thread to exit...");
					retries--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (runningThread != null && runningThread.isAlive()) {
			logger.error("Unable to terminate old thread!");
			return;
		}
		
		runningThread = new Thread(new Runnable() {
			public void run() {
				organize();
			}
		});
		runningThread.start();
	}
	
	/**
	 * Main method to organize a directory.  Fire each handler on all directories and files
	 */
	public void organize() {
		// TODO: Two phase pass, first for moving file handlers, second for handlers that don't move
		actionLog.clear();
		//fireHandlerInitialize();  done on startup
		running.set(true);
		
		int totalFiles = 0;
		if (!isRecountFiles() && countFilesSave != null && countFilesSave.exists()) {
			totalFiles = loadFileCounts(startSubdir);
			logger.info("Loaded file count for "+startSubdir+": "+totalFiles);
		} else {		
			if (countFilesSave != null) {
				try {
					BufferedWriter bwrite = new BufferedWriter(new FileWriter(countFilesSave));
					totalFiles = countFiles(startSubdir, bwrite);
					bwrite.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
		
		if (ui != null) {
			ui.initializeUI(totalFiles);
		}
		organize(startSubdir);
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
	
	public int loadFileCounts(File dir) {
		try {
			BufferedReader bread = new BufferedReader(new FileReader(countFilesSave));
			String line = bread.readLine();
			while (line != null) {
				int pos = line.lastIndexOf(": ");				
				if (pos > -1) {
					String d = line.substring(0, pos);
					if (d.equalsIgnoreCase(dir.getAbsolutePath())) {
						try {
							String cStr = line.substring(pos+2).trim();
							return Integer.parseInt(cStr);
						} catch (Exception ex) {
							ex.printStackTrace();
							return -1;
						} finally {
							bread.close();
						}
					}
				} else {
					logger.warn("Unable to parse file count line: "+line);
					bread.close();
					return -1;
				}
				line = bread.readLine();
			}
			bread.close();
			return -1;
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		} 		
	}
		
	public int countFiles(File dir, BufferedWriter saveFileWriter) {
		if (!checkPause()) {
			return 0;
		}
		if (ignoreSubdirNames.contains(dir.getName())) {
			logger.info("IGNORING "+dir.getName());
			return 0;
		}
		
		uiStatus("Counting files under "+dir);
		File[] dFiles = dir.listFiles(noDirectories);
		int count = dFiles.length;
		dFiles = dir.listFiles(directories);
		Arrays.sort(dFiles);
		for (File f : dFiles) {
			count += countFiles(f, saveFileWriter);
		}
		uiStatus("Counted "+count+" files under "+dir);
		if (saveFileWriter != null) {
			try {
				saveFileWriter.write(dir.getAbsolutePath()+": "+count+System.lineSeparator());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return count;
	}
	
	protected void organize(File dir) {
		if (!checkPause()) {
			return;
		}
		if (ignoreSubdirNames.contains(dir.getName())) {
			logger.info("IGNORING "+dir.getName());
			return;
		}
		logger.info("START Organizing directory: "+dir);
		boolean abort = false;
		if (!fireHandlerDirectoryStart(dir)) {
			logger.info("ABORTING Organizing directory: "+dir+" due to handler init");
			abort = true;
			return;
		}
		
		// Are any handlers not disabled?
		if (allHandlersDisabled()) {
			logger.info("All handlers are temporarily disabled!");
			abort = true;
		}
		
		File[] dFiles = dir.listFiles(noDirectories);
		if (!abort) {
			Arrays.sort(dFiles);
			for (File f : dFiles) {
				if (!checkPause()) {
					return;
				}
				fireHandlerFile(f);
			}
		} else {
			if (ui != null) {
				ui.incrementProgress(dFiles.length);
			}
		}
		
		logger.info("PROGRESS Organized files, starting on subdirectories: "+dir);
		
		dFiles = dir.listFiles(directories);
		Arrays.sort(dFiles);
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
	
	protected boolean allHandlersDisabled() {
		for (MediaHandler handler : handlers) {
			if (!handler.isTemporarilyDisabled()) {
				return false;
			}
		}
		return true;
	}
		
	protected boolean fireHandlerDirectoryStart(File dir) {
		for (MediaHandler handler : handlers) {
			logger.debug("Firing "+handler.getLabel()+" for directory start: "+dir.getName());
			if (!handler.directoryInit(dir)) {
				return false;
			}
		}
		if (ui != null) {
			ui.changeCurrentDirectory(dir.getAbsolutePath());
		}
		return true;
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
		uiStatus("FINISHED");
	}
	
	protected void fireHandlerInitialize() {
		for (MediaHandler handler : handlers) {
			uiStatus("Firing Initialize for "+handler.getLabel());
			if (!handler.initialize(props)) {
				uiStatus("Initialize FAILED for "+handler.getLabel());
				// Hard fail for initilization errors
				// You'll want to fix any issue before running anything
				exit();
			}
		}
	}
	
	protected void fireHandlerFile(File f) {
		MediaFile mFile = new MediaFile(f);
		if (imageOnly && !mFile.isImageFile()) {
			logger.debug("Ignoring non image file: "+f);
			if (ui != null) {
				ui.incrementProgress(1);
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
	
	public File getUniqueFile(File p2) {
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
			
			// Does this end with _V#?  If so the # is the index.
			pos = basename.lastIndexOf("_V");
			String suffix = "_V2";
			int index = 2;
			if (pos > -1) {
				try {
					suffix = basename.substring(pos+2);
					int idx = Integer.parseInt(suffix);
					basename = basename.substring(0, pos);
					idx++;
					index = idx;
					suffix = "_V"+idx;
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					suffix = "_V2";
				}
			}
			
			while (p2.exists()) {
				if (ext.length() > 0) {
					p2 = new File(p2.getParentFile(), basename+suffix+"."+ext);
				} else {
					p2 = new File(p2.getParentFile(), basename+suffix+index);
				}
				logger.warn("Trying: "+p2.getAbsolutePath());
				index++;
				suffix="_V"+index;
			}
		}
		return p2;
	}
	
	public void moveFile(File p1, File p2) throws IOException {
		if (!p2.getParentFile().exists()) {
			p2.getParentFile().mkdirs();
		}
		
		p2 = getUniqueFile(p2);		
		
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
		try {
			Files.move(p1.toPath(), p2.toPath());
		} catch (Exception ex) {
			ex.printStackTrace();
			// TODO: UI popup (try again)
			error(ex.toString());
		}
		uiStatus("Moved to "+p2.getAbsolutePath());
	}
	
	public void copyFile(File p1, File p2) throws IOException {
		if (!p2.getParentFile().exists()) {
			p2.getParentFile().mkdirs();
		}
		
		p2 = getUniqueFile(p2);
		
		Files.copy(p1.toPath(), p2.toPath());
		uiStatus("Copied to "+p2.getAbsolutePath());
	}
	
	public ActionLog addDeleteActionLog(String fname, String delFileName, String reason) {
		ActionLog al = new ActionLog(fname, ActionLog.Action.DELETE, delFileName, reason);
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
		return al;
	}
	
	public ActionLog addGoodActionLog(String fname, String gFileName, String reason) {
		ActionLog al = new ActionLog(fname, ActionLog.Action.GOOD, gFileName, reason);
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
		return al;
	}
	
	public ActionLog addRenameActionLog(String oldFileName, String newFileName, String reason) {
		ActionLog al = new ActionLog(oldFileName, ActionLog.Action.RENAME, newFileName, reason);
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
		return al;
	}
	
	public ActionLog addCopyActionLog(String oldFileName, String newFileName, String reason) {
		ActionLog al = new ActionLog(oldFileName, ActionLog.Action.COPY, newFileName, reason);
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
		return al;
	}
	
	public void addActionLog(ActionLog al) {
		actionLog.add(al);
		if (ui != null) {
			ui.updateActionLog(al.toString());
		}
	}
	
	public void completeMediaFileHandling(MediaFile mFile) {
		String mFileName = mFile.getBaseFile().getName();
		uiStatus("Complete handling for "+mFileName);
		if (mFile.getRenameTo() != null) {
			logger.debug("Renaming to "+mFile.getRenameTo());

			File p1 = mFile.getBaseFile();
			File p2 = new File(mFile.getBaseFile().getParentFile(), mFile.getRenameTo());
			ActionLog al = addRenameActionLog(p1.getAbsolutePath(), p2.getAbsolutePath(), mFile.getRenameReason());

			if (moveFiles) {
				al.executeAction(this);
			}
			
			if (backupHandler != null) {
				File backupFile = backupHandler.getBackupFile(mFile);
				if (backupFile != null && backupFile.exists()) {
					File bp2 = new File(backupFile.getParentFile(), p2.getName());
					ActionLog al2 = addRenameActionLog(backupFile.getAbsolutePath(), bp2.getAbsolutePath(), mFile.getRenameReason());
					if (moveFiles) {
						al2.executeAction(this);
					}
				} else {
					logger.warn("No backup file found for "+p1.getAbsolutePath());
				}
			}
			
			mFile.setBaseFile(p2);
			mFileName = p2.getName();					
		}
		// If the file is marked delete, remove it my moving it to trash
		if (mFile.isDelete()) {
			if (ui != null) {
				ui.handleFile(mFileName, false, true);
			}
			logger.debug("Marked for deletion");
			File p1 = mFile.getBaseFile();
			File p2 = mFile.getNewFilePath(rootDirectory, trashDir);
			ActionLog al = addDeleteActionLog(p1.getAbsolutePath(), p2.getAbsolutePath(), mFile.getDeleteReason());
			if (moveFiles) {
				al.executeAction(this);				
			}
		} else if (mFile.isGood()) {
			// If the file is marked good, move it to the good dir
			logger.debug("Marked GOOD");
			if (ui != null) {
				ui.handleFile(mFileName, true, false);
			}
			File p1 = mFile.getBaseFile();
			File p2 = mFile.getNewFilePath(rootDirectory, goodDir);
			ActionLog al = addGoodActionLog(p1.getAbsolutePath(), p2.getAbsolutePath(), mFile.getGoodReason());
			if (moveFiles) {
				al.executeAction(this);
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
	
	public void executeActionLog() {
		ui.clearActionLog();
		ui.initializeProgress(actionLog.size());
		for (ActionLog aLog : actionLog) {
			if (!checkPause()) {
				break;
			}
			logger.info("Executing: "+aLog);
			aLog.executeAction(this);			
			ui.incrementProgress(1);
		}
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
		if (handler instanceof VerifyBackup) {
			backupHandler = (VerifyBackup)handler;
		}
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
		ui.init(this, uiWidth, uiHeight);
		ui.setLocationRelativeTo(null);
		ui.setVisible(true);
	}
	
	public void pause() {
		running.set(false);
		ui.setPaused();
	}
	
	public void abort() {
		abortFlag = true;
		running.set(false);
		ui.setAborted();
	}

	public void resume() {
		running.set(true);
		ui.setResumed();
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
	
	public MediaHandler getSpecificHandler(Class<?> handlerClass) {
		for (MediaHandler h : handlers) {
			if (h.getClass().equals(handlerClass)) {
				return h;
			}
		}
		return null;
	}
	
	static public void main(String[] args) {
		Options options = new Options();
		options.addOption("p", "properties", true, "Properties file name");
		options.addOption("d", "dir", true, "Root directory to organize");
		options.addOption("s", "subdir", true, "Handle only this subdirectory from the root");

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cLine = parser.parse(options, args);
			String pFileName = cLine.getOptionValue("p", PropertyFileName);
			String rDir = cLine.getOptionValue("d", DefaultDir);
			String sDir = cLine.getOptionValue("s", null);
			OrganizeMedia oMedia = new OrganizeMedia(pFileName, rDir, sDir);
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

