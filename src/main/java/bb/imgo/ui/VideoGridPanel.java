package bb.imgo.ui;

import java.io.File;
import java.util.ArrayList;

import bb.imgo.struct.MediaFile;

public class VideoGridPanel extends ImageGridPanel {

	public VideoGridPanel(File directory, ArrayList<MediaFile> mediaFiles, int x, int y) {
		super(directory, mediaFiles, x, y);
	}
	
	protected ImagePanel createPanel(MediaFile mFile) {
		return new VideoPanel(mFile);
	}

}
