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
	            
	            setRGB(255, 0, 0);  // red
	            sleep(1000);
	            if (xPressed) break;
	            
	            api.disableUnderlights(); // off
	            sleep(1000);
	    }
	        api.disableUnderlights();
	        api.disableAllButtons();
	        System.out.println("[INC01] Stopped."); 
	        
	    }
	    private void setRGB(int r, int g, int b) {
	        api.fillUnderlights(new int[]{r, g, b});
	    }
	    private void setupXButtonStop() {
	        api.disableAllButtons();
	        api.enableButton(Button.X, () -> xPressed = true);
	    }
	    private static void sleep(int ms) {
	        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
	    }

}



