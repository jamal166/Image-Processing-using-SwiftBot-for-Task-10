import swiftbot.*;

public class Inc04_UltrasoundTest {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;

    public static void main(String[] args) {
        new Inc04_UltrasoundTest().run();
    }
    
    
    private void run() {
        setupXButtonStop();
        System.out.println("[INC04] Ultrasound test. Press X to stop.");

        while (!xPressed) {
            double d = readDistanceAvg(3);
            boolean within2m = (d > 0 && d <= DETECT_WITHIN_CM);

            System.out.printf("[INC04] Distance = %.1f cm | within2m=%s%n", d, within2m);
            sleep(500);
        }

