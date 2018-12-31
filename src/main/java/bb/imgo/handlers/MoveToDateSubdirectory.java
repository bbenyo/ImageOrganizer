package bb.imgo.handlers;

import bb.imgo.struct.MediaFile;

/**
 * If a file is NOT in a date subdirectory (named with YYYY-MM-DD), and is instead in a YYYY only directory
 *   move it to the proper YYYY-MM-DD subdirectory based on the file timestamp
 * If files are in YYYY-MM, move them to YYYY-MM-DD  
 *  
 * Do not move files that are not in a YYYY subdir (i.e. wedding or disney, etc)
 * @author Brett
 *
 */
public class MoveToDateSubdirectory extends MediaHandler {

	@Override
	public boolean fileFilter(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleFile(MediaFile f1) {
		// TODO Auto-generated method stub
		return false;
	}

}
