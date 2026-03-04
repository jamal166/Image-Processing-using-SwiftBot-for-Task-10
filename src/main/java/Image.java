import swiftbot.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Inc11_EncounterFrequencyPrompt {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;

    private static final int ENCOUNTER_LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private final List<Instant> encounters = new ArrayList<>();
    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        new Inc11_EncounterFrequencyPrompt().run();
    }
    private void run() {
        setupXButtonStop();
        setBlue();

        System.out.println("[INC11] Encounter frequency prompt demo. Object<=2m counts as encounter. >3 in 5min => prompt. X to stop.");

        boolean triggered = false;

        while (!xPressed) {
            api.move(18, 20, 250);

            double d = readDistanceAvg(2);
            boolean within2m = d > 0 && d <= DETECT_WITHIN_CM;

            if (within2m && !triggered) {
                triggered = true;
                recordEncounter();
                System.out.println("[INC11] Encounter count in window = " + countInWindow());

                if (countInWindow() > ENCOUNTER_LIMIT) {
                    char action = promptCT();
                    if (action == 'T') break;
                    // 'C' in this increment just clears encounters to simulate “mode change”
                    encounters.clear();
                    System.out.println("[INC11] Simulated mode change. Encounter counter cleared.");
                }
            }

            if (!within2m) triggered = false;
        }
        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
        System.out.println("[INC11] Stopped.");
    }
