package bb.imgo.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import bb.imgo.struct.MediaFile;

@SuppressWarnings("serial")
public class VideoGridPanel extends ImageGridPanel {
	private static Logger logger = Logger.getLogger(VideoGridPanel.class.getName());
	
	ArrayList<VideoPanel> panels;
	
	HashMap<MediaFile, String> probableDuplicates;
		
	public VideoGridPanel(File directory, ArrayList<MediaFile> mediaFiles, int nx, int ny) {
		super(directory, mediaFiles, nx, ny);
	}
	
	public void showPage() {

		// Check for potential duplicate videos by checking file size
		//  Low chance for two hundred M to gig size videos to have file size within 1 KB and not be duplicates
		HashMap<Long, String> fileSizes = new HashMap<Long, String>();
		if (probableDuplicates != null) {
			probableDuplicates.clear();
		} else {
			probableDuplicates = new HashMap<MediaFile, String>();
		}

		for (MediaFile f : mediaFiles) {
			Long sz = Math.round(f.getBaseFile().length() / 1000.0); // Get kilobyte size
			String sameSize = fileSizes.get(sz);
			if (sameSize == null) {
				fileSizes.put(sz, f.getBaseName());
			} else {
				// Probable duplicate
				probableDuplicates.put(f, sameSize);
				logger.info("Probable duplicate (same size): "+f.getBaseName()+" = "+sameSize);
			}
		}
		if (panels != null) {
			for (VideoPanel vp : panels) {
				vp.finalize();
			}
		}
		super.showPage();
	}
	
	protected ImagePanel createPanel(MediaFile mFile) {
		VideoPanel vp = new VideoPanel(mFile);
		String pDup = probableDuplicates.get(mFile);
		if (pDup != null) {
			vp.setPotentialDuplicate(pDup);
			vp.trashButton.doClick();
		}
		if (panels == null) {
			panels = new ArrayList<VideoPanel>();
		}
		panels.add(vp);
		return vp;
	}
	
	@Override
	public void cleanup() {
		for (VideoPanel vp : panels) {
			vp.finalize();
		}
		setVisible(false);
		dispose();
		// Tell anyone waiting that we're done
		// UserChooser waits for this
		synchronized(this) {
			this.notifyAll();
		}
	}

}
