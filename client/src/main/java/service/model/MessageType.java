package service.model;

public enum MessageType {
    INVALID((short)(-1)), TEXT((short)0), IMAGE((short)1), AUDIO((short)2),
    SENDER((short)3), RECEIVER((short)4);
    private short val;

    MessageType(short val) {
        this.val = val;
    }

    public short getVal() {
        return val;
    }

    public static MessageType valueOf(int val) {
        switch (val) {
            case 0:
                return TEXT;
            case 1:
                return IMAGE;
            case 2:
                return AUDIO;
            case 3:
                return SENDER;
            case 4:
                return RECEIVER;
            default:
                return INVALID;
        }
    }

    public static void main(String[] args) {
        System.out.println(valueOf(0));
        System.out.println(valueOf(1));
        System.out.println(valueOf(2));
    }
}
