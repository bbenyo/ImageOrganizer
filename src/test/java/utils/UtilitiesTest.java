package utils;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import util.MD5Checksum;
import util.struct.FileTypeStats;
import util.struct.FileUtilities;

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
			if (f.getName().startsWith("notajpg") ||
				f.getName().equals("babyvision.bmp")) {
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
		
		Assert.assertTrue(fStats.getFileCount() == 5);
		Assert.assertTrue(fStats.getMinSize() == minFile.length());
		Assert.assertTrue(fStats.getMaxSize() == maxFile.length());
		
		// Try to account for potential differences in file sizes across different systems
		Assert.assertTrue(fStats.getMeanSize() > 24000 && fStats.getMeanSize() < 25000);
		Assert.assertTrue(fStats.getStddevSize() > 35000 && fStats.getStddevSize() < 36000);
		
		// Last mod time can vary depending on os, when downloaded, touching, etc, so lets not test that
		// Not important anyway
	}
}
