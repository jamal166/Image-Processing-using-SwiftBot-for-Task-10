import swiftbot.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Inc06_EncounterSaveImage {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;
    private static final Path IMAGE_DIR = Paths.get("/data/home/pi/task10/images");
    private static final ImageSize SIZE = ImageSize.SQUARE_480x480;

    private static final int WANDER_L = 18;
    private static final int WANDER_R = 20;

    public static void main(String[] args) {
        try {
            new Inc06_EncounterSaveImage().run();
        } catch (Exception e) {
            System.out.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void run() throws Exception {
        setupXButtonStop();
        Files.createDirectories(IMAGE_DIR);

        api.fillUnderlights(new int[]{0,0,255});
        System.out.println("[INC06] Wander. If object within 2m, save one image. X to stop.");

        boolean alreadyTriggered = false;

        while (!xPressed) {
            api.move(WANDER_L, WANDER_R, 250);

            double d = readDistanceAvg(2);
            boolean within2m = (d > 0 && d <= DETECT_WITHIN_CM);

            if (within2m && !alreadyTriggered) {
                alreadyTriggered = true;
                System.out.printf("[INC06] Encounter at %.1f cm. Capturing image...%n", d);
                saveImage("encounter");
            }

            if (!within2m) alreadyTriggered = false; // allow re-trigger when object leaves
        }

        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
        System.out.println("[INC06] Stopped.");
    }

    private void saveImage(String prefix) throws Exception {
        BufferedImage img = api.takeStill(SIZE);
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String name = prefix + "_" + ts + "_" + UUID.randomUUID().toString().substring(0,8) + ".jpg";
        Path out = IMAGE_DIR.resolve(name);
        ImageIO.write(img, "jpg", out.toFile());
        System.out.println("[INC06] Saved: " + out);
    }

    private double readDistanceAvg(int samples) {
        double sum = 0; int ok = 0;
        for (int i=0;i<samples;i++){
            try {
                double d = api.useUltrasound();
                if (d>0 && d<5000){ sum += d; ok++; }
            } catch(Exception ignored){}
            sleep(40);
        }
        return ok==0 ? -1 : sum/ok;
    }

    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}