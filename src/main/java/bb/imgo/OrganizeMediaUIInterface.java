package bb.imgo;

public interface OrganizeMediaUIInterface {

	// File has been handled, update statistics
	public void handleFile(boolean good, boolean delete);
	
	// Moving to a new directory
	public void changeCurrentDirectory(String cwd);
	
	public void updateStatus(String status);
	
	public void initializeProgressBar(int min, int max);
	
	public void updateProgress(int value);
	
	// Pointer to the controller for start/stop.  Could use an interface here, 
	//   but there will only be one controller (OrganizeMedia)
	// If that ever changes, convert this to an interface 
	public void init(OrganizeMedia oMedia);

}
