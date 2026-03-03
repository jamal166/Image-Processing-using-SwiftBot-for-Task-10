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