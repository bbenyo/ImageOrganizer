package bb.imgo.ui;

import java.io.File;
import java.util.ArrayList;

import bb.imgo.struct.MediaFile;

@SuppressWarnings("serial")
public class VideoGridPanel extends ImageGridPanel {
	ArrayList<VideoPanel> panels;
	
	public VideoGridPanel(File directory, ArrayList<MediaFile> mediaFiles, int x, int y) {
		super(directory, mediaFiles, x, y);
	}
	
	protected ImagePanel createPanel(MediaFile mFile) {
		VideoPanel vp = new VideoPanel(mFile);
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
