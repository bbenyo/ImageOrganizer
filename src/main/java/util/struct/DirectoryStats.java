package util.struct;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * File type statistics for all files in a directory
 * Contains aggregate statistics, then a breakdown per file type
 * @author Brett
 *
 */
public class DirectoryStats extends FileTypeStats {

	FileTypeStats statsWithSubs;
	HashMap<String, FileTypeStats> typeStatistics;
	ArrayList<DirectoryStats> subdirectories;
	
	public DirectoryStats(File dir) {
		super(dir, FileTypeStats.WILDCARD);
		typeStatistics = new HashMap<String, FileTypeStats>();
		subdirectories = new ArrayList<DirectoryStats>();
	}
	
	@Override
	public boolean handleFile(File f) {
		String ext = FileUtilities.getExtension(f);
		FileTypeStats eStats = typeStatistics.get(ext);
		if (eStats == null) {
			eStats = new FileTypeStats(getDirectory(), ext);
			typeStatistics.put(ext, eStats);
		}
		// For the wildcard all files stats
		super.handleFile(f);
		return eStats.handleFile(f);
	}
	
	public String reportLocal() {
		return reportLocal("");
	}
	
	public String reportLocal(String indent) {
		StringBuffer sb = new StringBuffer(toString());
		String lineSep = System.lineSeparator();
		Set<String> types = typeStatistics.keySet();
		if (types.isEmpty()) {
			sb.append(" (Empty)");
			return sb.toString();
		}
		List<String> sTypes = new ArrayList<String>(types);
		Collections.sort(sTypes);
		for (String type : sTypes) {
			FileTypeStats fStats = typeStatistics.get(type);
			if (fStats != null) {
				sb.append(lineSep+indent);
				sb.append(fStats.report());
			}
		}
		return sb.toString();		
	}
	
	@Override
	public String report() {
		return reportLocal();
	}
	
	public String reportTree() {
		return reportTree("");
	}
	
	public String reportTree(String indent) {
		StringBuffer sb = new StringBuffer(reportLocal(indent));
		String lineSep = System.lineSeparator();
		for (DirectoryStats sub : subdirectories) {
			sb.append(lineSep+indent+sub.reportTree(indent+"  "));
		}
		return sb.toString();
	}
	
	public void addSubdirectory(DirectoryStats subStats) {
		subdirectories.add(subStats);
	}
	
	public void combine() {
		
	}
	
}
