package bb.imgo.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import bb.imgo.PropertyNames;
import bb.imgo.struct.DirectoryStats;
import bb.imgo.struct.MediaFile;

public class Statistics extends MediaHandler {
	
	static private Logger logger = Logger.getLogger(Statistics.class.getName());
	
	// Store all computed statistics
	protected HashMap<File, DirectoryStats> directoryStats;
	protected String OutputFileName = "imageStatistics.txt";
	protected String OutputFileCSV = "imageStatistics.csv";                         
	protected String OutputFileDir = null; // Use the root of the searched directory
	
	DirectoryStats rootStats; 
		
	public Statistics() {
		super();
		directoryStats = new HashMap<File, DirectoryStats>();
	}
	
	@Override
	public boolean initialize(Properties props) {
		directoryStats.clear();
		String of = props.getProperty(PropertyNames.STATS_OUTPUTFILENAME);
		if (of != null) {
			OutputFileName = of;
		}
		String ofd = props.getProperty(PropertyNames.STATS_OUTPUTFILEDIR);
		if (ofd != null) {
			OutputFileDir = ofd;
		}
		String ofc = props.getProperty(PropertyNames.STATS_OUTPUTFILECSV);
		if (ofc != null) {
			OutputFileCSV = ofc;
		}

		// Empty string (or "null") OutputFile means we don't write to a file
		if (OutputFileName != null) {
			if (OutputFileName.length() == 0 || OutputFileName.equalsIgnoreCase("null")) {
				OutputFileName = null;
			}
		}	
		return true;
	}
	
	public String printConfig(String indent) {
		StringBuffer sb = new StringBuffer();
		String ls = System.lineSeparator();
		sb.append(indent+" Stats OutputFile Name: "+OutputFileName+ls);
		sb.append(indent+" Stats OutputFile Directory: "+OutputFileDir+ls);
		sb.append(indent+" Stats OutputFile CSV: "+OutputFileCSV+ls);
		return sb.toString();
	}
	
	@Override
	public boolean directoryInit(File directory) {
		DirectoryStats dStats = new DirectoryStats(directory);
		if (rootStats == null) {
			rootStats = dStats;
		}
		directoryStats.put(directory, dStats);
		return true;
	}
	
	@Override
	public void subDirectoryInit(File directory, File subdir) {
		// no-op, on complete we'll add if necessary
	}

	@Override
	public void subDirectoryComplete(File directory, File subdir) {
		DirectoryStats subStats = directoryStats.get(subdir);
		DirectoryStats dStats = directoryStats.get(directory);
		if (dStats != null && subStats != null) {
			dStats.addSubdirectory(subStats);
		}
	}
	
	@Override
	public boolean handleFile(MediaFile f1) {
		File f = f1.getBaseFile();
		DirectoryStats cDir = directoryStats.get(f.getParentFile());
		if (cDir == null) {
			logger.warn("Unable to find stats for "+f.getParentFile());
			cDir = new DirectoryStats(f.getParentFile());
			directoryStats.put(f.getParentFile(), cDir);
		} 
		// Statistics doesn't apply any tags to the MediaFile, so we can just get the base File
		cDir.handleFile(f1.getBaseFile());
		return false;
	}

	@Override
	public void directoryComplete(File directory) {
		DirectoryStats cDir = directoryStats.get(directory);
		if (cDir == null) {
			logger.error("Can't find stats for "+directory);
		} else {
			logger.info(cDir.report(""));
		}
	}

	@Override
	public boolean fileFilter(MediaFile f1) {
		return true;
	}
	
	@Override
	public void finalize() {
		if (rootStats != null) {
			String out = rootStats.reportTree();
			if (OutputFileDir == null) {
				OutputFileDir = rootStats.getDirectory().getAbsolutePath();
			}
			if (OutputFileName != null) {
				File f1 = new File(OutputFileDir, OutputFileName);
				logger.info("Writing statistics to "+f1);
				try {
					BufferedWriter bwrite = new BufferedWriter(new FileWriter(f1));
					bwrite.write(out);
					bwrite.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (OutputFileCSV != null) {
				File f1 = new File(OutputFileDir, OutputFileCSV);
				String outCSV = rootStats.reportTreeCSV();
				logger.info("Writing statistics to "+f1);
				try {
					BufferedWriter bwrite = new BufferedWriter(new FileWriter(f1));
					bwrite.write(rootStats.csvHeaders());
					bwrite.write(outCSV);
					bwrite.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			logger.info(out);
		} else {
			logger.error("No root stats found");
		}
	}

	@Override
	public String getDescription() {
		return "Write out file types and sizes to a statistics text and csv file";
	}

	@Override
	public Map<String, String> getConfigurationOptions() {
		HashMap<String, String> configs = new HashMap<String, String>();
		configs.put(PropertyNames.STATS_OUTPUTFILEDIR, OutputFileDir);
		configs.put(PropertyNames.STATS_OUTPUTFILENAME, OutputFileName);
		configs.put(PropertyNames.STATS_OUTPUTFILECSV, OutputFileCSV);
		return configs;
	}

	@Override
	public void setConfigurationOption(String key, String value) {
		if (key.equalsIgnoreCase(PropertyNames.STATS_OUTPUTFILEDIR)) {
			File f1 = new File(value);
			if (!f1.exists()) {
				logger.error("New outputFileDir "+value+" doesn't exist!");
				return;
			}
			OutputFileDir = f1.getAbsolutePath();
		} else if (key.equalsIgnoreCase(PropertyNames.STATS_OUTPUTFILENAME)) {
			OutputFileName = value;
		} else if (key.equalsIgnoreCase(PropertyNames.STATS_OUTPUTFILECSV)) {
			OutputFileCSV = value;
		}		
	}

}
