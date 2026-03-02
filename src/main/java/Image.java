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