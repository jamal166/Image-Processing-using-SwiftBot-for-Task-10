import swiftbot.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
public class Inc07_ScaredyOnly {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int SCAREDY_WITHIN_CM = 50;
    private static final Path IMAGE_DIR = Paths.get("/data/home/pi/task10/images");
    private static final ImageSize SIZE = ImageSize.SQUARE_480x480;

    private static final int WANDER_L = 18;
    private static final int WANDER_R = 20;

    public static void main(String[] args) {
        try { new Inc07_ScaredyOnly().run(); }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void run() throws Exception {
        setupXButtonStop();
        Files.createDirectories(IMAGE_DIR);

        setBlue();
        System.out.println("[INC07] Scaredy only. If object <=50cm: photo + blink red + flee. X to stop.");

        while (!xPressed) {
            api.move(WANDER_L, WANDER_R, 200);

            double d = readDistanceAvg(2);
            if (d > 0 && d <= SCAREDY_WITHIN_CM) {
                System.out.printf("[INC07] Trigger! distance=%.1f cm%n", d);
                scaredyAction();
            }
        }
        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
        System.out.println("[INC07] Stopped.");
    }
    private void scaredyAction() throws Exception {
        // Save image
        saveImage("scaredy");

        // Blink red
        for (int i=0;i<3 && !xPressed;i++){
            setRed(); sleep(150);
            api.disableUnderlights(); sleep(150);
        }

        if (xPressed) return;

        // Flee red
        setRed();
        api.move(-35, -35, 600);  // back up
        api.move(40, -40, 450);   // turn away
        api.move(35, 35, 3000);   // move away 3 seconds
        api.stopMove();

        setBlue();
    }
    private void saveImage(String prefix) throws Exception {
        BufferedImage img = api.takeStill(SIZE);
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String name = prefix + "_" + ts + "_" + UUID.randomUUID().toString().substring(0,8) + ".jpg";
        Path out = IMAGE_DIR.resolve(name);
        ImageIO.write(img, "jpg", out.toFile());
        System.out.println("[INC07] Saved: " + out);
    }