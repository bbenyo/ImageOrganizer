package bb.imgo.struct;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import bb.imgo.OrganizeMedia;

public class ActionLog {
	static private Logger logger = Logger.getLogger(ActionLog.class.getName());
	String filename;
	public enum Action {GOOD, DELETE, CONVERT, COPY, RENAME, UNKNOWN};
	Action action = Action.UNKNOWN;
	String data;
	String reason;
	long timestamp;
	
	public ActionLog(String filename, Action a, String reason) {
		this.action = a;
		this.filename = filename;
		timestamp = System.currentTimeMillis();
		this.reason = reason;
	}
	
	public ActionLog(String filename, Action a, String extradata, String reason) {
		this(filename, a, reason);
		this.data = extradata;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(action.name());
		sb.append(": "+filename);
		sb.append(" (");
		sb.append(reason);
		sb.append(")");
		if (data != null) {
			sb.append(" -> ");
			sb.append(data);
		} 
		return sb.toString();
	}
	
	public void executeAction(OrganizeMedia oMedia) {
		switch (action) {
		case CONVERT:
			logger.error("Can't execute basic CONVERT action, this should be overridden by a subclass");
			break;
		case COPY:
			File cp1 = new File(filename);
			File cp2 = new File(data);
			try {
				oMedia.copyFile(cp1, cp2);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case DELETE:
			File f = new File(filename);
			if (f.isDirectory()) {
				File[] files = f.listFiles();
				if (files != null) {
					for (File f1 : files) {
						if (f1.getName().equals("Thumbs.db") || f1.getName().equals("ZbThumbnail.info")) {
							f1.delete();
						} else {
							logger.warn("Unable to delete non-empty directory: "+f1.getAbsolutePath());
							return;
						}
					}
				}
				f.delete();
			} else {
				File p2 = new File(data);
				try {
					oMedia.moveFile(f, p2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case GOOD:
		case RENAME:
			File p1 = new File(filename);
			File p2 = new File(data);
			try {
				oMedia.moveFile(p1, p2);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}
	}
		
}
