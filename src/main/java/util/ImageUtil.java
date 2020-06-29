package util;

import javafx.scene.image.Image;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ImageUtil {
    private static JavaFXFrameConverter converter = new JavaFXFrameConverter();

    public static Image convert(Frame frame) {
        return converter.convert(frame);
    }

    public static Frame convert(Image image) {
        return converter.convert(image);
    }

    public static byte[] imageToBytes(BufferedImage img) {
        if (img == null) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", bos);
            return bos.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }
}
