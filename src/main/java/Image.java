import swiftbot.*;

public class Inc08_CuriousOnlyBuffer {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int BUFFER_CM = 30;
    private static final int TOLERANCE_CM = 2;

    private static final int DETECT_WITHIN_CM = 200;

    private static final int WANDER_L = 18;
    private static final int WANDER_R = 20;

    public static void main(String[] args) {
        new Inc08_CuriousOnlyBuffer().run();
    }