package bb.imgo.handlers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.MD5Checksum;
import bb.imgo.PropertyNames;
import bb.imgo.struct.ActionLog;
import bb.imgo.struct.MediaFile;

// Verify that a defined backup directory has a copy of every file here
public class VerifyBackup extends MediaHandler {
	static private Logger logger = Logger.getLogger(VerifyBackup.class.getName());

	File imageBackupRoot = new File("X:\\Pictures");
	File videoBackupRoot = new File("Y:\\All Videos");
	File imageBackupRootGood = new File("X:\\Good Pictures");
	File videoBackupRootGood = new File("Y:\\Good Videos");
	
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
		String imageRootGood = props.getProperty(PropertyNames.BACKUP_IMAGE_ROOT_GOOD);
		if (imageRoot != null) {
			imageBackupRootGood = new File(imageRootGood);
		}
		String videoRootGood = props.getProperty(PropertyNames.BACKUP_VIDEO_ROOT_GOOD);
		if (videoRoot != null) {
			videoBackupRootGood = new File(videoRootGood);
		}
		
		doChecksum = Boolean.parseBoolean(props.getProperty(PropertyNames.BACKUP_CHECK_MD5));	
		
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
	
	public boolean handleGoodBackup(MediaFile mFile) {
		File regBackup = getBackupFile(mFile);
		if (regBackup.exists()) {
			File gBackup = getGoodBackupFile(mFile);
			if (regBackup.equals(gBackup)) {
				logger.debug("Good backup is the same as regular backup");
				return true;
			} else {
				try {
					logger.info("Copying backup to "+gBackup.getAbsolutePath());
					main.copyFile(regBackup, gBackup);
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			logger.warn("Backup not found for "+mFile.getBaseName());
			File gBackup = getGoodBackupFile(mFile);
			try {
				logger.info("Copying backup to "+gBackup.getAbsolutePath());
				main.copyFile(mFile.getBaseFile(), gBackup);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public String printConfig(String indent) {
		StringBuffer sb = new StringBuffer();
		sb.append(indent+"Image Backup Root: "+imageBackupRoot+System.lineSeparator());
		sb.append(indent+"Video Backup Root: "+videoBackupRoot);
		return sb.toString();
	}
	
	protected boolean verifyBackupFile(File f1, File f2) {
		try {
			if (!doChecksum) {
				if (f1.length() == f2.length()) {
					logger.debug("Backup "+f2+" verified with size check");
					return true;
				} else {
					logger.warn("Backup "+f2+" has a different size: "+f1.length()+" vs "+f2.length());
					return false;
				}
			} else {
				if (f1.length() != f2.length()) {
					// No need to check the md5, sizes are different
					logger.warn("Backup is a different size, no need to check md5");
					return false;
				}
				String cs = MD5Checksum.getMD5Checksum(f1.getAbsolutePath());
				String bs = MD5Checksum.getMD5Checksum(f2.getAbsolutePath());
				if (!cs.equals(bs)) {
					logger.warn("Backup doesn't match MD5 sums: "+cs+" vs "+bs);
					return false;
				} else {
					// backup matches
					logger.debug("Backup "+f2+" verified with MD5 checksum");
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public File getBackupFile(MediaFile f1) {
		File backupFile = null;
		if (f1.isImageFile()) {
			backupFile = f1.getNewFilePath(main.getRootDirectory(), imageBackupRoot);
		} else if (f1.isVideoFile()) {
			backupFile = f1.getNewFilePath(main.getRootDirectory(), videoBackupRoot);			
		} else {
			logger.error("Logic error, should have filtered out any non-image/video files with the fileFilter!");
			return null;
		}
		return backupFile;
	}
	
	public File getGoodBackupFile(MediaFile f1) {
		File backupFile = null;
		if (f1.isImageFile()) {
			backupFile = f1.getNewFilePath(main.getRootDirectory(), imageBackupRootGood);
		} else if (f1.isVideoFile()) {
			backupFile = f1.getNewFilePath(main.getRootDirectory(), videoBackupRootGood);			
		} else {
			logger.error("Logic error, should have filtered out any non-image/video files with the fileFilter!");
			return null;
		}
		return backupFile;
	}
	
	@Override
	public boolean handleFile(MediaFile f1) {
		File backupFile = getBackupFile(f1);
		if (backupFile == null) {
			return false;
		}
		
		if (backupFile.exists()) {
			if (verifyBackupFile(f1.getBaseFile(), backupFile)) {
				logger.debug("Backup file "+backupFile+" found");
				return false;
			}
		} else {
			logger.debug("Backup file "+backupFile+" not found");
		}
		
		// TODO: Check md5sums for any backup file that matches		
		String base = f1.getBaseName();
		PrefixFileFilter pff = new PrefixFileFilter(base);
		// Check for a backup file using the datetime naming convention
		// For images: IMG_YYYYMMDD_HHMMSS.JPG
		if (f1.isImageFile()) {
			long origDate = f1.getOriginalTimestamp();
			logger.info("Original Date: "+origDate);
			Date d1 = new Date(origDate);
			String imgName = "IMG_"+ymdhms.format(d1);
			pff.addPrefix(imgName);
		}
		
		File[] prefixFiles = backupFile.getParentFile().listFiles(pff);
		if (prefixFiles != null) {
			for (File bFile : prefixFiles) {
				if (doChecksum) {
					if (verifyBackupFile(f1.getBaseFile(), bFile)) {
						logger.debug("Backup file "+backupFile+" found");
						return false;
					}
				} else {
					logger.debug("Backup file "+backupFile+" found, checksum verification disabled");
					return false;
				}
			}
		}
		
		// No joy, we found nothing that looks like it.  Could check file sizes, or all files in the directory
		logger.info("Backup version doesn't exist: "+backupFile);
		ActionLog al = main.addCopyActionLog(f1.getBaseFile().getAbsolutePath(), backupFile.getAbsolutePath(), "Backup");
		if (main.moveFiles) {
			al.executeAction(main);
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
	
	public File getImageBackupRoot() {
		return imageBackupRoot;
	}

	public void setImageBackupRoot(File imageBackupRoot) {
		this.imageBackupRoot = imageBackupRoot;
	}

	public File getVideoBackupRoot() {
		return videoBackupRoot;
	}

	public void setVideoBackupRoot(File videoBackupRoot) {
		this.videoBackupRoot = videoBackupRoot;
	}

	private class PrefixFileFilter implements FileFilter {

		ArrayList<String> prefixes = null;
		
		public PrefixFileFilter(String prefix) {
			this.prefixes = new ArrayList<String>();
			prefixes.add(prefix);
		}
		
		public void addPrefix(String p2) {
			prefixes.add(p2);
		}
		
		@Override
		public boolean accept(File pathname) {
			if (pathname != null) {
				for (String prefix : prefixes) {
					if (pathname.getName().startsWith(prefix)) {
						return true;
					}
				}						
			}
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "Backup all files to an image or video archive directory";
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		HashMap<String, String> configs = new HashMap<String, String>();
		configs.put(PropertyNames.BACKUP_IMAGE_ROOT, imageBackupRoot.getAbsolutePath());
		configs.put(PropertyNames.BACKUP_VIDEO_ROOT, videoBackupRoot.getAbsolutePath());
		configs.put(PropertyNames.BACKUP_IMAGE_ROOT_GOOD, imageBackupRootGood.getAbsolutePath());
		configs.put(PropertyNames.BACKUP_VIDEO_ROOT_GOOD, videoBackupRootGood.getAbsolutePath());
		return configs;
	}

	@Override
	public void setConfigurationOption(String key, String value) {
		File f1 = new File(value);
		if (!f1.isDirectory()) {
			logger.error(value+" is not a directory!");
			return;
		}
		if (key.equalsIgnoreCase(PropertyNames.BACKUP_IMAGE_ROOT)) {
			imageBackupRoot = f1;
			logger.info("Image backup directory is now "+f1);
		} else if (key.equalsIgnoreCase(PropertyNames.BACKUP_VIDEO_ROOT)) {
			videoBackupRoot = f1;
			logger.info("Video backup directory is now "+f1);
		} else if (key.equalsIgnoreCase(PropertyNames.BACKUP_IMAGE_ROOT_GOOD)) {
			imageBackupRootGood = f1;
			logger.info("Good Image backup directory is now "+f1);
		} else if (key.equalsIgnoreCase(PropertyNames.BACKUP_VIDEO_ROOT_GOOD)) {
			videoBackupRootGood = f1;
			logger.info("Good Video backup directory is now "+f1);
		} else {
			logger.error("Unknown config option: "+key);
		}		
	}
}
