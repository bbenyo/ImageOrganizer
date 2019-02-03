package bb.imgo.test;

import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import bb.imgo.MD5Checksum;
import bb.imgo.OrganizeMedia;
import bb.imgo.struct.FileTypeStats;
import bb.imgo.struct.FileUtilities;
import bb.imgo.struct.MediaFile;

public class UtilitiesTest {

	@Test
	public void testMD5sum() {
		String f1 = "data/test/resources/001.png";
		String f2 = "data/test/resources/001b.png";
		String f3 = "data/test/resources/babyvision.bmp";
		try {
			String md5_1 = MD5Checksum.getMD5Checksum(f1);
			Assert.assertTrue(md5_1 != null && md5_1.length() > 0);
			System.out.println(md5_1);
			String md5_2 = MD5Checksum.getMD5Checksum(f2);
			Assert.assertTrue(md5_2 != null && md5_2.length() > 0);
			System.out.println(md5_2);
			Assert.assertFalse(md5_1.equals(md5_2));
			
			String md5_1b = MD5Checksum.getMD5Checksum(f1);
			String md5_2b = MD5Checksum.getMD5Checksum(f2);
			Assert.assertTrue(md5_1.equals(md5_1b));
			Assert.assertTrue(md5_2.equals(md5_2b));
			
			String md5_3 = MD5Checksum.getMD5Checksum(f3);
			Assert.assertTrue(md5_3 != null && md5_3.length() > 0);
			System.out.println(md5_3);
			Assert.assertFalse(md5_1.equals(md5_3));
			Assert.assertFalse(md5_2.equals(md5_3));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}		
	}
	
	@Test
	public void testUtils() {
		String f1 = "data/test/resources/001.png";
		String f2 = "data/test/resources/001b.png";
		String f3 = "data/test/resources/babyvision.bmp";
		
		File l1 = new File(f1);
		Assert.assertTrue(l1 != null && l1.exists());
		Assert.assertTrue(FileUtilities.getExtension(l1).equals("png"));
		
		File l2 = new File(f2);
		Assert.assertTrue(l2 != null && l2.exists());
		Assert.assertTrue(FileUtilities.getExtension(l2).equals("png"));
		
		File l3 = new File(f3);
		Assert.assertTrue(l3 != null && l3.exists());
		Assert.assertTrue(FileUtilities.getExtension(l3).equals("bmp"));
	}
	
	@Test
	public void testFileTypeStats() {
		File rDir = new File("data/test/resources");
		Assert.assertTrue(rDir.exists());
		FileTypeStats fStats = new FileTypeStats(rDir, "png");
		
		boolean first = true;
		File[] files = rDir.listFiles();
		for (File f : files) {
			boolean ret = fStats.handleFile(f);
			if (!f.getName().endsWith(".png")) {
				Assert.assertFalse(ret);
			} else {
				Assert.assertTrue(ret);
				System.out.println("Count: "+fStats.getFileCount()+" Mean: "+fStats.getMeanSize()+" SD: "+fStats.getStddevSize());
				if (first) {
					Assert.assertTrue(Double.isNaN(fStats.getStddevSize()));
					Assert.assertTrue(fStats.getMeanSize() == fStats.getMinSize() &&
							          fStats.getMinSize() == fStats.getMaxSize());
					first = false;
				}
			}
		}

		File minFile = new File(rDir, "001.png");
		File maxFile = new File(rDir, "cube.png");
		
		Assert.assertTrue(fStats.getFileCount() == 10);
		Assert.assertTrue(fStats.getMinSize() == minFile.length());
		Assert.assertTrue(fStats.getMaxSize() == maxFile.length());
		
		// Try to account for potential differences in file sizes across different systems
		Assert.assertTrue(fStats.getMeanSize() > 33000 && fStats.getMeanSize() < 35000);
		Assert.assertTrue(fStats.getStddevSize() > 35000 && fStats.getStddevSize() < 40000);
		
		// Last mod time can vary depending on os, when downloaded, touching, etc, so lets not test that
		// Not important anyway
	}
	
