import swiftbot.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Image
{
        public static void main(String args[]) throws Exception
        {
                SwiftBotAPI sb = swiftbot.SwiftBotAPI.INSTANCE;
                BufferedImage img = sb.takeGrayscaleStill (ImageSize.SQUARE_1080x1080);
                ImageIO.write(img, "jpg", new File("/data/home/pi/TestImage.jpg"));

                System.exit(1);
        }
}

