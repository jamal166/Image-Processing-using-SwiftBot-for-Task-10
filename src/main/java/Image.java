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
	        
	        
	        
	    }

}



