package bb.imgo.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import bb.imgo.OrganizeMedia;
import bb.imgo.OrganizeMediaUIInterface;
import bb.imgo.handlers.MediaHandler;
import bb.imgo.handlers.UserChooser;
import bb.imgo.handlers.VideoRenameAndTag;

@SuppressWarnings("serial")
public class OverviewFrame extends JFrame implements OrganizeMediaUIInterface {

	static private Logger logger = Logger.getLogger(OverviewFrame.class.getName());
	
	BrowseLabel startSubdirBrowse;
	BrowseLabel userProgressBrowse;
	
	BrowseLabel unorganizedDirectoryBrowse;
	BrowseLabel goodDirectoryBrowse;
	BrowseLabel trashDirectoryBrowse;
	BrowseLabel trashVideoDirectoryBrowse;
	BrowseLabel archiveImageDirectoryBrowse;
	BrowseLabel archiveVideoDirectoryBrowse;
	
	JScrollPane actionLogPane;
	JTextArea actionLogArea;
	
	JTextArea activeHandlers;
	JButton editHandlers;
	JCheckBox moveFilesBox;
	JCheckBox recountBox;
	JButton executeActions;
	JLabel startTimeLabel;
	JLabel statisticsLabel;
	
	JButton viewFullLogButton;
	JButton viewGoodDirectory;
	JButton viewTrashDirectory;
	
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
	static public Font labelFont = new Font("Arial", Font.PLAIN, 24);
		
	public OverviewFrame() {
		super("Media Organizer");
	}
	
	public void init(OrganizeMedia oMedia, int w, int h) {
		controller = oMedia;
		String rootDir = oMedia.getRootDirectory().getAbsolutePath();
		List<MediaHandler> handlers = oMedia.getHandlers();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1.0;
		gbc.gridwidth=3;
		gbc.gridheight=1;
		gbc.insets = new Insets(5,5,5,5); 
		gbc.fill=GridBagConstraints.BOTH;
		gbc.anchor=GridBagConstraints.LINE_START;

		unorganizedDirectoryBrowse = new BrowseLabel("Unorganized Media", "Unorganized", controller.getRootDirectory());
		unorganizedDirectoryBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);		
				
