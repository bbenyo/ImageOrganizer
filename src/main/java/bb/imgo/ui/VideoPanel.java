package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import bb.imgo.struct.MediaFile;

@SuppressWarnings("serial")
public class VideoPanel extends ImagePanel {
	private static Logger logger = Logger.getLogger(VideoPanel.class.getName());
	JTextField txtField;

	public VideoPanel(MediaFile mFile) {
		super(mFile);
	}
	
	@Override
	protected void displayCenter() {
		JButton playBtn = new JButton("PLAY");
		playBtn.setPreferredSize(new Dimension(100,40));
		JLabel rename = new JLabel("Rename video to: ");
		rename.setFont(OverviewFrame.arial18);
		txtField = new JTextField("");
		txtField.setFont(OverviewFrame.arial18);
		txtField.setPreferredSize(new Dimension(300,40));
		
		if (mFile.getRenameTo() != null) {
			txtField.setText(mFile.getRenameTo());
		}
		
		txtField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeName();
			}
		});
			
		
		playBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(mFile.getBaseFile());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
				
		JPanel vPanel = new JPanel();
		JPanel nPanel = new JPanel();
		nPanel.add(playBtn, BorderLayout.CENTER);
		vPanel.setLayout(new BorderLayout());
		vPanel.add(nPanel, BorderLayout.CENTER);
		JPanel rPanel = new JPanel();
		rPanel.setLayout(new BorderLayout());
		rPanel.add(rename, BorderLayout.WEST);
		rPanel.add(txtField, BorderLayout.CENTER);
		vPanel.add(rPanel, BorderLayout.SOUTH);
		
		add(vPanel, BorderLayout.CENTER);
	}
	
	public void changeName() {
		String ext = mFile.getExt();
		String newname = txtField.getText();
		if (newname.length() > 0) {
			if (!newname.endsWith("."+ext)) {
				newname = newname+"."+ext;
			}
			logger.info("Setting renameTo to: "+newname);
			mFile.setRenameTo(newname);
			mFile.setRenameReason("User via VideoRenameAndTag");
		}
	}
	
	public void finalize() {
		changeName();
	}

}
