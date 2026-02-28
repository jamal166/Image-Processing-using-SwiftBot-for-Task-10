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
    	 while (!xPressed) {
             try {
                 BufferedImage qr = api.getQRImage();
                 String txt = api.decodeQRImage(qr);
                 if (txt == null) txt = "";
                 txt = txt.trim();
                 
                 if (txt.isEmpty()) {
                     System.out.println("[INC02] No QR detected. Try again...");
                     continue;
                 }
                 Mode m = Mode.from(txt);
                 if (m != null) return m;

                 System.out.println("[INC02] Invalid QR text: \"" + txt + "\"");
                 System.out.println("        Expect exactly: Curious SwiftBot | Scaredy SwiftBot | Dubious SwiftBot");
             } 
             catch (IllegalArgumentException e) {
                 System.out.println("[INC02] QR decode error: " + e.getMessage());
             } catch (Exception e) {
                 System.out.println("[INC02] Unexpected error: " + e.getMessage());
             }
         }
    	 return Mode.CURIOUS; // not used if X pressed
    }
    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    
}
                		 
                		 
                		 
                		 
                		 
                		 
    }
    	
    	   
    
    
    
    
    
    
}


