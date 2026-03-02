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
    private void run() {
        setupXButtonStop();
        setBlue();
        System.out.println("[INC05] Wandering. If no object within 2m for 5s, pause 1s then turn slightly. X to stop.");

        Instant lastSeen = Instant.now();

        while (!xPressed) {
            // Wander a bit
            api.move(WANDER_L, WANDER_R, 250);

            // Check distance
            double d = readDistanceAvg(2);
            boolean objectWithin2m = (d > 0 && d <= DETECT_WITHIN_CM);

            if (objectWithin2m) {
                lastSeen = Instant.now();
                System.out.printf("[INC05] Object within 2m at %.1f cm%n", d);
            } else {
                if (Duration.between(lastSeen, Instant.now()).compareTo(NO_OBJECT_TIMEOUT) >= 0) {
                    api.stopMove();
                    sleep(1000);
                    slightTurn();
                    lastSeen = Instant.now();
                }
            }
        }
        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
        System.out.println("[INC05] Stopped.");
    }
    private void slightTurn() {
        int turn = Math.random() < 0.5 ? 1 : -1;
        api.move(20 * turn, -20 * turn, 250);
        api.stopMove();
    }
    private double readDistanceAvg(int samples) {
        double sum = 0;
        int ok = 0;
        for (int i = 0; i < samples; i++) {
            try {
                double d = api.useUltrasound();
                if (d > 0 && d < 5000) { sum += d; ok++; }
            } catch (Exception ignored) {}
            sleep(40);
        }
        return ok == 0 ? -1 : sum / ok;
    }
