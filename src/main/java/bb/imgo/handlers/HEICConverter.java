package bb.imgo.handlers;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import bb.imgo.OrganizeMedia;
import bb.imgo.struct.MediaFile;
import bb.util.StreamGobbler;


/**
 * Convert HEIC to JPG using Windows Powershell script
 *   For Linux/MAC, you'll need another solution, look at tifig
 *   https://github.com/monostream/tifig
 * 
 * You will need to get the powershell script from github separately.  It is not included here.
 *   https://github.com/DavidAnson/ConvertTo-Jpeg
 *   Copy ConvertTo-Jpeg.ps1 to ImageOrganizer\src
 *   
 * For Windows, you'll need to enable local powershell scripts, this can be done via the setting
 *   set-executionpolicy remotesigned
 *   
 * RemoteSigned will allow your system to execute local scripts and signed scripts from the internet
 * To set this option:
 *    Open powershell as administrator
 *    type: "set-executionpolicy remotesigned"
 *    exit powershell
 *    
 * TODO: Look or create a java only converter?
 *    
 * @author Brett
 *
 */
public class HEICConverter extends MediaHandler {
	static private Logger logger = Logger.getLogger(HEICConverter.class.getName());
	static public String psCommand = "powershell.exe src\\ConvertTo-Jpeg.ps1";
	
	boolean ignoreDir = false;
	
	@Override
	public void linkOrganizeMedia(OrganizeMedia om) {
		super.linkOrganizeMedia(om);
		om.addIgnoreSubdirName("HEIC");		
	}
	
	@Override
	public boolean fileFilter(MediaFile f1) {
		if (ignoreDir) {
			return false;
		}
		if (f1.getType().equals("image/heic")) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean directoryInit(File dir) {
		if (dir.getName().equalsIgnoreCase("HEIC")) {
			ignoreDir = true;
		}
		ignoreDir = false;
		return true;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		if (main.moveFiles) {
			String cmd = psCommand + " "+f1.getBaseFile().getAbsolutePath();
			try {
				long lastMod = f1.getOriginalTimestamp();
				if (lastMod == 0) {
					lastMod = f1.getBaseFile().lastModified();
				}
				logger.info("LastMod time for "+f1+" is "+lastMod);
				Process psProc = Runtime.getRuntime().exec(cmd);
				StreamGobbler errorGobbler = new StreamGobbler(psProc.getErrorStream(), "err", logger);
				errorGobbler.start();
				StreamGobbler outGobbler = new StreamGobbler(psProc.getInputStream(), "out", logger);
				outGobbler.start();
				int exitVal = psProc.waitFor();
				logger.info("PowerShell ExitValue: " +exitVal); 
				
				File f2 = new File(f1.getBaseFile().getParentFile(), f1.getBaseFile().getName()+".jpg");
				f2.setLastModified(lastMod);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		main.addConvertActionLog(f1.getBaseFile().getName(), 
				f1.getBaseFile().getName()+".jpg", "HEIC to JPG convert");
            
		// Copy the original HEIC file to an HEIC subdirectory
		File heicDir = new File(f1.getBaseFile().getParentFile(), "HEIC");
		if (!heicDir.exists()) {
			logger.info("Creating HEIC subdirectory");
			heicDir.mkdirs();
		}
            
		File moveTo = new File(heicDir, f1.getBaseFile().getName());
		main.addRenameActionLog(f1.getBaseFile().getAbsolutePath(), moveTo.getAbsolutePath(), "HEIC");
		if (main.moveFiles) {
			try {
				logger.info("Moving original HEIC file to subdir: "+moveTo.getAbsolutePath());
				main.moveFile(f1.getBaseFile(), moveTo);
				f1.setBaseFile(moveTo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	} 
}
