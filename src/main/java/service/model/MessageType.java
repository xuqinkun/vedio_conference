package service.model;

public enum MessageType {
    TEXT((short)0), IMAGE((short)1), AUDIO((short)2), INVALID((short)(-1));
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
