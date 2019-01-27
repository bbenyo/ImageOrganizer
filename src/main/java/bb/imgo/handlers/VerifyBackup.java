package bb.imgo.handlers;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.MD5Checksum;
import bb.imgo.PropertyNames;
import bb.imgo.struct.MediaFile;

// Verify that a defined backup directory has a copy of every file here
public class VerifyBackup extends MediaHandler {
	static private Logger logger = Logger.getLogger(VerifyBackup.class.getName());
	
	File imageBackupRoot = new File("X:\\Pictures");
	File videoBackupRoot = new File("X:\\Videos");
	
	SimpleDateFormat ymdhms = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	boolean doChecksum = true;
	
	public VerifyBackup() {
		super();
	}
	
	@Override
	// Start (or restart) processing directories
	public boolean initialize(Properties props) {
		logger.info(getLabel()+" initialized");
		String imageRoot = props.getProperty(PropertyNames.BACKUP_IMAGE_ROOT);
		if (imageRoot != null) {
			imageBackupRoot = new File(imageRoot);
		}
		String videoRoot = props.getProperty(PropertyNames.BACKUP_VIDEO_ROOT);
		if (videoRoot != null) {
			videoBackupRoot = new File(videoRoot);
		}
		
		boolean failed = false;
		if (!imageBackupRoot.exists()) {
			logger.error(imageBackupRoot+" doesn't exist!");
			failed = true;
		}
		if (!videoBackupRoot.exists()) {
			logger.error(videoBackupRoot+" doesn't exist");
			failed = true;
		}
		
		return !failed;
	}
	
	@Override
	public boolean handleFile(MediaFile f1) {
		File backupFile = null;
		if (f1.isImageFile()) {
			backupFile = f1.getNewFilePath(main.getRootDirectory(), imageBackupRoot);
		} else if (f1.isVideoFile()) {
			backupFile = f1.getNewFilePath(main.getRootDirectory(), videoBackupRoot);			
		} else {
			logger.error("Logic error, should have filtered out any non-image/video files with the fileFilter!");
			return false;
		}
		
		if (!backupFile.exists()) {
			// Check for a backup file using the datetime naming convention
			// For images: IMG_YYYYMMDD_HHMMSS.JPG
			if (f1.isImageFile()) {
				long origDate = f1.getOriginalTimestamp();
				logger.info("Original Date: "+origDate);
				Date d1 = new Date(origDate);
				String imgName = "IMG_"+ymdhms.format(d1)+".JPG";
				backupFile = new File(backupFile.getParentFile(), imgName);
				if (backupFile.exists()) {
					logger.debug("Found image file with datetime format: "+imgName);
				}
			}
			
			if (backupFile.exists()) {
				if (doChecksum) {
					try {
						String cs = MD5Checksum.getMD5Checksum(f1.getBaseFile().getAbsolutePath());
						String bs = MD5Checksum.getMD5Checksum(backupFile.getAbsolutePath());
						if (!cs.equals(bs)) {
							logger.warn("Backup doesn't match MD5 sums: "+cs+" vs "+bs);
							backupFile = main.getUniqueFile(backupFile);
						} else {
							// backup matches
							logger.debug("Backup "+backupFile+" verified with MD5 checksum");
							return false;
						}
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				} else {
					logger.debug("Backup file "+backupFile+" found");
					return false;
				}
			}
					
			logger.info("Backup version doesn't exist: "+backupFile);
			main.addCopyActionLog(f1.getBaseFile().getName(), backupFile.getAbsolutePath(), "Backup");
			if (main.moveFiles) {
				try {
					main.copyFile(f1.getBaseFile(), backupFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		if (f1.isImageFile() || f1.isVideoFile()) {
			return true; 
		}
		return false;
	}
}
