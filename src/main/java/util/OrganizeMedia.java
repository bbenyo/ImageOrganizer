package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import util.handlers.MediaHandler;
import util.struct.MediaFile;

public class OrganizeMedia {

	File rootDirectory;
	static private Logger logger = Logger.getLogger(OrganizeMedia.class.getName());
	List<MediaHandler> handlers = new ArrayList<MediaHandler>();

	static public String PropertyFileName = "om.properties";
	static public String DefaultDir = "data";
	
	public OrganizeMedia(String rootDir, String pFileName) {
		rootDirectory = new File(rootDir);
		if (!rootDirectory.exists()) {
			error("Root Directory: "+rootDir+" does not exist!");
		}
		
		Properties props = new Properties();
		File pFile = new File(pFileName);
		if (!pFile.exists()) {
			logger.warning("Unable to find properties at "+pFileName);
		}
		
		initProperties(props);
	}
	
	protected void initProperties(Properties props) {
		String pDir = props.getProperty(PropertyNames.HANDLER_DEFAULT_PACKAGE, "util.handlers");
		String hList = props.getProperty(PropertyNames.HANDLER_LIST);
		if (hList == null) {
			error("No Handlers defined in handler.list");
		}
		String[] hArray = hList.split(",");
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
	}
	
	public void organize() {
		fireHandlerInitialize();
		organize(rootDirectory);
		fireHandlerFinalize();
	}
	
	protected void organize(File dir) {
		logger.info("START Organizing directory: "+dir);
		File[] dFiles = dir.listFiles();
		fireHandlerDirectoryStart(dir);
		for (File f : dFiles) {
			if (!f.isDirectory()) {
				fireHandlerFile(f);
			}
		}
		for (File f : dFiles) {
			if (f.isDirectory()) {
				organize(f);
			}
		}
		fireHandlerDirectoryComplete(dir);
		logger.info("COMPLETE Organizing directory: "+dir);
	}
	
	protected void fireHandlerDirectoryStart(File dir) {
		for (MediaHandler handler : handlers) {
			logger.fine("Firing "+handler.getLabel()+" for directory start: "+dir.getName());
			handler.directoryInit(dir);
		}
	}
	
	protected void fireHandlerDirectoryComplete(File dir) {
		for (MediaHandler handler : handlers) {
			logger.fine("Firing "+handler.getLabel()+" for directory complete: "+dir.getName());
			handler.directoryComplete(dir);
		}
	}
	
	protected void fireHandlerFinalize() {
		for (MediaHandler handler : handlers) {
			logger.fine("Firing finalize for "+handler.getLabel());
			handler.finalize();
		}
	}
	
	protected void fireHandlerInitialize() {
		for (MediaHandler handler : handlers) {
			logger.fine("Firing Initialize for "+handler.getLabel());
			handler.initialize();
		}
	}
	
	protected void fireHandlerFile(File f) {
		MediaFile mFile = new MediaFile(f);
		for (MediaHandler handler : handlers) {
			if (handler.fileFilter(mFile)) {
				logger.finer("Firing "+handler.getLabel()+" for file "+f.getName());
				boolean handled = handler.handleFile(mFile);
				if (handled) {
					logger.fine("File "+f.getName()+" handled by "+handler.getLabel());
					break;
				}
			} else {
				logger.finer("Handler "+handler.getLabel()+" won't handle "+f.getName());
			}
		}
		completeMediaFileHandling(mFile);
	}
	
	protected void completeMediaFileHandling(MediaFile mFile) {
		logger.fine("Complete handling for "+mFile.getBaseFile().getName());
		// If the file is marked delete, remove it my moving it to trash
		if (mFile.isDelete()) {
			
		} else if (mFile.isGood()) {
			// If the file is marked good, keep it
			
		} else {
			// The file is neither good nor delete, so move it to archive
		}
	}
	
	static public void error(String msg) {
		logger.severe(msg);
		System.exit(-1);;
	}
	
	public void addHandler(MediaHandler handler) {
		if (handler == null) {
			logger.warning("addHandler null parameter!");
			return;
		}
		logger.info("Registering "+handler.getClass());
		handlers.add(handler);
	}
	
	public void removeHandler(MediaHandler handler) {
		if (handlers.remove(handler)) {
			logger.info("Unregistered "+handler.getClass());
		} else {
			logger.warning("Attempted to unregister "+handler.getClass()+" which was not registered!");
		}
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
			oMedia.organize();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}

