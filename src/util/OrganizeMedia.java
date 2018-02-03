package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import util.handlers.MediaHandler;

public class OrganizeMedia {

	File rootDirectory;
	static private Logger logger = Logger.getLogger(OrganizeMedia.class.getName());
	List<MediaHandler> handlers = new ArrayList<MediaHandler>();

	static public String PropertyFileName = "om.properties";
	
	public OrganizeMedia(String pFileName) {
		Properties props = new Properties();
		File pFile = new File(pFileName);
		if (!pFile.exists()) {
			logger.warning("Unable to find properties at "+pFileName);
		}
		
		initProperties(props);
	}
	
	protected void initProperties(Properties props) {
		
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
		String pFileName = PropertyFileName;
		for (int i=0; i<args.length; ++i) {
			if (args[i].equals("-properties")) {
				if (args.length > (i+1)) {
					pFileName = args[i+1];
					i++;
				}
			} else {
				logger.warning("Unrecognized command line parameter: "+args[i]);
			}
		}
		
		OrganizeMedia oMedia = new OrganizeMedia(pFileName);
	}
	
}

