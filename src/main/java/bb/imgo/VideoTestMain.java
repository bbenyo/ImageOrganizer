package bb.imgo;

import java.io.File;

import javax.swing.JFrame;

import org.apache.tika.metadata.Metadata;

import bb.imgo.struct.MediaFile;
import bb.imgo.ui.VideoPanel;

public class VideoTestMain {
	
	public static void main(String[] args) {
		MediaFile p0 = new MediaFile(new File("data/test/resources", "MVI_0791.AVI"));
		String t = p0.getType();
		System.out.println("File Type: "+t);
		Metadata md = p0.getMetadata();
		String[] keys = md.names();
		for (String k : keys) {
			System.out.println("Metadata key: "+k+": "+md.get(k));
		}
		VideoPanel ip1 = new VideoPanel(p0);
		JFrame if1 = ip1.createFrame();
		if1.setLocationRelativeTo(null);
		if1.setVisible(true);
	}
}
