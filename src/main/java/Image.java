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