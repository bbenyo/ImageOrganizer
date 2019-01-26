package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bb.imgo.struct.ImageFileFilter;
import bb.imgo.struct.MediaFile;

@SuppressWarnings("serial")
public class ImageGridPanel extends JFrame {
	static private Logger logger = Logger.getLogger(ImageGridPanel.class.getName());
	
	JPanel mainPanel;
	JPanel buttonPanel;
	JButton back;
	JButton next;
	JButton done;
	
	File directory;
	
	int x,y;
	int startIndex = -1;
	int endIndex = -1;
	
	static public FileFilter imageFilter = new ImageFileFilter();
	
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
			}
			
		});
		
		cPane.add(mainPanel, BorderLayout.CENTER);
		cPane.add(buttonPanel, BorderLayout.SOUTH);
		this.setContentPane(cPane);

		showPage();
	}
	
	public void cleanup() {
		setVisible(false);
		dispose();
	}
	
	private void showPage() {
		logger.info("Showing page for "+directory+" starting at "+startIndex);
		mainPanel.removeAll();
		buttonPanel.removeAll();
		File[] files = directory.listFiles(imageFilter);
		
		int expectedEndIndex = startIndex + x*y;
		for (int i=startIndex; (i<expectedEndIndex && i<files.length); ++i) {
			File f = files[i];
			MediaFile mFile = new MediaFile(f);
			ImagePanel iPanel = new ImagePanel(mFile);
			iPanel.setBorder(BorderFactory.createLineBorder(Color.black, 4));
			mainPanel.add(iPanel);
			endIndex = i;
		}
		
		if (startIndex > 0) {
			buttonPanel.add(back, BorderLayout.WEST);
		}
		
		if (endIndex < (files.length - 1)) {
			buttonPanel.add(next, BorderLayout.EAST);
		}
		
		JPanel bcPanel = new JPanel();
		bcPanel.add(done);
		buttonPanel.add(bcPanel, BorderLayout.CENTER);
		
		revalidate();	
		repaint();
	}
}
