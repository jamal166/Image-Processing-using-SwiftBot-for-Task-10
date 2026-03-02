import swiftbot.*;
import java.time.Duration;
import java.time.Instant;

public class Inc05_WanderNoObjectTurn_Continuous {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;
    private static final Duration NO_OBJECT_TIMEOUT = Duration.ofSeconds(5);

    private static final int WANDER_L = 22;   // slightly higher to overcome friction
    private static final int WANDER_R = 24;

    public static void main(String[] args) {
        new Inc05_WanderNoObjectTurn_Continuous().run();
    }

    private void run() {
        setupXButtonStop();
        setBlue();

        System.out.println("[INC05.1] Continuous wandering. If no object within 2m for 5s: stop, wait 1s, turn, continue. X to stop.");

        Instant lastSeen = Instant.now();

        // Start moving continuously instead of short bursts
        api.startMove(WANDER_L, WANDER_R);

        while (!xPressed) {
            double d = readDistanceAvg(2);
            boolean objectWithin2m = (d > 0 && d <= DETECT_WITHIN_CM);

            if (objectWithin2m) {
                lastSeen = Instant.now();
            } else {
                if (Duration.between(lastSeen, Instant.now()).compareTo(NO_OBJECT_TIMEOUT) >= 0) {
                    api.stopMove();      // stop first
                    sleep(1000);         // wait 1 second
                    slightTurn();        // change direction a bit
                    lastSeen = Instant.now();

                    // resume wandering continuously
                    api.startMove(WANDER_L, WANDER_R);
                }
            }

            sleep(100); // loop delay so we don’t spam the sensor
        }

        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
        System.out.println("[INC05.1] Stopped.");
    }

    private void slightTurn() {
        int turn = Math.random() < 0.5 ? 1 : -1;
        api.move(25 * turn, -25 * turn, 300); // slightly stronger turn and longer time
        api.stopMove();
    }

    private double readDistanceAvg(int samples) {
        double sum = 0; int ok = 0;
        for (int i = 0; i < samples; i++) {
            try {
                double d = api.useUltrasound();
                if (d > 0 && d < 5000) { sum += d; ok++; }
            } catch (Exception ignored) {}
            sleep(40);
        }
        return ok == 0 ? -1 : sum / ok;
    }

    private void setBlue() {
        api.fillUnderlights(new int[]{0, 0, 255});
    }

    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}