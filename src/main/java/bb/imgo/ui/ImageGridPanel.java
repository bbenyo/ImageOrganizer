package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import bb.imgo.struct.MediaFile;

@SuppressWarnings("serial")
public class ImageGridPanel extends JFrame {
	static private Logger logger = Logger.getLogger(ImageGridPanel.class.getName());
	
	JLabel countLabel;
	JPanel mainPanel;
	JPanel buttonPanel;
	JButton back;
	JButton next;
	JButton done;
	JButton cancel;
	
	File directory;
	
	int x,y;
	int startIndex = -1;
	int endIndex = -1;
		
	boolean wasCancelled = false;
	
	ArrayList<MediaFile> mediaFiles;
		
	public ImageGridPanel(File directory, ArrayList<MediaFile> mediaFiles, int x, int y) {
		super(directory.getAbsolutePath());
		this.mediaFiles = mediaFiles;
		this.directory = directory;
		this.x = x;
		this.y = y;
		this.startIndex = 0;
		init();
		showPage();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		WindowListener exitListener = new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		        cleanup();
		    }
		};
		this.addWindowListener(exitListener);
		this.pack();
	}
	
	private void init() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(y,x,10,10));
		
		back = null;
		next = null;

		JPanel cPane = new JPanel();
		cPane.setLayout(new BorderLayout());
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
	
		done = new JButton("Done");
		done.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanup();
			}
		});
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wasCancelled = true;
				cleanup();				
			}
		});
		
		back = new JButton("<<");
		back.setFont(new Font("Arial", Font.BOLD, 32));
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("BACK");
				startIndex = startIndex - (x*y);
				if (startIndex < 0) {
					startIndex = 0;
				}
				showPage();
				loadImagesThread();
			}
		});
		
		next = new JButton(">>");	
		next.setFont(new Font("Arial", Font.BOLD, 32));
		next.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startIndex = startIndex + (x*y);
				logger.info("NEXT");
				showPage();
				loadImagesThread();
			}
			
		});
		
		countLabel = new JLabel("Showing 0 out of 0 images");
		countLabel.setFont(OverviewFrame.arial18);
		
		cPane.add(countLabel, BorderLayout.NORTH);
		cPane.add(mainPanel, BorderLayout.CENTER);
		cPane.add(buttonPanel, BorderLayout.SOUTH);
		this.setContentPane(cPane);
	}
	
	public void cleanup() {
		setVisible(false);
		dispose();
		// Tell anyone waiting that we're done
		// UserChooser waits for this
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	public boolean wasCancelled() {
		return wasCancelled;
	}

	protected ImagePanel createPanel(MediaFile mFile) {
		return new ImagePanel(mFile);
	}
	
	public void showPage() {		
		logger.info("Showing page for "+directory+" starting at "+startIndex);
		for (Component c : mainPanel.getComponents()) {
			if (c instanceof ImagePanel) {
				((ImagePanel)c).clean();
			}
		}
		mainPanel.removeAll();
		buttonPanel.removeAll();
		mainPanel.revalidate();

		countLabel.setText("Showing "+(startIndex+1)+" - "+(endIndex+1)+" of "+(mediaFiles.size())+" images");
		countLabel.setFont(OverviewFrame.arial18);
		countLabel.setHorizontalAlignment(SwingConstants.CENTER);
		if (startIndex > 0) {
			buttonPanel.add(back, BorderLayout.WEST);
		}

		int expectedEndIndex = startIndex + x*y;
		for (int i=startIndex; (i<expectedEndIndex && i<mediaFiles.size()); ++i) {
			MediaFile mFile = mediaFiles.get(i);
			ImagePanel iPanel = createPanel(mFile);
			mainPanel.add(iPanel);
			endIndex = i;
		}	
		
		JPanel bcPanel = new JPanel();
		bcPanel.add(cancel);
		if (endIndex < (mediaFiles.size() - 1)) {
			buttonPanel.add(next, BorderLayout.EAST);
		} else {
			bcPanel.add(done);
		}
		buttonPanel.add(bcPanel, BorderLayout.CENTER);
					
		revalidate();	
		repaint();
	}
	
	public void loadImages() {
		for (Component c : mainPanel.getComponents()) {
			if (c instanceof ImagePanel) {
				((ImagePanel)c).displayCenter();
				revalidate();
			}
		}
	}
	
	// Load images on a thread off the AWT event thread
	public void loadImagesThread() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				loadImages();
			}
		});
		t.start();
	}
}
