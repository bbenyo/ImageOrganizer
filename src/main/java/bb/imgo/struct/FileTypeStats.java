package bb.imgo.struct;

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
	long totalSize;
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
		totalSize = 0;
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
	
	public long getTotalSize() {
		return totalSize;
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
			totalSize += size;
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
	
	public String report(String indent) {
		StringBuffer sb = new StringBuffer(toString());
		sb.append(": Count: "+fileCount);
		if (fileCount > 0) {
			sb.append(" Total: "+humanReadableBytes(totalSize));
			sb.append(" Avg: "+humanReadableBytes(meanSize));
			sb.append(" Min: "+humanReadableBytes(minSize));
			sb.append(" Max: "+humanReadableBytes(maxSize));
			double stddev = getStddevSize();
			//String stddevStr = String.format("%.02f", stddev);
			sb.append(" SD: "+humanReadableBytes(stddev)); // stddevStr); 
			sb.append(System.lineSeparator()+indent);
			sb.append(" Time Window: "+sdf.format(earliestTime)+" - "+sdf.format(latestTime));
		}
		return sb.toString();
	}
	
	public String reportCSV() {
		StringBuffer sb = new StringBuffer(toString());
		sb.append(","+fileCount);
		if (fileCount > 0) {
			sb.append(","+totalSize);
			sb.append(","+meanSize);
			sb.append(","+minSize);
			sb.append(","+maxSize);
			double stddev = getStddevSize();
			sb.append(","+stddev); // stddevStr); 
			sb.append(","+sdf.format(earliestTime));
			sb.append(","+sdf.format(latestTime));
		} else {
			sb.append(",0,0,0,0,,,");
		}
		return sb.toString();
	}
	
	public String humanReadableBytes(double bytes) {
		if (Double.isNaN(bytes)) {
			return "-";
		}
		// If less than 1K, report bytes
		if (bytes <= 0) {
			return String.format("%d B", (long)bytes);
		}
		int exponent = (int)(Math.log10(bytes) / 3.0);
		float val = (float)(bytes / Math.pow(1000, exponent));
		String num = "";
		if (val == (long)val)
			num = String.format("%d", (long)val);
		else
			num = String.format("%.3f", val);
		switch (exponent) {
		case 0 : return num+" B"; 
		case 1 : return num+" K";
		case 2 : return num+" M";
		case 3 : return num+" G";
		case 4 : return num+" T";
		case 5 : return num+" P";
		case 6 : return num+" E";
		default :
			return bytes+" (Huge!?!)";
		} 
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
