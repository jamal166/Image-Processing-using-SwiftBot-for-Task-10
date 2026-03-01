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
    
    public static void main(String[] args) {
        try {
            new Inc03_SaveOnePhoto().run();
        } catch (Exception e) {
            System.out.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void run() throws Exception {
        setupXButtonStop();
        Files.createDirectories(IMAGE_DIR);

        System.out.println("[INC03] Taking one photo now... Press X to stop anytime.");
