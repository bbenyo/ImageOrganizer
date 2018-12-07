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
	
	@Test
	public void testNewFilePath() {
		String f1Name = "data/test/resources/001.png";
		File f1 = new File(f1Name);
		ImageFile i1 = new ImageFile(f1);
		File baseTest = new File("data/test");
		File baseData = new File("data");
		File baseResources = new File("data/test/resources");
		File baseGrumpy = new File("grumpy");
		
		File g1 = new File("data/Good");
		File g2 = new File("data/test/resources/Good/Maybe");
		File g3 = new File(".");

		File r1 = i1.getNewFilePath(baseResources, g1);
		File e1 = new File("data/Good/001.png");
		System.out.println("Moving "+r1+" to "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));

		r1 = i1.getNewFilePath(baseTest, g1);
		e1 = new File("data/Good/resources/001.png");
		System.out.println("Moving "+r1+" : "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));

		r1 = i1.getNewFilePath(baseData, g1);
		e1 = new File("data/Good/test/resources/001.png");
		System.out.println("Moving "+r1+" : "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));
		
		r1 = i1.getNewFilePath(baseGrumpy, g1);
		e1 = new File("data/Good/data/test/resources/001.png");
		System.out.println("Moving "+r1+" : "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));
		
		r1 = i1.getNewFilePath(baseResources, g2);
		e1 = new File("data/test/resources/Good/Maybe/001.png");
		System.out.println("Moving "+r1+" : "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));

		r1 = i1.getNewFilePath(baseData, g2);
		e1 = new File("data/test/resources/Good/Maybe/test/resources/001.png");
		System.out.println("Moving "+r1+" : "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));
		
		r1 = i1.getNewFilePath(baseGrumpy, g2);
		e1 = new File("data/test/resources/Good/Maybe/data/test/resources/001.png");
		System.out.println("Moving "+r1+" : "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));
		
		r1 = i1.getNewFilePath(baseGrumpy, g3);
		e1 = new File("./data/test/resources/001.png");
		System.out.println("Moving "+r1+" to "+e1);
		Assert.assertTrue(r1.getAbsolutePath().equals(e1.getAbsolutePath()));
	}
}

