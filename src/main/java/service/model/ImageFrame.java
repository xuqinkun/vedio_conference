package service.model;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

public class ImageFrame implements Serializable {
    private int size;
    private byte [] data;

    public ImageFrame(int size, byte[] data) {
        this.size = size;
        this.data = data;
    }

    public BufferedImage toBufferedImage() {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try {
            return ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image toImage() {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            return new Image(bais);
        } catch (IOException ex) {
            return null;
        }
    }
}
