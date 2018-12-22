package bb.imgo.struct;

public class ActionLog {

	String filename;
	public enum Action {GOOD, DELETE, ARCHIVE, UNKNOWN};
	Action action = Action.UNKNOWN;
	long timestamp;
	
	public ActionLog(String filename, Action a) {
		this.action = a;
		this.filename = filename;
		timestamp = System.currentTimeMillis();
	}
	
	public String toString() {
		return action.name() + ": "+filename;
	}
	
}