		startSubdirBrowse = new BrowseLabel("Start in Subdir", "StartSubdir", controller.getStartSubdir());
		startSubdirBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);
		
		// TODO: Allow handlers to contribute to a panel (tabbed pane?) on the ui instead of this custom code
		MediaHandler uHandler = oMedia.getSpecificHandler(UserChooser.class);
		if (uHandler == null) {
			uHandler = oMedia.getSpecificHandler(VideoRenameAndTag.class);
		}
		
		if (uHandler != null) {
			UserChooser uChoose = (UserChooser)uHandler;
			String cpd = uChoose.getCurrentProgressDirectory();
			if (cpd == null) {
				userProgressBrowse = new BrowseLabel("Start at the beginning", "UserProgress", controller.getRootDirectory());
				userProgressBrowse.setLabelPrefix("Resume at");
			} else {
				userProgressBrowse = new BrowseLabel("Resume at", "UserProgress", new File(cpd));
			}
			userProgressBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);
		}
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 4;
		gbc.gridheight = 1;
		mainPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
		
		goodDirectoryBrowse = new BrowseLabel("Good Files", "Good", controller.getGoodDir());
		goodDirectoryBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);
				
		trashDirectoryBrowse = new BrowseLabel("Trash Files", "Trash", controller.getTrashDir());
		trashDirectoryBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);
		
		File archiveGood = controller.getArchiveImageDir();
		if (archiveGood != null) {
			archiveImageDirectoryBrowse = new BrowseLabel("Image Archive", "ImageArchive", controller.getArchiveImageDir());
			archiveImageDirectoryBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);
		}
		
		File archiveVideoGood = controller.getArchiveVideoDir();
		if (archiveVideoGood != null) {
			archiveVideoDirectoryBrowse = new BrowseLabel("Video Archive", "VideoArchive", controller.getArchiveVideoDir());
			archiveVideoDirectoryBrowse.addToGridBagLayout(gbc, mainPanel, this, oMedia, labelFont);
		}
		
		// TODO: toggle on/off handlers via checkboxes
		StringBuffer handlerStr = new StringBuffer("Handlers: ");
		for (MediaHandler handler : handlers) {
			handlerStr.append(handler.getLabel()+" ");
		}
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=4;
		gbc.gridheight=2;
		gbc.weighty=0.25;
		activeHandlers = new JTextArea(handlerStr.toString());
		activeHandlers.setFont(labelFont);
		activeHandlers.setLineWrap(true);
		activeHandlers.setWrapStyleWord(true);
		mainPanel.add(new JScrollPane(activeHandlers), gbc);
		
		editHandlers = new JButton("Edit Handlers");
		editHandlers.setFont(labelFont);
		editHandlers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showEditHandlerDialog();
			}
		});
		
		gbc.gridy+=2;
		gbc.weighty=0;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		mainPanel.add(editHandlers, gbc);
		
		moveFilesBox = new JCheckBox("Auto Execute Actions?");
		moveFilesBox.setFont(labelFont);
		if (oMedia.moveFiles) {
			moveFilesBox.setSelected(true);
		} else {
			moveFilesBox.setSelected(false);
		}
		moveFilesBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setMoveFiles(moveFilesBox.isSelected());
				if (moveFilesBox.isSelected()) {
					executeActions.setEnabled(false);
				} else {
					executeActions.setEnabled(true);
				}
			}
		});
		
		gbc.gridx+=1;
		gbc.weighty=0;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		mainPanel.add(moveFilesBox, gbc);
		
		recountBox = new JCheckBox("Count Files again? ");
		recountBox.setFont(labelFont);
		
		if (oMedia.isRecountFiles()) {
			recountBox.setSelected(true);
		} else {
			recountBox.setSelected(false);
		}
		recountBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setRecountFiles(recountBox.isSelected());
			}
		});

		gbc.gridwidth=1;
		gbc.gridx=2;
		mainPanel.add(recountBox, gbc);
		
		executeActions = new JButton("Execute Actions");
		executeActions.setFont(labelFont);
		executeActions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				oMedia.executeActionLog();
			}
		});
		if (oMedia.moveFiles) {
			executeActions.setEnabled(false);
		}

		gbc.gridx=3;
		mainPanel.add(executeActions, gbc);

				
		actionLogArea = new JTextArea("Action Log:\n");
		actionLogArea.setFont(labelFont);
		actionLogPane = new JScrollPane(actionLogArea);
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth=4;
		gbc.gridheight=6;
		gbc.weighty=0.75;
		gbc.fill=GridBagConstraints.BOTH;
		mainPanel.add(actionLogPane, gbc);
		
		statisticsLabel = new JLabel();
		statisticsLabel.setFont(labelFont);
		gbc.gridy = gbc.gridy + 6;
		gbc.weighty=0;
		gbc.gridwidth=3;
		gbc.gridheight=1;
		startTimeLabel = new JLabel("Not Started");
		startTimeLabel.setFont(labelFont);
		updateStatistics();
		mainPanel.add(startTimeLabel, gbc);
		
		viewFullLogButton = new JButton("View Full Log");
		viewFullLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File gd = new File("io.log"); // TODO: get the log file from log4j.properties
				if (gd != null && gd.exists()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(gd);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		gbc.gridx=3;
		gbc.gridwidth=1;
		mainPanel.add(viewFullLogButton, gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=3;
		gbc.gridheight=3;
		mainPanel.add(statisticsLabel, gbc);
		
		viewGoodDirectory = new JButton("Good Directory");
		viewGoodDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File gd = controller.getGoodDir();
				if (gd != null && gd.exists()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(gd);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		gbc.gridx=3;
		gbc.gridy++;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		mainPanel.add(viewGoodDirectory, gbc);
		
		viewTrashDirectory = new JButton("Trash Directory");
		viewTrashDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File gd = controller.getTrashDir();
				if (gd != null && gd.exists()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(gd);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		gbc.gridy++;
		mainPanel.add(viewTrashDirectory, gbc);
		
		curDirectoryLabel = new JLabel("Working in: ");
		curDirectoryLabel.setFont(labelFont);
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth=4;
		gbc.gridheight=1;
		mainPanel.add(curDirectoryLabel, gbc);
		
		statusLabel = new JLabel("Status");
		statusLabel.setFont(labelFont);
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth=4;
		gbc.gridheight=2;
		mainPanel.add(statusLabel, gbc);
				
		progress = new JProgressBar();
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setValue(1);
		progress.setFont(labelFont);
		gbc.gridy+=2;
		gbc.gridheight=1;
		mainPanel.add(progress, gbc);
				
		JPanel buttonPanel = new JPanel();
		startButton = new JButton("Start");
		startButton.setFont(labelFont);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.startThread();
				start();
				startButton.setEnabled(false);
			}				
		});
		
		stopButton = new JButton("Pause");
		stopButton.setFont(labelFont);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (stopButton.getText().equalsIgnoreCase("Pause")) {
					controller.pause();
				} else {
					controller.resume();
				}
			}
		});
		
		cancelButton = new JButton("Cancel");
		cancelButton.setFont(labelFont);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.abort();
				stopButton.setText("Pause");
				startButton.setEnabled(true);
			}
		});
		
		exitButton = new JButton("Exit");
		exitButton.setFont(labelFont);
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.exit();
			}
		});
		
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(exitButton);
		
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth=4;
		gbc.gridheight=1;
		mainPanel.add(buttonPanel, gbc);
		
		this.setContentPane(mainPanel);
		this.pack();
		this.setSize(w,h);
	}
	
	public void showEditHandlerDialog() {
		EditHandlersFrame ehFrame = new EditHandlersFrame(controller);
		ehFrame.setLocationRelativeTo(this);
		ehFrame.setVisible(true);
	}
	
	public void setPaused() {
		stopButton.setText("Resume");
		stopButton.validate();
	}
	
	public void setResumed() {
		stopButton.setText("Pause");
		stopButton.validate();
	}
	
	public void setAborted() {
		startButton.setEnabled(true);
		stopButton.setText("Pause");
	}
	
	public void handleFile(String fname, boolean good, boolean delete) {
		filesHandled++;
		progress.setString(fname);
		progress.setValue(progress.getValue() + 1);
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
	
	public void updateActionLog(String log) {
		actionLogArea.append(System.lineSeparator()+log);
	}
	
	public void clearActionLog() {
		actionLogArea.setText("Action Log:\n");
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		startTimeLabel.setText("Started at "+ymdhms.format(new Date(startTime)));
		startTimeLabel.repaint();
	}

	@Override
	public void initializeUI(int progressmax) {
		progress.setMinimum(0);
		progress.setMaximum(progressmax);
		progress.setValue(0);
		progress.setStringPainted(true);
		progress.revalidate();
		
		actionLogArea.setText("Action Log:\n");
		filesHandled = 0;
		filesToGood = 0;
		filesToDelete = 0;
		updateStatistics();
	}

	@Override
	public void incrementProgress(int count) {
		progress.setValue(progress.getValue() + count);		
	}
	
	public void initializeProgress(int maximum) {
		progress.setMinimum(0);
		progress.setMaximum(maximum);
		progress.setValue(0);
		progress.setStringPainted(true);
		progress.revalidate();
	}
	
}
