package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

// Old File, to be refactored, ignore this
// TODO: refactor completely
public class OrganizePictures {
	
	static public String DEFAULT_PIC="C:/Users/Brett/Pictures";
	static public String DEFAULT_VIDEO="C:/Users/Brett/Videos";
	
	File picturesDir = null;
	File videoDir = null;
	boolean noAction = true;
	
	public OrganizePictures(File picDir, File vidDir, boolean noAction) {
		picturesDir = picDir;
		videoDir = vidDir;
		this.noAction = noAction;
	}
	
	private boolean isPictureOrVideo(File f) {
		String fname = f.getName();
		if (fname.endsWith("jpg") || fname.endsWith("gif") || fname.endsWith("mov") ||
			fname.endsWith("JPG") || fname.endsWith("GIF") || fname.endsWith("MOV")) {
			return true;
		}
		return false;
	}
	
	private boolean isLargeMovie(File f) {
		String fname = f.getName();
		if (fname.endsWith("mov") || fname.endsWith("MOV")) {
			if (f.length() > (5000 * 1024)) {
				System.out.println("Large movie: "+f.getName()+" size: "+(f.length() / 1024)+" kb");
				return true; 
			} else {
				System.out.println("Small movie: "+fname+" (IOS LivePicture probably) size: "+(f.length() / 1024)+" kb");
			}
		}
		return false;
	}
	
	public void removeDuplicates(File dir) {
		System.out.println("Removing Duplicate files in "+dir.getAbsolutePath());
		// Files first, then recursively do subdirectories
		ArrayList<File> removeUs = new ArrayList<File>();
		for (File f : dir.listFiles()) {
			if (f.isFile() && isPictureOrVideo(f)) {
				String fname = f.getName();
				int spos = fname.lastIndexOf(" ");
				if (spos > -1) {
					String prefix = fname.substring(0, spos).trim();
					String suffix = fname.substring(spos).trim();
					if (suffix.matches("(.).*")) {
						// Could be a duplicate, look for the file without the suffix
						File pOrig = new File(f.getParent(), prefix+fname.substring(fname.lastIndexOf(".")));
						System.out.println("Looking for potential duplicate for "+fname+": "+pOrig);
						if (pOrig.exists()) {
							if (checkMD5Equal(pOrig, f)) {
								System.out.println("MD5Sums matched: "+f.getName()+" is a duplicate");
								// Duplicate, remove f
								removeUs.add(f);
							}
						}
					}
				}
			}
		}
		
		for (File f : removeUs) {
			if (noAction) {
				System.out.println("Would delete "+f.getAbsolutePath());
			} else {
				System.out.println("Deleting "+f.getAbsolutePath());
				f.delete();
			}
		}

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				removeDuplicates(f);
			}
		}
	}
	
	private boolean checkMD5Equal(File f1, File f2) {
		try {
			String md5a = MD5Checksum.getMD5Checksum(f1.getAbsolutePath());
			String md5b = MD5Checksum.getMD5Checksum(f2.getAbsolutePath());
			System.out.println("MD5Sum for "+f1.getName()+": "+md5a);
			System.out.println("MD5Sum for "+f2.getName()+": "+md5b);
			if (md5a.equals(md5b)) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void moveVideos(String subDir) {
		String pPath = picturesDir.getAbsolutePath() + subDir;
		File pDir = new File(pPath);
		String toPath = videoDir.getAbsolutePath() + subDir;
		File tDir = new File(toPath);
		System.out.println("Moving Video files from "+pDir.getAbsolutePath()+" to "+tDir.getAbsolutePath());
		if (!tDir.exists()) {
			tDir.mkdirs();
		}
		for (File f : pDir.listFiles()) {
			if (f.isFile() && isLargeMovie(f)) {
				File toFile = new File(tDir, f.getName());
				if (noAction) {
					System.out.println("Would move "+f+" to "+toFile);
				} else {
					System.out.println("Moving "+f+" to "+toFile);
					try {
						Files.move(f.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
		}
		for (File f : pDir.listFiles()) {
			if (f.isDirectory()) {
				moveVideos(subDir + "\\" + f.getName());
			}
		}
	}
	
	static public void main(String[] args) {
		// Walk the source directory, look for duplicate files (same name with (num) suffix)
		//   Check a dupe is real via md5sum
		//   Delete duplicate files
		// Move all .MOV files to the Videos directory, in the same tree
		//  Small .mov files stay in pictures (these are the live photos from ios 7)
		
		String picDir = DEFAULT_PIC;
		String vidDir = DEFAULT_VIDEO;
		boolean removeDup = true;
		boolean noAction = true;
		
		try {
			for (int i=0; i<args.length; ++i) {
				String arg = args[i];
				if (arg.equalsIgnoreCase("-pictures")) {
					picDir = args[i+1];
					i++;
				} else if (arg.equalsIgnoreCase("-video")) {
					vidDir = args[i+1];
					i++;
				} else if (arg.equalsIgnoreCase("-noRemoveDup")) {
					removeDup = false;
				} else if (arg.equalsIgnoreCase("-action")) {
					noAction = false;
				} else {
					usage();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			usage();
		}		
		
		File pdir = new File(picDir);
		if (!pdir.exists()) {
			System.err.println("Can't find Pictures directory at "+pdir);
			usage();
		} 
		File vdir = new File(vidDir);
		if (!vdir.exists()) {
			System.err.println("Can't find Videos directory at "+vdir);;
			usage();
		}
		OrganizePictures op = new OrganizePictures(pdir, vdir, noAction);
		if (removeDup) {
			op.removeDuplicates(pdir);
		}
		op.moveVideos("");
	}
		
	static public void usage() {
		System.err.println("Usage: OrganizePictures [-pictures PicturesDirectory] [-video VideoDirctory] [-noRemoveDup] [-action]");
		System.exit(1);
	}

}