	@Test
	public void testHumanReadableBytes() {
		FileTypeStats fstats = new FileTypeStats(null, null);
		Assert.assertTrue(fstats.humanReadableBytes(0).equals("0 B"));
		Assert.assertTrue(fstats.humanReadableBytes(1).equals("1 B"));
		Assert.assertTrue(fstats.humanReadableBytes(999).equals("999 B"));
		Assert.assertTrue(fstats.humanReadableBytes(1000).equals("1 K"));
		Assert.assertTrue(fstats.humanReadableBytes(1001).equals("1.001 K"));
		Assert.assertTrue(fstats.humanReadableBytes(2400).equals("2.400 K"));
		Assert.assertTrue(fstats.humanReadableBytes(10000).equals("10 K"));
		Assert.assertTrue(fstats.humanReadableBytes(100000).equals("100 K"));
		Assert.assertTrue(fstats.humanReadableBytes(999999).equals("999.999 K"));
		Assert.assertTrue(fstats.humanReadableBytes(1000000).equals("1 M"));
		Assert.assertTrue(fstats.humanReadableBytes(1000001).equals("1.000 M"));
		Assert.assertTrue(fstats.humanReadableBytes(1000000000.0).equals("1 G"));
		Assert.assertTrue(fstats.humanReadableBytes(4222000000000.0).equals("4.222 T"));
		Assert.assertTrue(fstats.humanReadableBytes(5100000000000111.0).equals("5.100 P"));
		Assert.assertTrue(fstats.humanReadableBytes(7010000000000111222.0).equals("7.010 E"));
		Assert.assertTrue(fstats.humanReadableBytes(5999999999999999999999.0).equals("6.0E21 (Huge!?!)"));
	}
	
	@Test
	public void testDateParse() {

		SimpleDateFormat tikaDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String date = "2004-06-28T01:26:08";
		try {
			Date d1 = tikaDate.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d1);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1; // Seriously, you're starting Months at 0? Come on...
			int day = cal.get(Calendar.DAY_OF_MONTH);

			System.out.println(year+" "+month+" "+day);
			
			Assert.assertTrue(year == 2004);
			Assert.assertTrue(month == 6);
			Assert.assertTrue(day == 28);
			
		} catch (ParseException ex) {
			ex.printStackTrace();
			Assert.fail();
		} 
	}
	
	private void writeDummyFile(File f1) {
		try {
			BufferedWriter bwrite = new BufferedWriter(new FileWriter(f1));
			bwrite.write("Not A JPG");
			bwrite.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Test
	public void testUniqueFile() {
		String resDir = "data/test/resources";
		OrganizeMedia oMedia = new OrganizeMedia("data/test/resources/statsonly.properties", resDir);
		File f1 = new File(resDir, "IMG_Test2020.jpg");
		if (f1.exists()) {
			f1.delete();
		}
		File f1b = new File(resDir, "IMG_Test2020_V2.jpg");
		if (f1b.exists()) {
			f1b.delete();
		}
		f1b = new File(resDir, "IMG_Test2020_V3.jpg");
		if (f1b.exists()) {
			f1b.delete();
		}
		f1b = new File(resDir, "IMG_Test2020_V4.jpg");
		if (f1b.exists()) {
			f1b.delete();
		}

		writeDummyFile(f1);
		
		Assert.assertTrue(f1.exists());
		File f2 = oMedia.getUniqueFile(f1);
		Assert.assertFalse(f2.exists());
		Assert.assertTrue(f2.getName().equals("IMG_Test2020_V2.jpg"));
		writeDummyFile(f2);
		
		Assert.assertTrue(f2.exists());
		File f3 = oMedia.getUniqueFile(f1);
		Assert.assertFalse(f3.exists());
		Assert.assertTrue(f3.getName().equals("IMG_Test2020_V3.jpg"));
		writeDummyFile(f3);
		
		try {
			oMedia.moveFile(f1, f2);
			File f4 = new File(resDir, "IMG_Test2020_V4.jpg");
			Assert.assertTrue(f4.exists());
			f4.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		f1.delete();
		f2.delete();
		f3.delete();
	}
}
