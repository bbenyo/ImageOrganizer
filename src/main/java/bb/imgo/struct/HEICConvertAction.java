package bb.imgo.struct;

import bb.imgo.OrganizeMedia;
import bb.imgo.handlers.HEICConverter;

public class HEICConvertAction extends ActionLog {
	
	HEICConverter converter;
	MediaFile mFile;

	public HEICConvertAction(String filename, String reason, HEICConverter converter, MediaFile mFile) {
		super(filename, ActionLog.Action.CONVERT, reason);
		this.converter = converter;
		this.mFile = mFile;
	}
	
	@Override
	public void executeAction(OrganizeMedia oMedia) {
		converter.execute(mFile);
	}
	
}
