package bb.imgo;

import java.io.File;

import bb.imgo.struct.ImageGridPanel;

public class UITestMain {
	
	public static void main(String[] args) {
		//File rDir = new File("data/test/Pictures/I/J/K/L");
		File rDir = new File("data/test/resources");
		ImageGridPanel ig = new ImageGridPanel(rDir, 5, 2);
		ig.setLocationRelativeTo(null);
		ig.setVisible(true);
	}
}
