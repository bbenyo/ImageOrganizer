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
import javax.swing.JTextArea;

import bb.imgo.OrganizeMedia;
import bb.imgo.OrganizeMediaUIInterface;
import bb.imgo.handlers.MediaHandler;
import bb.imgo.handlers.UserChooser;

@SuppressWarnings("serial")
public class OverviewFrame extends JFrame implements OrganizeMediaUIInterface {
	
	JLabel rootDirectoryLabel;
	JButton rootDirectoryBrowse;
	JLabel startSubdirLabel;
	JButton startSubdirBrowse;
	JLabel userProgressLabel;
	JButton userProgressBrowse;
	
	JScrollPane actionLogPane;
	JTextArea actionLogArea;
	
	JLabel activeHandlers;
	JCheckBox moveFilesBox;
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
	static public Font arial18 = new Font("Arial", Font.PLAIN, 18);
		
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
		
		rootDirectoryLabel = new JLabel("Root: "+rootDir);
		rootDirectoryLabel.setFont(arial18);
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.weightx=1.0;
		gbc.gridwidth=3;
		gbc.gridheight=1;
		gbc.insets = new Insets(5,5,5,5); 
		gbc.fill=GridBagConstraints.BOTH;
		gbc.anchor=GridBagConstraints.LINE_START;
		mainPanel.add(rootDirectoryLabel, gbc);
		
		rootDirectoryBrowse = new JButton("Browse");
		rootDirectoryBrowse.setFont(arial18);
		gbc.gridx=3;
		gbc.weightx=0;
		gbc.gridwidth=1;
		mainPanel.add(rootDirectoryBrowse, gbc);
		final JFrame moi = this;
		rootDirectoryBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser rChooser = new JFileChooser();
				rChooser.setCurrentDirectory(controller.getRootDirectory());
				rChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = rChooser.showDialog(moi, "Select");
				if (retval == JFileChooser.APPROVE_OPTION) {
					File sFile = rChooser.getSelectedFile();
					controller.setRootDirectory(sFile);
					rootDirectoryLabel.setText("Root: "+sFile.getAbsolutePath());
					controller.setStartSubdir(null);
					startSubdirLabel.setText("Start in Subdir: ");
				}				
			}
		});
		
		startSubdirLabel = new JLabel("Start in Subdir: "+controller.getStartSubdir().getAbsolutePath());
		startSubdirLabel.setFont(arial18);
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=3;
		gbc.gridheight=1;
		mainPanel.add(startSubdirLabel, gbc);
		
		startSubdirBrowse = new JButton("Browse");
		startSubdirBrowse.setFont(arial18);
		gbc.gridx=3;
		gbc.weightx=0;
		gbc.gridwidth=1;
		mainPanel.add(startSubdirBrowse, gbc);
		startSubdirBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser rChooser = new JFileChooser();
				rChooser.setCurrentDirectory(controller.getStartSubdir());
				rChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int retval = rChooser.showDialog(moi, "Select");
				if (retval == JFileChooser.APPROVE_OPTION) {
					File sFile = rChooser.getSelectedFile();
					controller.setStartSubdir(sFile);
					startSubdirLabel.setText("Start in Subdir: "+sFile.getAbsolutePath());
				}				
			}
		});
		
		MediaHandler uHandler = oMedia.getSpecificHandler(UserChooser.class);
		if (uHandler != null) {
			UserChooser uChoose = (UserChooser)uHandler;
			String cpd = uChoose.getCurrentProgressDirectory();
			if (cpd == null) {
				userProgressLabel = new JLabel("Start at the beginning: "+oMedia.getRootDirectory().getAbsolutePath());
			} else {
				userProgressLabel = new JLabel("Resume at "+cpd);
			}
			userProgressLabel.setFont(arial18);
			gbc.gridx=0;
			gbc.gridy++;
			gbc.gridwidth=3;
			gbc.gridheight=1;
			mainPanel.add(userProgressLabel, gbc);
			
			userProgressBrowse = new JButton("Change");
			userProgressBrowse.setFont(arial18);
			gbc.gridx=3;
			gbc.weightx=0;
			gbc.gridwidth=1;
			mainPanel.add(userProgressBrowse, gbc);
			userProgressBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser rChooser = new JFileChooser();
					rChooser.setCurrentDirectory(controller.getStartSubdir());
					rChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int retval = rChooser.showDialog(moi, "Select");
					if (retval == JFileChooser.APPROVE_OPTION) {
						File sFile = rChooser.getSelectedFile();
						userProgressLabel.setText("Resume at "+sFile.getAbsolutePath());
						uChoose.setCurrentProgressDirectory(sFile.getAbsolutePath());
					}				
				}
			});
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
		activeHandlers = new JLabel(handlerStr.toString());
		activeHandlers.setFont(arial18);
		mainPanel.add(new JScrollPane(activeHandlers), gbc);
		
		moveFilesBox = new JCheckBox("Move Files (or just log) ?");
		moveFilesBox.setFont(arial18);
		if (oMedia.moveFiles) {
			moveFilesBox.setSelected(true);
		} else {
			moveFilesBox.setSelected(false);
		}
		moveFilesBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.setMoveFiles(moveFilesBox.isSelected());
			}
		});
		
		gbc.gridy+=2;
		gbc.weighty=0;
		gbc.gridheight=1;
		mainPanel.add(moveFilesBox, gbc);
		
		actionLogArea = new JTextArea("Action Log:\n");
		actionLogArea.setFont(arial18);
		actionLogPane = new JScrollPane(actionLogArea);
		gbc.gridy++;
		gbc.gridheight=6;
		gbc.weighty=0.75;
		gbc.fill=GridBagConstraints.BOTH;
		mainPanel.add(actionLogPane, gbc);
		
		statisticsLabel = new JLabel();
		statisticsLabel.setFont(arial18);
		gbc.gridy = gbc.gridy + 6;
		gbc.weighty=0;
		gbc.gridwidth=3;
		gbc.gridheight=1;
		startTimeLabel = new JLabel("Not Started");
		startTimeLabel.setFont(arial18);
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
		curDirectoryLabel.setFont(arial18);
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth=4;
		gbc.gridheight=1;
		mainPanel.add(curDirectoryLabel, gbc);
		
		statusLabel = new JLabel("Status");
		statusLabel.setFont(arial18);
		gbc.gridy++;
		gbc.gridx=0;
		gbc.gridwidth=4;
		gbc.gridheight=2;
		mainPanel.add(statusLabel, gbc);
				
		progress = new JProgressBar();
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setValue(1);
		progress.setFont(arial18);
		gbc.gridy+=2;
		gbc.gridheight=1;
		mainPanel.add(progress, gbc);
				
		JPanel buttonPanel = new JPanel();
		startButton = new JButton("Start");
		startButton.setFont(arial18);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.startThread();
				start();
				startButton.setEnabled(false);
			}				
		});
		
		stopButton = new JButton("Pause");
		stopButton.setFont(arial18);
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
		cancelButton.setFont(arial18);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.abort();
				stopButton.setText("Pause");
				startButton.setEnabled(true);
			}
		});
		
		exitButton = new JButton("Exit");
		exitButton.setFont(arial18);
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

}
