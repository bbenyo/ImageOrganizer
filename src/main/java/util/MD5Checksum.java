package util;

import java.io.*;
import java.security.MessageDigest;

/**
 * Adapted from http://www.rgagnon.com/javadetails/java-0416.html
 * @author bbenyo
 */

public class MD5Checksum {

   public static byte[] createChecksum(String filename) throws Exception {
       InputStream fis =  new FileInputStream(filename);

       byte[] buffer = new byte[1024];
       MessageDigest complete = MessageDigest.getInstance("MD5");
       int numRead;

       do {
           numRead = fis.read(buffer);
           if (numRead > 0) {
               complete.update(buffer, 0, numRead);
           }
       } while (numRead != -1);

       fis.close();
       return complete.digest();
   }

   static final String HEXES = "0123456789ABCDEF";
   public static String getHex( byte [] raw ) {
	   if ( raw == null ) {
		   return null;
	   }
	   final StringBuilder hex = new StringBuilder( 2 * raw.length );
	   for ( final byte b : raw ) {
		   hex.append(HEXES.charAt((b & 0xF0) >> 4));
		   hex.append(HEXES.charAt((b & 0x0F)));
	   }
	   return hex.toString();
   }
   
   public static String getMD5Checksum(String filename) throws Exception {
       byte[] b = createChecksum(filename);
       return getHex(b);
   }
   
}
