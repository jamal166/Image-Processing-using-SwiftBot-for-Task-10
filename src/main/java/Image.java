import swiftbot.*;
 
public class Inc01_UnderlightsTest 
{
	 private static volatile boolean xPressed = false;
	    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

	    public static void main(String[] args) {
	        new Inc01_UnderlightsTest().run();
	    }
	    
	    private void run() {
	        setupXButtonStop();
	        System.out.println("[INC01] Underlights test. Press X to stop.");
	        
	        while (!xPressed) {
	            setRGB(0, 0, 255);  // blue
	            sleep(1000);
	            if (xPressed) break;
	        
	            setRGB(0, 255, 0);  // green
	            sleep(1000);
	            if (xPressed) break;
	    }
	    }

}



