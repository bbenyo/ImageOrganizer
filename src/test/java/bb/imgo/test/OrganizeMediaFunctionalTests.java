package bb.imgo.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bb.imgo.OrganizeMedia;
import bb.imgo.test.DownloadPicture.PictureParameters;

public class OrganizeMediaFunctionalTests {

	static public String testdir = "data/test/Pictures";
	
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
		OrganizeMedia.main(new String[] {"-p", "data/test/resources/statsonly.properties", "-d", testdir});
	}
		
	public void testFileMove() {
		OrganizeMedia.main(new String[] {"-p", "data/test/resources/testmove.properties", "-d", testdir});
	}
	
}
