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
    private Path saveImage(String prefix) throws Exception {
        BufferedImage img = api.takeStill(SIZE);
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String name = prefix + "_" + ts + "_" + UUID.randomUUID().toString().substring(0,8) + ".jpg";
        Path out = IMAGE_DIR.resolve(name);
        ImageIO.write(img, "jpg", out.toFile());
        System.out.println("[INC09] Saved: " + out);
        return out;
    }
    private static int r(int p) { return (p >> 16) & 0xFF; }
    private static int g(int p) { return (p >> 8) & 0xFF; }
    private static int b(int p) { return p & 0xFF; }

    private static BufferedImage thresholdToBW(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        long sum = 0, n = 0;

        // average brightness (sample)
        for (int y=0;y<h;y+=2){
            for (int x=0;x<w;x+=2){
                int p = img.getRGB(x,y);
                int br = (r(p)+g(p)+b(p))/3;
                sum += br; n++;
            }
        }
        int avg = n==0 ? 128 : (int)(sum/n);

        BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        int white = (255<<16)|(255<<8)|255;
        int black = 0;
        for (int y=0;y<h;y++){
            for (int x=0;x<w;x++){
                int p = img.getRGB(x,y);
                int br = (r(p)+g(p)+b(p))/3;
                out.setRGB(x,y, br>=avg ? white : black);
            }
        }
        return out;
    }int avg = n==0 ? 128 : (int)(sum/n);

    BufferedImage out = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
    int white = (255<<16)|(255<<8)|255;
    int black = 0;
    for (int y=0;y<h;y++){
        for (int x=0;x<w;x++){
            int p = img.getRGB(x,y);
            int br = (r(p)+g(p)+b(p))/3;
            out.setRGB(x,y, br>=avg ? white : black);
        }
    }
    return out;
}
private static int roiWhiteOccupancy(BufferedImage img) {
    BufferedImage bw = thresholdToBW(img);

    int w = bw.getWidth(), h = bw.getHeight();
    int x0 = (int)Math.round(w*ROI_MIN), x1 = (int)Math.round(w*ROI_MAX);
    int y0 = (int)Math.round(h*ROI_MIN), y1 = (int)Math.round(h*ROI_MAX);

    int white = (255<<16)|(255<<8)|255;
    int count = 0;

    for (int y=y0;y<y1;y+=2){
        for (int x=x0;x<x1;x+=2){
            int p = bw.getRGB(x,y) & 0xFFFFFF;
            if (p == (white & 0xFFFFFF)) count++;
        }
    }
    return count;
}
