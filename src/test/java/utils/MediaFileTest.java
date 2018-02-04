package utils;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import util.struct.ImageFile;

public class MediaFileTest {
	
	@Test
	public void testTags() {
		String f1Name = "data/test/resources/001.png";
		File f1 = new File(f1Name);
		
		ImageFile i1 = new ImageFile(f1);
		Assert.assertFalse(i1.isGood());
		Assert.assertFalse(i1.isDelete());
		i1.setGood();
		Assert.assertTrue(i1.isGood());
		Assert.assertFalse(i1.isDelete());
		i1.setDelete();
		Assert.assertTrue(i1.isGood());
		Assert.assertTrue(i1.isDelete());
		i1.clearGood();
		Assert.assertFalse(i1.isGood());
		Assert.assertTrue(i1.isDelete());
		i1.clearDelete();
		Assert.assertFalse(i1.isGood());
		Assert.assertFalse(i1.isDelete());
		
		i1.setTag(0x4);
		Assert.assertTrue(i1.isTag(0x4));
		Assert.assertFalse(i1.isGood());
		Assert.assertFalse(i1.isDelete());
		
		i1.setGood();
		i1.setDelete();
		i1.clearTag(0x4);
		Assert.assertFalse(i1.isTag(0x4));
		Assert.assertTrue(i1.isGood());
		Assert.assertTrue(i1.isDelete());
		
	}										
}

