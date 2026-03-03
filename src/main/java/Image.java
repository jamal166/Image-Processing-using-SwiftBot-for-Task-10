import swiftbot.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID
public class Inc09_CuriousWithImageMovedCheck {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;
    private static final int BUFFER_CM = 30;
    private static final int TOLERANCE_CM = 2;

    private static final Path IMAGE_DIR = Paths.get("/data/home/pi/task10/images");
    private static final ImageSize SIZE = ImageSize.SQUARE_480x480;

    // ROI + thresholds
    private static final double ROI_MIN = 0.35, ROI_MAX = 0.65;
    private static final int OCCUPANCY_DELTA_THRESHOLD = 150;

    public static void main(String[] args) {
        try { new Inc09_CuriousWithImageMovedCheck().run(); }
        catch (Exception e) { e.printStackTrace(); }
    }
    private void run() throws Exception {
        setupXButtonStop();
        Files.createDirectories(IMAGE_DIR);

        setBlue();
        System.out.println("[INC09] Curious + moved check. Object<=2m -> buffer 30cm -> save -> wait5s -> compare ROI occupancy. X to stop.");

        while (!xPressed) {
            api.move(18, 20, 250);

            double d = readDistanceAvg(2);
            if (d > 0 && d <= DETECT_WITHIN_CM) {
                setGreen();
                api.stopMove();

                // Move to buffer
                reachBuffer();

                // Save reference image
                Path refPath = saveImage("curious_ref");
                BufferedImage refImg = ImageIO.read(refPath.toFile());

                // Wait 5s
                sleep(5000);

                // Capture later image
                BufferedImage later = api.takeStill(SIZE);

                int occRef = roiWhiteOccupancy(refImg);
                int occLater = roiWhiteOccupancy(later);
                int delta = occLater - occRef;

                System.out.println("[INC09] ROI occupancy delta = " + delta);

                if (Math.abs(delta) < OCCUPANCY_DELTA_THRESHOLD) {
                    // Not moved: wait 1s and turn slightly then resume
                    api.stopMove();
                    sleep(1000);
                    slightTurn();
                } else if (delta > 0) {
                    // appears closer -> move back slightly
                    api.move(-25, -25, 250);
                    api.stopMove();
                } else {
                    // appears farther -> move forward slightly
                    api.move(25, 25, 250);
                    api.stopMove();
                }

                api.disableUnderlights();
                setBlue();
            }
        }
        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
    }
    private void reachBuffer() {
        while (!xPressed) {
            double d = readDistanceAvg(2);
            if (d <= 0) break;

            if (d > BUFFER_CM + TOLERANCE_CM) {
                api.startMove(25, 25);
                sleep(120);
            } else if (d < BUFFER_CM - TOLERANCE_CM) {
                api.startMove(-25, -25);
                sleep(120);
            } else {
                api.stopMove();
                blinkGreen();
                break;
            }
        }
        api.stopMove();
    }
    private void blinkGreen() {
        for (int i=0;i<3 && !xPressed;i++){
            setGreen(); sleep(200);
            api.disableUnderlights(); sleep(200);
        }
        setGreen();
    }

    private void slightTurn() {
        int turn = Math.random() < 0.5 ? 1 : -1;
        api.move(20*turn, -20*turn, 250);
        api.stopMove();
    }