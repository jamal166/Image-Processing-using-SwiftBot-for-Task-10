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

    private void run() {
        setupXButtonStop();
        System.out.println("[INC00] Running. Press X to stop.");

        while (!xPressed) {
            sleep(100);
        }

        System.out.println("[INC00] X pressed. Exiting cleanly.");
        api.disableAllButtons();
    }

    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}


