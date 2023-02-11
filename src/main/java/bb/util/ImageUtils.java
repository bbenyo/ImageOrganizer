package bb.util;

import java.awt.Dimension;

public class ImageUtils {
	
	private ImageUtils() {
	}

	public static Dimension resizeImage(int origWidth, int origHeight, int maxWidth, int maxHeight) {
		if (origWidth <= maxWidth && origHeight <= maxHeight) {
			return new Dimension(origWidth, origHeight);
		}
		
	    int width = origWidth;
	    int height = origHeight;
	    
	    double aspect = (double)width / height;
	    
	    if (maxWidth < origWidth) { // too large width wise
	    	width = maxWidth;
	    	height = MathUtils.toInt(width / aspect);
	    }

	    if (maxHeight < height) {
	    	height = maxHeight;
	    	width = MathUtils.toInt(height * aspect);
	    }

	    return new Dimension(width, height);
	}
}
