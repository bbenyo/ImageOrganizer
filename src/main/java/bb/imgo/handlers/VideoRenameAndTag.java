package bb.imgo.handlers;

import bb.imgo.struct.MediaFile;

/** 
 * Prompt the user to rename a video that is named ###.avi (no letters)
 * Pop up a UI that lets the user play the video, rename, and tag (good/trash/archive)
 * 
 * Auto name short videos as livephotoX, and auto-archive
 * 
 * @author Brett
 *
 */
public class VideoRenameAndTag extends MediaHandler {

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
