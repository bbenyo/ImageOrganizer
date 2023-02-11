package bb.imgo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import bb.imgo.struct.FileUtilities;
import bb.imgo.struct.MediaFile;
import bb.util.ImageUtils;
import bb.util.MathUtils;
import bb.util.NoopMouseListener;

@SuppressWarnings("serial")

public class ImagePanel extends JPanel {
	private static Logger logger = Logger.getLogger(ImagePanel.class.getName());
	// Image icon
	// Stat label
	// Radio button (good/trash/archive)
	
	protected ImageIcon imgIcon;
	protected JLabel statsLabel;
	protected JLabel sizeLabel;
	protected JLabel title;
	
	protected JRadioButton goodButton;
	protected JRadioButton trashButton;
	protected JRadioButton archiveButton;
	
	protected ButtonGroup tagGroup;

	protected MediaFile mFile;
	
	// Child popup frame for a larger view
	JFrame zoomedFrame = null;
	
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
	
	// No arguments for width/height, only scale if we're too large
	public void displayCenter() {
		displayCenter(-1, -1);
	}
	
	public void displayCenter(int scaledWidth, int scaledHeight) {
		logger.info("Loading image from "+mFile.getURL());
		URL imgUrl = mFile.getURL();
		try {
			if (imgUrl != null) {
				BufferedImage img = null;
				img = ImageIO.read(imgUrl);		
				if (img != null) {
					if (scaledWidth < 0 || scaledHeight < 0) {
						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
						int width = MathUtils.toInt(screenSize.getWidth());
						int height = MathUtils.toInt(screenSize.getHeight());
						Dimension resized = ImageUtils.resizeImage(img.getWidth(), img.getHeight(), width, height);
						scaledWidth = MathUtils.toInt(resized.getWidth());
						scaledHeight = MathUtils.toInt(resized.getHeight());
					}
					// TODO: Allow you to set the size via properties
					Image imgResized = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT);
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
		
		JLabel iLbl = new JLabel(imgIcon);
		add(iLbl, BorderLayout.CENTER);
		iLbl.addMouseListener(new NoopMouseListener() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (zoomedFrame != null) {
					zoomedFrame.setVisible(false);
					zoomedFrame.dispose();
					zoomedFrame = null;
					return;
				}
				ImagePanel zoomed = new ImagePanel(mFile);
				zoomed.displayCenter();
				zoomed.zoomedFrame = zoomed.createFrame();
				// zoomed.zoomedFrame.setLocation(width-25, height-25);
				zoomed.zoomedFrame.setVisible(true);
			}			
		});
	}
	
	private void init() {
		setLayout(new BorderLayout());
		JLabel name = new JLabel(mFile.getBaseFile().getName());
		name.setFont(arial14);
		name.setHorizontalAlignment(SwingConstants.CENTER);
		add(name, BorderLayout.NORTH);
		JLabel iLbl = new JLabel();
		iLbl.setPreferredSize(new Dimension(400,300));
		add(iLbl, BorderLayout.CENTER);
		//displayCenter();
		
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
	
	public void clean() {
		logger.info("Cleaning ImagePanel");
		if (imgIcon != null && imgIcon.getImage() != null) {
			logger.info("Flushing imageIcon");
			imgIcon.getImage().flush();
		}
		imgIcon = null;
		
	}
}


