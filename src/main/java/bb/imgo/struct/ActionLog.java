package bb.imgo.struct;

public class ActionLog {

	String filename;
	public enum Action {GOOD, DELETE, ARCHIVE, CONVERT, RENAME, UNKNOWN};
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
	
}
