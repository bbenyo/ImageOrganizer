package bb.util;

public class MathUtils {
	
	private MathUtils() {
	}
	
	public static int toInt(double d) {
		return Math.toIntExact(Math.round(d));
	}	
	
	public static int toInt(float d) {
		return Math.toIntExact(Math.round(d));
	}	

}
