package bb.imgo.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bb.imgo.OrganizeMedia;
import bb.imgo.struct.ActionLog;
import bb.imgo.struct.FileUtilities;
import bb.imgo.struct.MediaFile;
import bb.imgo.test.DownloadPicture.PictureParameters;

public class OrganizeMediaFunctionalTests {

	static public String testdir = "data/test/Pictures";
	static public String resDir = "data/test/resources";
	
	@Before
	public void setUp() {
		File testRootDir = new File(testdir);
		if (testRootDir.exists() && testRootDir.listFiles().length > 0) {
			// Test directory has been generated
			System.out.println("Test directory has been generated at "+testRootDir);
		} else {
			generateTestDirectory();
		}
	}
	
	/** Download random pictures and use them to populate a test directory tree
	 * Directory name (picture count not including subdirectories)
	 * Root (4, 300x200) ->
	 *   A (5, 600x400) ->
	 *     B (2, 400) ->
	 *        C (0)
	 *     D (0) ->
	 *        E (1, 1000)
	 *        F (10, 300x200, 9 blurred same image)
	 *        G (3, 600x400, gray)
	 *   H (2, 100)
	 *   I -> (0)
	 *     J -> (0)
	 *       K -> (3, 300x200, blur, gray)
	 *         L (20, 4 images, 1 normal, 5 blur, 600x480 all)
	 *         
	 *  Eventually some of these will have specific features like blur or different sizes
	 ***/
	public void generateTestDirectory() {
		File testRootDir = new File(testdir);
		if (testRootDir.exists()) {
			System.err.println(testRootDir+" exists, adding new files, won't delete");
		} else {
			testRootDir.mkdirs();
		}
		
		PictureParameters params = new PictureParameters(300, 200);
		int files = DownloadPicture.downloadPicturesToDirectory(testRootDir, 4, params);
		System.out.println("Downloaded "+files+" to "+testRootDir);
		
		// Make subtree structure
		File aDir = new File(testRootDir, "A");
		aDir.mkdir();
		params = new PictureParameters(600, 400);
		files = DownloadPicture.downloadPicturesToDirectory(aDir, 5, params);
		System.out.println("Downloaded "+files+" to "+aDir);
		
		File bDir = new File(aDir, "B");
		bDir.mkdir();
		params = new PictureParameters(400, 400);
		files = DownloadPicture.downloadPicturesToDirectory(bDir, 2, params);
		System.out.println("Downloaded "+files+" to "+bDir);
		
		File cDir = new File(bDir, "C");
		cDir.mkdir();
		File dDir = new File(aDir, "D");
		dDir.mkdir();
		File eDir = new File(dDir, "E");
		eDir.mkdir();
		params = new PictureParameters(1000, 1000);
		files = DownloadPicture.downloadPicturesToDirectory(eDir, 1, params);
		System.out.println("Downloaded "+files+" to "+eDir);
		
		File fDir = new File(dDir, "F");
		fDir.mkdir();
		params = new PictureParameters(300, 200);
		params.specificIndex = 10;
		files = DownloadPicture.downloadPicturesToDirectory(fDir, 1, params);
		System.out.println("Downloaded "+files+" to "+fDir);
		params.blur = true;
		files = DownloadPicture.downloadPicturesToDirectory(fDir, 9, params, 1);
		System.out.println("Downloaded "+files+" more bluurred to "+fDir);
		
		File gDir = new File(dDir, "G");
		gDir.mkdir();
		params = new PictureParameters(600, 400);
		params.grayscale = true;
		files = DownloadPicture.downloadPicturesToDirectory(gDir, 3, params);
		System.out.println("Downloaded "+files+" to "+gDir);
		
		File hDir = new File(testRootDir, "H");
		hDir.mkdir();
		params = new PictureParameters(100, 100);
		files = DownloadPicture.downloadPicturesToDirectory(hDir, 1, params);
		System.out.println("Downloaded "+files+" to "+hDir);
		
		File iDir = new File(testRootDir, "I");
		iDir.mkdir();
		File jDir = new File(iDir, "J");
		jDir.mkdir();
		File kDir = new File(jDir, "K");
		kDir.mkdir();
		params = new PictureParameters(300, 200);
		params.blur = true;
		params.grayscale = true;
		files = DownloadPicture.downloadPicturesToDirectory(kDir, 1, params);
		System.out.println("Downloaded "+files+" to "+kDir);
		
		File lDir = new File(kDir, "L");
		lDir.mkdir();
		params = new PictureParameters(600, 480);
		for (int i=0; i<4; ++i) {
			params.specificIndex = 20 + (10 * i);
			params.blur = false;
			files = DownloadPicture.downloadPicturesToDirectory(lDir, 1, params, 0+(5*i));
			System.out.println("Downloaded "+files+" to "+lDir);
			params.blur = true;
			files = DownloadPicture.downloadPicturesToDirectory(lDir, 4, params, 1+(5*i));
			System.out.println("Downloaded "+files+" more blurred to "+lDir);
		}
	}
	
