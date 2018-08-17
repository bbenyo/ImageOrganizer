package util.struct;

import java.io.File;
import java.text.SimpleDateFormat;

public class FileTypeStats {
	//static private Logger logger = Logger.getLogger(FileTypeStats.class.getName());
	
	// No need for full regexs for file types, we'll just have a single wildcard for "all types"
	static public String WILDCARD = "*";
	
	String fileType;
	File directory;
	int fileCount;
	long maxSize;
	long minSize;
	float meanSize;
	double powSumAvg;
	double stddevSize;
	long earliestTime;
	long latestTime;
	
	protected static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	public FileTypeStats(File dir, String type) {
		this.directory = dir;
		if (type == null) {
			fileType = WILDCARD;
		} else {
			fileType = type;
		}
		resetStats();
	}
	
	protected void resetStats() {
		fileCount = 0;
		maxSize = -1; // if fileCount is 0, we consider these to be uninitialized, so this default value is irrelevant
		minSize = -1;
		meanSize = 0;
		powSumAvg = 0;
		stddevSize = 0;
		earliestTime = -1;
		latestTime = -1;	
	}	
	
	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getFileType() {
		return fileType;
	}

	public int getFileCount() {
		return fileCount;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public long getMinSize() {
		return minSize;
	}

	public float getMeanSize() {
		return meanSize;
	}

	public double getStddevSize() {
		if (fileCount <= 1) {
			return Float.NaN;
		}
		double sd = Math.sqrt(((powSumAvg * fileCount) - (fileCount * meanSize * meanSize)) / (fileCount - 1));
		return sd;
	}

	public long getEarliestTime() {
		return earliestTime;
	}

	public long getLatestTime() {
		return latestTime;
	}

	public boolean handleFile(File f) {
		if (f.isDirectory()) {
			// Only handle files here
			return false;
		}
		if (fileType.equals(WILDCARD) || FileUtilities.getExtension(f).equals(fileType)) {
			long size = f.length();
			long lastMod = f.lastModified();
			if (fileCount == 0) {
				maxSize = size;
				minSize = size;
				earliestTime = lastMod;
				latestTime = lastMod;
			} else {
				if (size > maxSize) {
					maxSize = size;
				} 
				if (size < minSize) {
					minSize = size;
				}
				if (lastMod < earliestTime) {
					earliestTime = lastMod;
				}
				if (lastMod > latestTime) {
					latestTime = lastMod;
				}
			}
			fileCount++;
			meanSize += (size - meanSize) / fileCount;
			// compute running power sum, we'll compute stddev using this on demand
			powSumAvg += ((size * size) - powSumAvg) / fileCount;
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		return directory+","+fileType;
	}
	
	public String report() {
		StringBuffer sb = new StringBuffer(toString());
		sb.append(": Count: "+fileCount);
		if (fileCount > 0) {
			sb.append(" Avg: "+meanSize+" Min: "+minSize+" Max: "+maxSize);
			double stddev = getStddevSize();
			String stddevStr = String.format("%.02f", stddev);
			sb.append(" Stddev: "+stddevStr);
			sb.append(" Time Window: "+sdf.format(earliestTime)+" - "+sdf.format(latestTime));
		}
		return sb.toString();
	}
	
	public String toCSV() {
		StringBuffer sb = new StringBuffer(directory.toString());
		sb.append(","+fileType);
		sb.append(","+fileCount);
		sb.append(","+meanSize);
		sb.append(","+minSize);
		sb.append(","+maxSize);
		sb.append(","+getStddevSize());
		sb.append(","+sdf.format(earliestTime));
		sb.append(","+sdf.format(latestTime));
		return sb.toString();
	}

}
