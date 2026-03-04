import swiftbot.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Inc12_FinalTask10 {

    enum Mode {
        CURIOUS("Curious SwiftBot"),
        SCAREDY("Scaredy SwiftBot"),
        DUBIOUS("Dubious SwiftBot");

        final String qrText;
        Mode(String qrText) { this.qrText = qrText; }

        static Optional<Mode> fromQr(String s) {
            for (Mode m : values()) if (m.qrText.equals(s)) return Optional.of(m);
            return Optional.empty();
        }
    }

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;
    private final Scanner console = new Scanner(System.in);

    private static final Path IMAGE_DIR = Paths.get("/data/home/pi/task10/images");
    private static final Path DEBUG_DIR = Paths.get("/data/home/pi/task10/images_debug");
    private static final Path LOG_DIR   = Paths.get("/data/home/pi/task10/logs");

    private static final ImageSize CAPTURE_SIZE = ImageSize.SQUARE_480x480;

    private static final double ROI_MIN = 0.35, ROI_MAX = 0.65;
    private static final int ROI_STEP = 4;

    private static final double ROI_DIFF_THRESHOLD = 25.0;
    private static final int OCCUPANCY_DELTA_THRESHOLD = 150;

    private static final int DEFAULT_BUFFER_CM = 30;
    private static final int[] ALLOWED_BUFFERS = {20, 30, 40};
    private static final int TOLERANCE_CM = 2;

    private static final int DETECT_WITHIN_CM = 200; // 2m
    private static final int SCAREDY_WITHIN_CM = 50;

    private static final int ENCOUNTER_LIMIT = 3;
    private static final Duration ENCOUNTER_WINDOW = Duration.ofMinutes(5);

    private static final Duration OBJECT_CHECK_INTERVAL = Duration.ofSeconds(5);
    private static final Duration WANDER_NO_OBJECT_TIMEOUT = Duration.ofSeconds(5);

    // ======= SPEED MODIFICATIONS (FASTER) =======
    private static final int WANDER_L = 40;   // was 18
    private static final int WANDER_R = 44;   // was 20
    private static final int WANDER_STEP_MS = 700; // was 250

    private static final int TURN_SPEED = 35;      // was 20
    private static final int TURN_MS = 400;        // was 250

    private static final int BUFFER_ADJUST_SPEED = 35;  // was 25
    private static final int BUFFER_STEP_MS = 120;      // keep same (stable control)

    private static final int CURIOUS_CORRECT_SPEED = 35; // was 25
    private static final int CURIOUS_CORRECT_MS = 300;   // was 250

    // Optional faster Scaredy (enabled here)
    private static final int SCAREDY_BACK_SPEED = 45; // was 35
    private static final int SCAREDY_BACK_MS = 700;   // was 600
    private static final int SCAREDY_TURN_SPEED = 55; // was 40
    private static final int SCAREDY_TURN_MS = 500;   // was 450
    private static final int SCAREDY_AWAY_SPEED = 45; // was 35
    private static final int SCAREDY_AWAY_MS = 3000;  // keep 3 seconds
    // ===========================================

    private Mode selectedMode;
    private int curiousBufferCm = DEFAULT_BUFFER_CM;

    private final List<Path> savedImages = new ArrayList<>();
    private final List<Instant> encounterTimes = new ArrayList<>();

    private final Instant startTime = Instant.now();

    public static void main(String[] args) {
        try { new Inc12_FinalTask10().run(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void run() throws Exception {
        ensureDirs();
        setupXButtonStop();
        printWelcome();

        selectedMode = scanModeByQr();
        System.out.println("[FINAL] Mode selected: " + selectedMode.qrText);

        if (selectedMode == Mode.CURIOUS || selectedMode == Mode.DUBIOUS) {
            curiousBufferCm = promptBufferDistance();
            System.out.println("[FINAL] Curious buffer set to: " + curiousBufferCm + " cm");
        }

        mainLoop();

        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();

        Path logPath = writeLog();
        System.out.println("[FINAL] Log saved to: " + logPath);
        System.out.println("[FINAL] Images saved under: " + IMAGE_DIR);
    }

    private void mainLoop() throws Exception {
        setBlue();
        BufferedImage prev = safeCapture();
        Instant lastObjectSeen = Instant.now();

        while (!xPressed) {
            wanderStep();

            BufferedImage now = safeCapture();
            double diff = roiDiffScore(prev, now);

            double d = readDistanceAvg(3);
            boolean within2m = d > 0 && d <= DETECT_WITHIN_CM;
            boolean visionSuggests = diff >= ROI_DIFF_THRESHOLD;

            if (within2m || visionSuggests) {
                lastObjectSeen = Instant.now();
                recordEncounter();

                Mode effective = selectedMode;
                if (selectedMode == Mode.DUBIOUS) {
                    effective = new Random().nextBoolean() ? Mode.CURIOUS : Mode.SCAREDY;
                    System.out.println("[FINAL] Dubious chose: " + effective.qrText);
                }

                if (effective == Mode.CURIOUS) curiousResponse(d);
                else scaredyResponse(d);

                if (encountersInWindow() > ENCOUNTER_LIMIT) {
                    char action = promptChangeModeOrTerminate();
                    if (action == 'T') return;

                    selectedMode = scanModeByQr();
                    System.out.println("[FINAL] Mode changed to: " + selectedMode.qrText);

                    if (selectedMode == Mode.CURIOUS || selectedMode == Mode.DUBIOUS) {
                        curiousBufferCm = promptBufferDistance();
                        System.out.println("[FINAL] Curious buffer set to: " + curiousBufferCm + " cm");
                    }
                }

            } else {
                if (Duration.between(lastObjectSeen, Instant.now()).compareTo(WANDER_NO_OBJECT_TIMEOUT) >= 0) {
                    api.stopMove();
                    sleep(1000);
                    slightTurn();
                    lastObjectSeen = Instant.now();
                }
            }

            prev = now;
        }

        System.out.println("[FINAL] X pressed. Stopping.");
    }

    private void curiousResponse(double distanceCm) throws Exception {
        setGreen();
        api.stopMove();

        // buffer control with ultrasound
        if (distanceCm > 0) {
            reachBuffer();
        } else {
            blink(0,255,0);
        }

        Path imgPath = saveEncounterImage("curious");
        saveDebugThresholdImage(imgPath);

        BufferedImage ref = ImageIO.read(imgPath.toFile());
        sleep((int) OBJECT_CHECK_INTERVAL.toMillis());
        BufferedImage later = safeCapture();

        int delta = roiWhiteOccupancy(later) - roiWhiteOccupancy(ref);

        if (Math.abs(delta) < OCCUPANCY_DELTA_THRESHOLD) {
            api.stopMove();
            sleep(1000);
            slightTurn();
        } else if (delta > 0) {
            // appears closer -> move back slightly (FASTER)
            api.move(-CURIOUS_CORRECT_SPEED, -CURIOUS_CORRECT_SPEED, CURIOUS_CORRECT_MS);
            api.stopMove();
        } else {
            // appears farther -> move forward slightly (FASTER)
            api.move(CURIOUS_CORRECT_SPEED, CURIOUS_CORRECT_SPEED, CURIOUS_CORRECT_MS);
            api.stopMove();
        }

        api.disableUnderlights();
        setBlue();
    }

    private void reachBuffer() {
        while (!xPressed) {
            double d = readDistanceAvg(2);
            if (d <= 0) break;

            if (d > curiousBufferCm + TOLERANCE_CM) {
                api.startMove(BUFFER_ADJUST_SPEED, BUFFER_ADJUST_SPEED);  // faster
                sleep(BUFFER_STEP_MS);
            } else if (d < curiousBufferCm - TOLERANCE_CM) {
                api.startMove(-BUFFER_ADJUST_SPEED, -BUFFER_ADJUST_SPEED); // faster
                sleep(BUFFER_STEP_MS);
            } else {
                api.stopMove();
                blink(0,255,0);
                break;
            }
        }
        api.stopMove();
    }

    private void scaredyResponse(double distanceCm) throws Exception {
        if (distanceCm > 0 && distanceCm <= SCAREDY_WITHIN_CM) {
            Path imgPath = saveEncounterImage("scaredy");
            System.out.println("[FINAL] Scaredy image: " + imgPath);

            blink(255,0,0);
            setRed();

            // Faster flee sequence
            api.move(-SCAREDY_BACK_SPEED, -SCAREDY_BACK_SPEED, SCAREDY_BACK_MS);
            api.move(SCAREDY_TURN_SPEED, -SCAREDY_TURN_SPEED, SCAREDY_TURN_MS);
            api.move(SCAREDY_AWAY_SPEED, SCAREDY_AWAY_SPEED, SCAREDY_AWAY_MS);

            api.stopMove();
            setBlue();
        }
    }

    private void wanderStep() {
        setBlue();
        api.move(WANDER_L, WANDER_R, WANDER_STEP_MS); // faster + longer step
        if (Math.random() < 0.30) slightTurn();
    }

    private void slightTurn() {
        int t = Math.random() < 0.5 ? 1 : -1;
        api.move(TURN_SPEED * t, -TURN_SPEED * t, TURN_MS); // stronger + longer turn
        api.stopMove();
    }

    private BufferedImage safeCapture() throws Exception {
        return api.takeStill(CAPTURE_SIZE);
    }

    private Path saveEncounterImage(String prefix) throws Exception {
        BufferedImage img = safeCapture();
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String filename = prefix + "_" + ts + "_" + UUID.randomUUID().toString().substring(0,8) + ".jpg";
        Path out = IMAGE_DIR.resolve(filename);
        ImageIO.write(img, "jpg", out.toFile());
        savedImages.add(out);
        System.out.println("[FINAL] Saved: " + out);
        return out;
    }

    private Path saveDebugThresholdImage(Path originalJpg) throws Exception {
        BufferedImage img = ImageIO.read(originalJpg.toFile());
        BufferedImage bw = thresholdToBW(img);

        String dbgName = originalJpg.getFileName().toString().replace(".jpg", "_bw.jpg");
        Path out = DEBUG_DIR.resolve(dbgName);

        ImageIO.write(bw, "jpg", out.toFile());
        return out;
    }

    // ==== QR mode scan ====
    private Mode scanModeByQr() {
        System.out.println("[FINAL] Scan QR for mode...");
        while (!xPressed) {
            try {
                BufferedImage qr = api.getQRImage();
                String txt = api.decodeQRImage(qr);
                if (txt == null) txt = "";
                txt = txt.trim();

                if (txt.isEmpty()) {
                    System.out.println("[FINAL] No QR detected.");
                    continue;
                }

                Optional<Mode> m = Mode.fromQr(txt);
                if (m.isPresent()) return m.get();

                System.out.println("[FINAL] Invalid QR text: \"" + txt + "\"");
            } catch (IllegalArgumentException e) {
                System.out.println("[FINAL] QR decode error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[FINAL] QR unexpected error: " + e.getMessage());
            }
        }
        return Mode.CURIOUS;
    }

    // ==== prompt buffer (extra feature) ====
    private int promptBufferDistance() {
        System.out.println("[FINAL] Configure Curious buffer? Y/N (default 30)");
        while (true) {
            System.out.print("> ");
            String s = console.nextLine().trim().toUpperCase(Locale.ROOT);
            if (s.equals("N")) return DEFAULT_BUFFER_CM;
            if (s.equals("Y")) break;
        }
        while (true) {
            System.out.print("[FINAL] Enter buffer 20/30/40: ");
            String s = console.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                for (int a : ALLOWED_BUFFERS) if (v == a) return v;
            } catch (NumberFormatException ignored) {}
        }
    }

    private char promptChangeModeOrTerminate() {
        while (true) {
            System.out.print("[FINAL] >3 encounters/5min. C=change mode, T=terminate: ");
            String s = console.nextLine().trim().toUpperCase(Locale.ROOT);
            if (s.equals("C")) return 'C';
            if (s.equals("T")) return 'T';
        }
    }

    // ==== encounter tracking ====
    private void recordEncounter() {
        encounterTimes.add(Instant.now());
        pruneEncounters();
        System.out.println("[FINAL] Encounters in 5min window: " + encounterTimes.size());
    }

    private void pruneEncounters() {
        Instant cutoff = Instant.now().minus(ENCOUNTER_WINDOW);
        encounterTimes.removeIf(t -> t.isBefore(cutoff));
    }

    private int encountersInWindow() {
        pruneEncounters();
        return encounterTimes.size();
    }

    // ==== ultrasound ====
    private double readDistanceAvg(int samples) {
        double sum = 0; int ok = 0;
        for (int i=0;i<samples;i++){
            try {
                double d = api.useUltrasound();
                if (d>0 && d<5000){ sum+=d; ok++; }
            } catch (Exception ignored) {}
            sleep(40);
        }
        return ok==0 ? -1 : sum/ok;
    }

    // ==== underlights ====
    private void setBlue(){ api.fillUnderlights(new int[]{0,0,255}); }
    private void setGreen(){ api.fillUnderlights(new int[]{0,255,0}); }
    private void setRed(){ api.fillUnderlights(new int[]{255,0,0}); }

    private void blink(int r,int g,int b){
        api.stopMove();
        for(int i=0;i<3 && !xPressed;i++){
            api.fillUnderlights(new int[]{r,g,b});
            sleep(200);
            api.disableUnderlights();
            sleep(200);
        }
        api.fillUnderlights(new int[]{r,g,b});
    }

    // ==== button ====
    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    }

    // ==== image processing ====
    private static int r(int p){ return (p>>16)&0xFF; }
    private static int g(int p){ return (p>>8)&0xFF; }
    private static int b(int p){ return p&0xFF; }

    private static double pixelDist(int r1,int g1,int b1,int r2,int g2,int b2){
        int dr=r1-r2, dg=g1-g2, db=b1-b2;
        return Math.sqrt(dr*dr + dg*dg + db*db);
    }

    private static double roiDiffScore(BufferedImage a, BufferedImage c) {
        int w = Math.min(a.getWidth(), c.getWidth());
        int h = Math.min(a.getHeight(), c.getHeight());
        int x0 = (int)Math.round(w*ROI_MIN), x1=(int)Math.round(w*ROI_MAX);
        int y0 = (int)Math.round(h*ROI_MIN), y1=(int)Math.round(h*ROI_MAX);

        double sum=0; int n=0;
        for(int y=y0;y<y1;y+=ROI_STEP){
            for(int x=x0;x<x1;x+=ROI_STEP){
                int pa=a.getRGB(x,y), pc=c.getRGB(x,y);
                sum += pixelDist(r(pa),g(pa),b(pa), r(pc),g(pc),b(pc));
                n++;
            }
        }
        return n==0 ? 0 : sum/n;
    }

    private static BufferedImage thresholdToBW(BufferedImage img){
        int w=img.getWidth(), h=img.getHeight();
        long sum=0, n=0;
        for(int y=0;y<h;y+=2){
            for(int x=0;x<w;x+=2){
                int p=img.getRGB(x,y);
                int br=(r(p)+g(p)+b(p))/3;
                sum+=br; n++;
            }
        }
        int avg = n==0 ? 128 : (int)(sum/n);

        BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        int white=(255<<16)|(255<<8)|255, black=0;
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                int p=img.getRGB(x,y);
                int br=(r(p)+g(p)+b(p))/3;
                out.setRGB(x,y, br>=avg ? white : black);
            }
        }
        return out;
    }

    private static int roiWhiteOccupancy(BufferedImage img){
        BufferedImage bw = thresholdToBW(img);
        int w=bw.getWidth(), h=bw.getHeight();
        int x0=(int)Math.round(w*ROI_MIN), x1=(int)Math.round(w*ROI_MAX);
        int y0=(int)Math.round(h*ROI_MIN), y1=(int)Math.round(h*ROI_MAX);

        int white=(255<<16)|(255<<8)|255;
        int count=0;
        for(int y=y0;y<y1;y+=2){
            for(int x=x0;x<x1;x+=2){
                int p=bw.getRGB(x,y)&0xFFFFFF;
                if(p==(white&0xFFFFFF)) count++;
            }
        }
        return count;
    }

    // ==== logging ====
    private Path writeLog() throws IOException {
        Files.createDirectories(LOG_DIR);
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Path logPath = LOG_DIR.resolve("task10_log_" + ts + ".txt");

        Duration runtime = Duration.between(startTime, Instant.now());

        try (BufferedWriter w = Files.newBufferedWriter(logPath)) {
            w.write("SwiftBot Task10 Log\n");
            w.write("===================\n");
            w.write("Mode selected: " + (selectedMode == null ? "UNKNOWN" : selectedMode.qrText) + "\n");
            w.write("Curious buffer (cm): " + curiousBufferCm + "\n");
            w.write("Execution start: " + startTime + "\n");
            w.write("Execution duration: " + formatDuration(runtime) + "\n");
            w.write("Encounters recorded (window list size at end): " + encounterTimes.size() + "\n");
            w.write("\nImage directory: " + IMAGE_DIR + "\n");
            w.write("Saved images:\n");
            for (Path p : savedImages) w.write("  - " + p + "\n");
        }
        return logPath;
    }

    private static String formatDuration(Duration d) {
        long s = d.getSeconds();
        long hh = s / 3600; s %= 3600;
        long mm = s / 60;   s %= 60;
        long ss = s;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    // ==== setup / utils ====
    private static void ensureDirs() throws IOException {
        Files.createDirectories(IMAGE_DIR);
        Files.createDirectories(DEBUG_DIR);
        Files.createDirectories(LOG_DIR);
    }

    private static void printWelcome() {
        System.out.println("====================================================");
        System.out.println(" Inc12 Final – SwiftBot Task 10 Detect Object (FASTER)");
        System.out.println("====================================================");
        System.out.println(" - Scan QR for mode");
        System.out.println(" - Wander blue (faster speeds)");
        System.out.println(" - Detect within 2m (ultrasound), and also ROI change (camera)");
        System.out.println(" - Press X to stop");
        System.out.println();
    }

    private static void sleep(int ms){
        try{ Thread.sleep(ms);} catch(InterruptedException ignored){}
    }
}