package javacv;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


public class OpenCVImageViewer extends Application {

    public static String title;
    public static Mat img;

    @Override
    public void start(Stage primaryStage) throws Exception {

        //验证mat是不是为空
        if (img == null) {
            throw new NullPointerException("imshow img(Mat) is null");
        }
        //输出args获取的参数
        //log(this.getParameters().getRaw());
        if (title == null) {
            title = "ImageViewer";
        }

        //mat的宽度和高度
        int width = img.cols();
        int height = img.rows();

        //将mat的数据保存到BufferedImage对象中
        //注意mat的type(主要是通道数)和BufferedImage的type一致
        BufferedImage bf = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte []data = new byte[width*height*(int)img.elemSize()];
        img.get(0, 0, data);
        //以下方法设置颜色有问题,原图像显示偏蓝,不正常
        //bf.getRaster().setDataElements(0, 0, width, height, data);
        //此方式设置的颜色能正常显示没问题
        byte[] tartgetData = ((DataBufferByte)bf.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, tartgetData, 0, data.length);

        //将BufferedImage转换为JavaFX能显示的Image图像!
        Image image = SwingFXUtils.toFXImage(bf, null);
        ImageView iv = new ImageView(image);

        //采用流式布局(layout)
        FlowPane root = new FlowPane();
        root.getChildren().add(iv);

        Scene scene = new Scene(root,width,height);
        //给舞台设置场景
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();

    }

    private static <T> void log(T t) {
        System.out.println(t);
    }

}