package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import bb.imgo.OrganizeMedia;
import bb.imgo.OrganizeMediaUIInterface;
import bb.imgo.handlers.MediaHandler;

@SuppressWarnings("serial")
public class OverviewFrame extends JFrame implements OrganizeMediaUIInterface {
	
	JLabel rootDirectoryLabel;
	// JButton rootDirectoryBrowse;
	
	JLabel activeHandlers;
	JLabel startTimeLabel;
	JLabel statisticsLabel;
	
	JLabel curDirectoryLabel;
	JLabel statusLabel;
	
	JProgressBar progress;
	
	JButton startButton;
	JButton stopButton;
	JButton cancelButton;
	JButton exitButton;
	
	int filesHandled = 0;
	int filesToDelete = 0;
	int filesToGood = 0;
	long startTime = 0;
	
	static public SimpleDateFormat ymdhms = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
	
	OrganizeMedia controller;
	// Replace with an interface if needed
	
	// TODO: Specify via properties
	Font arial18 = new Font("Arial", Font.PLAIN, 18);
	
	public OverviewFrame() {
		super("Media Organizer");
	}
	
	public void init(OrganizeMedia oMedia) {
		controller = oMedia;
		String rootDir = oMedia.getRootDirectory().getAbsolutePath();
		List<MediaHandler> handlers = oMedia.getHandlers();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		rootDirectoryLabel = new JLabel("  Root: "+rootDir+"  ");
		rootDirectoryLabel.setFont(arial18);
		
		StringBuffer handlerStr = new StringBuffer("<html><h1>Media Handlers in Operation</h1>");
		for (MediaHandler handler : handlers) {
			handlerStr.append("<br><center>"+handler.getLabel()+"</center>");
		}
		handlerStr.append("</html>");
		activeHandlers = new JLabel(handlerStr.toString());
		activeHandlers.setFont(arial18);
		
		statisticsLabel = new JLabel();
		statisticsLabel.setFont(arial18);
		startTimeLabel = new JLabel("Not Started");
		startTimeLabel.setFont(arial18);
		updateStatistics();
		
		mainPanel.add(rootDirectoryLabel, BorderLayout.NORTH);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(activeHandlers, BorderLayout.CENTER);
		p1.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(startTimeLabel, BorderLayout.NORTH);
		p2.add(statisticsLabel, BorderLayout.CENTER);
		p2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 4));
		p1.add(p2, BorderLayout.SOUTH);
		
		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout());
		curDirectoryLabel = new JLabel("Working in: ");
		curDirectoryLabel.setFont(arial18);
		curDirectoryLabel.setPreferredSize(new Dimension(400,30));
		statusLabel = new JLabel("Status");
		statusLabel.setFont(arial18);
		p3.add(curDirectoryLabel, BorderLayout.NORTH);
		p3.add(statusLabel, BorderLayout.CENTER);
		p3.setBorder(BorderFactory.createLineBorder(Color.BLUE, 4));
		
		progress = new JProgressBar();
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setValue(1);
		p3.add(progress, BorderLayout.SOUTH);
		
		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BorderLayout());
		cPanel.add(p1, BorderLayout.NORTH);
		cPanel.add(p3, BorderLayout.CENTER);
		
		mainPanel.add(cPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread oThread = new Thread(new Runnable() {
					public void run() {
						controller.organize();
					}
				});
				oThread.start();
				start();
			}				
		});
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.pause();
				startButton.setText("Resume");
			}
		});
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.abort();
				startButton.setText("Start");
			}
		});
		
		exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.exit();
			}
		});
		
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(exitButton);
		
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		// TODO: Set size via propeties
		this.pack();
		this.setSize(600,500);
	}
	
	public void handleFile(boolean good, boolean delete) {
		filesHandled++;
		if (good) {
			filesToGood++;
		} 
		if (delete) {
			filesToDelete++;
		}
		updateStatistics();
	}
		
	private void updateStatistics() {
		StringBuffer sb = new StringBuffer("<html>Files Handled: ");
		sb.append(filesHandled);
		sb.append("<br>Good Files: ");
		sb.append(filesToGood);
		sb.append("<br>Files to Delete: ");
		sb.append(filesToDelete);
		sb.append("</html>");
		statisticsLabel.setText(sb.toString());
		statisticsLabel.repaint();
	}
	
	public void changeCurrentDirectory(String cwd) { 
		curDirectoryLabel.setText("Working in: "+cwd);
		curDirectoryLabel.repaint();
	}
	
	public void updateStatus(String status) {
		statusLabel.setText(status);
		// TODO: Do I need to call these repaints?
		statusLabel.repaint();
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		startTimeLabel.setText("Started at "+ymdhms.format(new Date(startTime)));
		startTimeLabel.repaint();
	}

	@Override
	public void initializeProgressBar(int min, int max) {
		progress.setMinimum(min);
		progress.setMaximum(max);
		progress.setValue(min);
		progress.revalidate();
	}

	@Override
	public void updateProgress(int value) {
		progress.setValue(value);		
	}

}
