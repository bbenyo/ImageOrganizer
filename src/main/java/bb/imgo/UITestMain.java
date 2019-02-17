package bb.imgo;

import java.io.File;
import java.util.ArrayList;

import bb.imgo.struct.MediaFile;
import bb.imgo.ui.ImageGridPanel;

public class UITestMain {
	
	public static void main(String[] args) {
		//File rDir = new File("data/test/Pictures/I/J/K/L");
		File rDir = new File("data/test/resources");
		File[] files = rDir.listFiles();
		ArrayList<MediaFile> mFiles = new ArrayList<MediaFile>();
		for (File f : files) {
			MediaFile mf = new MediaFile(f);
			mFiles.add(mf);
		}
		ImageGridPanel ig = new ImageGridPanel(rDir, mFiles, 5, 2);
		ig.setLocationRelativeTo(null);
		ig.setVisible(true);
	}
}
