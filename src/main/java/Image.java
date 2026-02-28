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
    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    public static void main(String[] args) {
        new Inc02_QRModeSelect().run();
    }
    
    private void run() {
        setupXButtonStop();
        System.out.println("[INC02] Scan QR for mode (Curious/Scaredy/Dubious). Press X to stop.");
        
        Mode m = scanModeByQr();
        if (!xPressed) {
            System.out.println("[INC02] Mode selected: " + m.text);
        }
        api.disableAllButtons();
        System.out.println("[INC02] Exiting.");
    }
    
    private Mode scanModeByQr() 
    {
    	
    }
    
    
    
    
    
    
}


