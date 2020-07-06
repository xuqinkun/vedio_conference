import com.github.sarxos.webcam.Webcam;
import org.junit.Test;
import util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class WebcamTest {

    @Test
    public void testImageSize() {
        Webcam webcam = Webcam.getDefault();
        int height = 480;
        webcam.setViewSize(new Dimension(640, height));
        webcam.open();
        Dimension viewSize = webcam.getViewSize();
        assertEquals(viewSize.getHeight(), height, 0);
        System.out.println(webcam.getImageBytes().limit());
        BufferedImage image = webcam.getImage();
        byte[] data = ImageUtil.imageToBytes(image);
        System.out.println(data.length);
    }
}
