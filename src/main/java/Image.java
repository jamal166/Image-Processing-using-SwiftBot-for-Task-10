import swiftbot.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Inc03_SaveOnePhoto {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;
    private static final Path IMAGE_DIR = Paths.get("/data/home/pi/task10/images");
    private static final ImageSize SIZE = ImageSize.SQUARE_480x480;