	@Test
	public void verifyTestDirectory() {
		File testRootDir = new File(testdir);
		Assert.assertTrue(testRootDir.exists());
		File[] files = testRootDir.listFiles();
		int count = 0;
		for (File f : files) {
			if (f.getName().endsWith(".jpg")) {
				count++;
			}
		}
		Assert.assertTrue(count >= 4);
		File aDir = new File(testRootDir, "A");
		Assert.assertTrue(aDir.exists());
	}
	
	@Test
	public void testStatistics() {
		OrganizeMedia oMedia = new OrganizeMedia("data/test/resources/statsonly.properties", testdir);
		int count = oMedia.countFiles(oMedia.getRootDirectory(), null);
		System.out.println("Counted files: "+count);
		Assert.assertTrue(count >= 47 && count <= 49); // Depends on if we've run statistics yet or not
		oMedia.organize();
		
		// todo assertions
	}
		
	@Test
	public void testFileMove() {
		OrganizeMedia oMedia = new OrganizeMedia("data/test/resources/testmove.properties", testdir);
		// Cleanup first
		File goodDir = oMedia.getGoodDir();
		File trashDir = oMedia.getTrashDir();
		
		Assert.assertTrue(goodDir.getAbsolutePath().indexOf("unittest") > -1);
		FileUtilities.deleteDirectoryContents(goodDir);
		FileUtilities.deleteDirectoryContents(trashDir);
		Assert.assertTrue(goodDir.listFiles().length == 0);
		Assert.assertTrue(trashDir.listFiles().length == 0);
				
		boolean failMe = false;
		try {

			oMedia.organize();
			
			// Expect Pic_0's to be good, Pic_1's to be deleted
			File testFile = new File(goodDir, "Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "A/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "A/B/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "A/D/E/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "A/D/F/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "A/D/G/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "H/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "I/J/K/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(goodDir, "I/J/K/L/Pic_0.jpg");
			Assert.assertTrue(testFile.exists());
			checkFileExists(goodDir, "Pic_0.jpg", true);
			checkFileExists(oMedia.getRootDirectory(), "Pic_0.jpg", false);
			
			testFile = new File(trashDir, "Pic_1.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(trashDir, "A/Pic_1.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(trashDir, "A/B/Pic_1.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(trashDir, "A/D/E/Pic_1.jpg");
			Assert.assertFalse(testFile.exists());
			
			testFile = new File(trashDir, "A/D/F/Pic_1.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(trashDir, "A/D/G/Pic_1.jpg");
			Assert.assertTrue(testFile.exists());
			testFile = new File(trashDir, "H/Pic_1.jpg");
			Assert.assertFalse(testFile.exists());
			
			testFile = new File(trashDir, "I/J/K/Pic_1.jpg");
			Assert.assertFalse(testFile.exists());
			testFile = new File(trashDir, "I/J/K/L/Pic_1.jpg");
			Assert.assertTrue(testFile.exists());
			
			checkFileExists(trashDir, "Pic_1.jpg", true);
			checkFileExists(oMedia.getRootDirectory(), "Pic_1.jpg", false);

		} catch (AssertionError ae) {
			ae.printStackTrace();
			failMe = true;
		} finally {
			// Clean up, copy goodDir files back
			System.out.println("Moving files back from "+goodDir+" to "+oMedia.getRootDirectory());
			moveFilesBack(oMedia, goodDir, oMedia.getRootDirectory());
			System.out.println("Moving files back from "+trashDir+" to "+oMedia.getRootDirectory());
			moveFilesBack(oMedia, trashDir, oMedia.getRootDirectory());
		}
		
		File rootDir = oMedia.getRootDirectory();
		File testFile = new File(rootDir, "Pic_0.jpg");
		Assert.assertTrue(testFile.exists());
		testFile = new File(rootDir, "Pic_1.jpg");
		Assert.assertTrue(testFile.exists());
		testFile = new File(rootDir, "I/J/K/L/Pic_0.jpg");
		Assert.assertTrue(testFile.exists());
		testFile = new File(rootDir, "I/J/K/L/Pic_1.jpg");
		Assert.assertTrue(testFile.exists());
		
		if (failMe) {
			Assert.fail();
		}
	}
	
	// Check that all files in dir are either named 'fname' (wantIt = true) or not 'fname' (wantIt = false).
	private void checkFileExists(File dir, String fname, boolean wantIt) {
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				checkFileExists(f, fname, wantIt);
			} else {
				if (wantIt) {
					Assert.assertTrue(f.getName().equals(fname));
				} else {
					Assert.assertFalse(f.getName().equals(fname));
				}
			}
		}
	}
	
	private void moveFilesBack(OrganizeMedia oMedia, File dir, File root) {
		moveFilesBack1(oMedia, dir, dir, root);
	}
	
	private void moveFilesBack1(OrganizeMedia oMedia, File dir, File currentDir, File root) {
		File[] files = currentDir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				moveFilesBack1(oMedia, dir, f, root);
			} else {
				MediaFile mFile = new MediaFile(f);
				File p2 = mFile.getNewFilePath(dir, root);
				try {
					oMedia.moveFile(f, p2);
				} catch (IOException e) {
					e.printStackTrace();
					Assert.fail();
				}
			}
		}
	}
	

