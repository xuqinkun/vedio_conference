package util;

import java.util.Arrays;

public class BytesUtil {
    public static int bytes2Int(byte[] bytes) {
        int result = 0;
        result = bytes[0] & 0xff;
        result = result << 8 | bytes[1] & 0xff;
        result = result << 8 | bytes[2] & 0xff;
        result = result << 8 | bytes[3] & 0xff;
        return result;
    }

    public static byte[] int2Bytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(num >>> 24);
        bytes[1] = (byte)(num >>> 16);
        bytes[2] = (byte)(num >>> 8);
        bytes[3] = (byte)num;
        return bytes;
    }

    public static void main(String[] args) {
        byte[] data = int2Bytes(Integer.MAX_VALUE);
        System.out.println(Arrays.toString(data));
        System.out.println(bytes2Int(data));
    }
}