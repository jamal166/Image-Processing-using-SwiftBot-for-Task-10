import swiftbot.*;

import java.time.Duration;
import java.time.Instant;
public class Inc05_WanderNoObjectTurn {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;

    private static final Duration NO_OBJECT_TIMEOUT = Duration.ofSeconds(5);

    private static final int WANDER_L = 18;
    private static final int WANDER_R = 20;

    public static void main(String[] args) {
        new Inc05_WanderNoObjectTurn().run();
    }