package bb.imgo.struct;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileFilter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageGridPanel extends JFrame {

	JPanel mainPanel;
	JButton back;
	JButton next;
	JButton done;
	
	File directory;
	
	int x,y;
	int startIndex = -1;
	int endIndex = -1;
	
	static public FileFilter dirFilter = new DirectoryFileFilter();
	
	public ImageGridPanel(File directory, int x, int y) {
		super(directory.getAbsolutePath());
		this.directory = directory;
		this.x = x;
		this.y = y;
		init();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
	}
	
	private void init() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(y,x,10,10));
		
		startIndex = 0;
		back = null;
		next = null;
		showPage();
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		if (back != null) {
			buttonPanel.add(back, BorderLayout.WEST);
		}
		if (next != null) {
			buttonPanel.add(next, BorderLayout.EAST);
		}
		done = new JButton("Done");
		buttonPanel.add(done, BorderLayout.CENTER);
		
		JPanel cPane = new JPanel();
		cPane.setLayout(new BorderLayout());
		cPane.add(mainPanel, BorderLayout.CENTER);
		cPane.add(buttonPanel, BorderLayout.SOUTH);
		this.setContentPane(cPane);
	}
	
	private void showPage() {
		File[] files = directory.listFiles(dirFilter);
		int showingCount = x*y;
		
		for (int i=startIndex; (i<showingCount && i<files.length); ++i) {
			File f = files[i];
			MediaFile mFile = new MediaFile(f);
			ImagePanel iPanel = new ImagePanel(mFile);
			mainPanel.add(iPanel);
			endIndex = i;
		}
		
		if (startIndex > 0) {
			back = new JButton("Back");			
		}
		
		if (endIndex < (files.length - 1)) {
			next = new JButton("Next");			
		}
				
	}
}
