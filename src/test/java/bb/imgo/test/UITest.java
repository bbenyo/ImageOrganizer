package bb.imgo.test;

import java.io.File;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;

import bb.imgo.struct.MediaFile;
import bb.imgo.ui.ImageGridPanel;
import bb.imgo.ui.ImagePanel;

public class UITest {
	
	static public String resDir = "data/test/resources";
	
	@Test
	public void testImageFrame() {
		MediaFile p0 = new MediaFile(new File(resDir, "Pic_0.jpg"));
		Assert.assertTrue(p0.getBaseFile().exists());
		
		ImagePanel ip1 = new ImagePanel(p0);
		JFrame if1 = ip1.createFrame();
		Assert.assertTrue(if1.isVisible());
	}
	
	@Test
	public void testImageGrid() {
		File rDir = new File("data/test/Pictures/I/J/K/L");
		Assert.assertTrue(rDir.exists());
		
		ImageGridPanel ig = new ImageGridPanel(rDir, 5, 2);
		ig.setVisible(true);
		Assert.assertTrue(ig.isVisible());
	}
}
