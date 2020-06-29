package service.schedule;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.bytedeco.javacv.FrameGrabber;
import service.model.Message;
import service.network.MessageReceiver;
import service.network.TcpClient;

public class VideoReceiverService extends ScheduledService<Image> {
    private ImageGrabber grabber;
    private boolean started;
    private TcpClient client;
    long lastReceive = System.currentTimeMillis();

    public VideoReceiverService(String output) throws FrameGrabber.Exception {
        this.grabber = new ImageGrabber(output);
        started = false;
    }

    public VideoReceiverService(int port) {
        client = new MessageReceiver(port);
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() {
                if (!started) {
                    new Thread(client).start();
                    started = true;
                }
                Message msg;
                try {
                    msg = client.getNext();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                System.out.printf("Receive image: %dms \n", (System.currentTimeMillis() - lastReceive));
                lastReceive = System.currentTimeMillis();
                return msg.toImage();
            }
        };
    }

}