	@Test
	public void testRemoveDuplicates() {
		OrganizeMedia org = new OrganizeMedia("data/test/resources/testdup.properties", resDir);
		org.organize();
		ArrayList<ActionLog> alog = org.getActionLog();
		Assert.assertTrue(alog.size() == 7);
	}
	
	@Test
	public void testRemoveEmptySubdirectories() {
		OrganizeMedia org = new OrganizeMedia("data/test/resources/testrmempty.properties", resDir);
		File makeEmpty = new File("data/test/resources/testEmpty1");
		File makeEmpty2 = new File("data/test/resources/testEmpty1/testEmpty2");
		File makeEmpty3 = new File("data/test/resources/testEmpty3");
		File makeNonEmpty4 = new File("data/test/resources/testNotEmpty4");
		makeEmpty.mkdirs();
		makeEmpty2.mkdirs();
		makeEmpty3.mkdirs();
		makeNonEmpty4.mkdirs();
		File f1 = new File(makeNonEmpty4, "test.txt");
		try {
			f1.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertTrue(f1.exists());
		
		org.organize();
		ArrayList<ActionLog> alog = org.getActionLog();
		Assert.assertTrue(alog.size() == 3);  
		// Expect to delete testEmpty1, 2, 3
				
		Assert.assertFalse(makeEmpty.exists());
		Assert.assertFalse(makeEmpty2.exists());
		Assert.assertFalse(makeEmpty3.exists());
		Assert.assertTrue(makeNonEmpty4.exists());
	}
}
