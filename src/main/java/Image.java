import swiftbot.*;
import java.awt.image.BufferedImage;
 
public class Inc02_QRModeSelect {

    enum Mode {
        CURIOUS("Curious SwiftBot"),
        SCAREDY("Scaredy SwiftBot"),
        DUBIOUS("Dubious SwiftBot");
    	
    	final String text; 
    	Mode(String t) { text = t; }
    	
    	static Mode from(String s) {
    		for (Mode m : values()) if (m.text.equals(s)) 
    			return m; 
    		
    		return null;
    	}
    }
}


