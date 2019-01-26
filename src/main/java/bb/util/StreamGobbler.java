package bb.util;

import org.apache.log4j.Logger;
import java.io.*;
	
public class StreamGobbler extends Thread {
	
	InputStream is;
	String type;
	
	boolean squelch = true;
	Logger useLogger = null;
	
	public StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}
	
	public StreamGobbler(InputStream is, String type, Logger logger) {
		this(is, type);
		useLogger = logger;
		squelch = false;
	}
	    
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ((line = br.readLine()) != null) {
				if (!squelch && useLogger != null) {
					useLogger.info(type + ">" + line);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();  
		}
	}
}
