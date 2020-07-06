import java.net.InetAddress;
import java.net.UnknownHostException;

public class DatagramTest {


    public static void main(String[] args) throws UnknownHostException {
        InetAddress address = InetAddress.getByName("192.168.0.105");
        System.out.println(address.getHostAddress());
        System.out.println(address.getHostName());
    }
}
