package bb.imgo.struct;

public class ActionLog {

	String filename;
	public enum Action {GOOD, DELETE, ARCHIVE, RENAME, UNKNOWN};
	Action action = Action.UNKNOWN;
	String data;
	long timestamp;
	
	public ActionLog(String filename, Action a) {
		this.action = a;
		this.filename = filename;
		timestamp = System.currentTimeMillis();
	}
	
	public ActionLog(String filename, Action a, String data) {
		this(filename, a);
		this.data = data;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(action.name());
		sb.append(": "+filename); 
		if (data != null) {
			sb.append(" -> ");
			sb.append(data);
		} 
		return sb.toString();
	}
	
}
