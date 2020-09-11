package bb.imgo.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bb.imgo.DirectoryController;

public class BrowseLabel {
	
	protected File currentFile;
	protected String labelPrefix;
	protected JLabel label;
	protected JButton browse;
	protected String controllerAction;
	
	protected boolean directoryOnly = true;
	
	public BrowseLabel(String lblText, String controllerAction, File curFile) {
		this.currentFile = curFile;
		this.controllerAction = controllerAction;
		this.labelPrefix = lblText;
		label = new JLabel();
		browse = new JButton("Browse");
		setLabelText();
	}
	
	protected void setLabelText() {
		label.setText(labelPrefix+": "+currentFile.getAbsolutePath());
	}
	
	public boolean isDirectoryOnly() {
		return directoryOnly;
	}
	
	public String getLabelPrefix() {
		return labelPrefix;
	}

	public void setLabelPrefix(String labelPrefix) {
		this.labelPrefix = labelPrefix;
	}

	public void setDirectoryOnly(boolean directoryOnly) {
		this.directoryOnly = directoryOnly;
	}

	public void addToGridBagLayout(GridBagConstraints gbc, JPanel mainPanel, JFrame frame, DirectoryController controller, Font font) {
		label.setFont(font);
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth=3;
		gbc.gridheight=1;
		mainPanel.add(label, gbc);
		
		browse.setFont(font);
		gbc.gridx=3;
		gbc.weightx=0;
		gbc.gridwidth=1;
		mainPanel.add(browse, gbc);
		
		browse.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser rChooser = new JFileChooser();
				rChooser.setCurrentDirectory(currentFile);
				if (directoryOnly) {
					rChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}
				int retval = rChooser.showDialog(frame, "Select");
				if (retval == JFileChooser.APPROVE_OPTION) {
					File sFile = rChooser.getSelectedFile();
					controller.setDirectory(controllerAction, sFile);
					currentFile = sFile;
					setLabelText();
				}				
			}
		});	
	}
	
}
