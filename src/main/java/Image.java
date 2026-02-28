import swiftbot.*;
 
public class Inc00_SkeletonStop {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    public static void main(String[] args) {
        try {
            new Inc00_SkeletonStop().run();
        } catch (Exception e) {
            System.out.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
        }
    }
   
}



