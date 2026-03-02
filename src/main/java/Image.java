import swiftbot.*;

public class Inc04_UltrasoundTest {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;

    public static void main(String[] args) {
        new Inc04_UltrasoundTest().run();
    }
