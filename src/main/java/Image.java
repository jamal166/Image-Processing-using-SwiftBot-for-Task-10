import swiftbot.*;

import java.util.Random;

public class Inc10_DubiousRandomChoice {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;
    private static final int SCAREDY_WITHIN_CM = 50;

    public static void main(String[] args) {
        new Inc10_DubiousRandomChoice().run();
    }