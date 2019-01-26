package bb.imgo;

public interface OrganizeMediaUIInterface {

	// File has been handled, update statistics
	public void handleFile(String filename, boolean good, boolean delete);
	
	// Moving to a new directory
	public void changeCurrentDirectory(String cwd);
	
	public void updateStatus(String status);
	
	public void initializeUI(int max);
	
	public void incrementProgress();
	
	// Pointer to the controller for start/stop.  Could use an interface here, 
	//   but there will only be one controller (OrganizeMedia)
	// If that ever changes, convert this to an interface 
	public void init(OrganizeMedia oMedia, int width, int height);

}
