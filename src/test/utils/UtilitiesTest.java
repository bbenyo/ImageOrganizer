package test.utils;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import util.MD5Checksum;

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
}
