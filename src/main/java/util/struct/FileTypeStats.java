package util.struct;

import java.io.File;
//import java.util.logging.Logger;

public class FileTypeStats {
	//static private Logger logger = Logger.getLogger(FileTypeStats.class.getName());
	
	// No need for full regexs for file types, we'll just have a single wildcard for "all types"
	static public String WILDCARD = "*";
	String fileType;
	File directory;
	boolean includeSubdirs = false;
	int fileCount;
	long maxSize;
	long minSize;
	float meanSize;
	double powSumAvg;
	double stddevSize;
	long earliestTime;
	long latestTime;
	
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

	public boolean isIncludeSubdirs() {
		return includeSubdirs;
	}

	public void setIncludeSubdirs(boolean includeSubdirs) {
		this.includeSubdirs = includeSubdirs;
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

}
