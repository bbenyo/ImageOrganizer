package bb.imgo.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Download a random picture from Lorem Picsum
 * @author Brett
 *
 */
public class DownloadPicture {

	static public String PicsumURL = "http://picsum.photos";
	
	static class PictureParameters {
		int width;
		int height;
		boolean blur = false;
		boolean grayscale = false;
		int specificIndex = -1; // -1 = random, pos number for specific picture
		
		public PictureParameters(int width, int height) {
			this.width = width;
			this.height = height;
		}
	}
	
	static public long downloadRandomPicture(File toFilename) {
		PictureParameters params = new PictureParameters(300,200);
		return downloadPicture(toFilename, params);
	}
	
	static public long downloadPicture(File toFilename, PictureParameters params) {
		try {
			String urlStr = PicsumURL+"/";
			if (params.grayscale) {
				urlStr = urlStr + "g/";
			}
			urlStr = urlStr + params.width;
			if (params.width != params.height) {
				urlStr = urlStr + "/"+params.height;
			} 
			if (params.specificIndex == -1) {
				urlStr = urlStr + "?random";
			} else {
				urlStr = urlStr + "?image="+params.specificIndex;
			}
			if (params.blur) {
				urlStr = urlStr+"&blur";
			}
			
			System.out.println("Downloading picture from "+urlStr);
			URL picUrl = new URL(urlStr);
			return downloadPicture(picUrl, toFilename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	static public long downloadPicture(URL picUrl, File toFilename) {
		long bytesRead = 0;
		try (InputStream picStr = picUrl.openStream()) {
		    bytesRead = Files.copy(picStr, toFilename.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return bytesRead;
	}
	
	static public int downloadPicturesToDirectory(File dir, int numPics, PictureParameters params) {
		return downloadPicturesToDirectory(dir, numPics, params, 0);
	}
	
	static public int downloadPicturesToDirectory(File dir, int numPics, PictureParameters params, int fileNameIndex) {
		int successful = 0;
		int findex = fileNameIndex;
		for (int i=0; i<numPics; ++i) {
			File picFile = new File(dir, "Pic_"+findex+".jpg");
			findex++;
			long bytes = downloadPicture(picFile, params);
			if (bytes > 0) {
				successful++;
			}
		}
		return successful;
	}
	
	@Test
	@Ignore
	// Just for manual testing, don't run automatically as a unit test
	public void testDownload() {
		try {
			File tempFile = File.createTempFile("pic", "jpg");
			System.out.println("Downloading to "+tempFile.getAbsolutePath());
			long bytes = downloadRandomPicture(tempFile);
			Assert.assertTrue(bytes > 0);
			System.out.println("Bytes read: "+bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
