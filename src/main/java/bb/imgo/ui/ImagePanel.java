package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import bb.imgo.struct.FileUtilities;
import bb.imgo.struct.MediaFile;

@SuppressWarnings("serial")

public class ImagePanel extends JPanel {
	static private Logger logger = Logger.getLogger(ImagePanel.class.getName());
	// Image icon
	// Stat label
	// Radio button (good/trash/archive)
	
	ImageIcon imgIcon;
	JLabel statsLabel;
	JLabel sizeLabel;
	JLabel title;
	
	JRadioButton goodButton;
	JRadioButton trashButton;
	JRadioButton archiveButton;
	
	ButtonGroup tagGroup;

	MediaFile mFile;
	
	// TODO: Allow you to specify via properties
	Font arial14 = new Font("Arial", Font.BOLD, 14);
	
	public ImagePanel(MediaFile mFile) {
		super();
		this.mFile = mFile;
		init();
	}
	
	public JFrame createFrame() {
		JFrame frame = new JFrame(mFile.getBaseFile().getName());
		frame.setContentPane(this);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		return frame;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		URL imgUrl = mFile.getURL();
		try {
			if (imgUrl != null) {
				BufferedImage img = null;
				img = ImageIO.read(imgUrl);		
				if (img != null) {
					// TODO: Allow you to set the size via properties
					Image imgResized = img.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
					imgIcon = new ImageIcon(imgResized);
				} else {
					logger.info("Unable to read Image from "+imgUrl);
					imgIcon = new ImageIcon();
					mFile.setDelete("Unreadable");
				}
			} else {
				imgIcon = new ImageIcon();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		JLabel name = new JLabel(mFile.getBaseFile().getName());
		name.setFont(arial14);
		name.setHorizontalAlignment(SwingConstants.CENTER);
		add(name, BorderLayout.NORTH);
		JLabel iLbl = new JLabel(imgIcon);
		add(iLbl, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		add(bottomPanel, BorderLayout.SOUTH);
		File baseFile = mFile.getBaseFile();
		JPanel sPanel = new JPanel();
		statsLabel = new JLabel(mFile.getDateTime());
		statsLabel.setFont(arial14);
		sizeLabel = new JLabel(FileUtilities.humanReadableBytes(baseFile.length()));
		sizeLabel.setFont(arial14);
		sPanel.setLayout(new BorderLayout());
		sPanel.add(statsLabel, BorderLayout.WEST);
		sPanel.add(sizeLabel, BorderLayout.EAST);
		JLabel blank = new JLabel("");
		sPanel.add(blank, BorderLayout.CENTER);		
		bottomPanel.add(sPanel, BorderLayout.NORTH);
		
		goodButton = new JRadioButton("Good");
		goodButton.setFont(arial14);
		trashButton = new JRadioButton("Trash");
		trashButton.setFont(arial14);
		archiveButton = new JRadioButton("Archive");
		archiveButton.setFont(arial14);
		
		tagGroup = new ButtonGroup();
		tagGroup.add(goodButton);
		tagGroup.add(trashButton);
		tagGroup.add(archiveButton);
		if (mFile.isGood()) {
			goodButton.setSelected(true);
		} else if (mFile.isDelete()) {
			trashButton.setSelected(true);
		} else {
			archiveButton.setSelected(true);
		}
		
		goodButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyTags();
			}
		});
		
		trashButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyTags();
			}
		});
		archiveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyTags();
			}
		});
		
		JPanel rPanel = new JPanel();
		rPanel.add(goodButton);
		rPanel.add(trashButton);
		rPanel.add(archiveButton);
		bottomPanel.add(rPanel, BorderLayout.CENTER);
		setImageBorder();
	}
	
	private void setImageBorder() {
		setBorder(BorderFactory.createLineBorder(getBorderColor(mFile), 4));
	}
	

	private Color getBorderColor(MediaFile mf) {
		if (mf.isGood()) {
			return Color.green;
		}
		if (mf.isDelete()) {
			return Color.red;
		}
		return Color.black;
	}
		
	public void applyTags() {
		if (goodButton.isSelected()) {
			logger.info("Applying GOOD tag");
			mFile.clearDelete();
			mFile.setGood("User Choice");
		} else if (trashButton.isSelected()) {
			logger.info("Applying DELETE tag");
			mFile.clearGood();
			mFile.setDelete("User Choice");
		} else {
			mFile.clearGood();
			mFile.clearDelete();
		}
		setImageBorder();
	}
}


