package common.bean;

import util.BytesUtil;
import util.JsonUtil;
import util.NIOUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UserState {

    private String meetingId;

    private String username;

    private StateType state;

    public UserState() {
    }

    public UserState(String meetingId, String username, StateType state) {
        this.meetingId = meetingId;
        this.username = username;
        this.state = state;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public StateType getState() {
        return state;
    }

    public void setState(StateType state) {
        this.state = state;
    }

    public static UserState deserialize(final SocketChannel sc) throws IOException {
        ByteBuffer dataBuffer = NIOUtil.channelToBuffer(sc);
        if (dataBuffer == null) {
            return null;
        }
        byte[] data = new byte[dataBuffer.capacity()];
        dataBuffer.get(data);
        return JsonUtil.byteArrayToObject(data, UserState.class);
    }

    @Override
    public String toString() {
        return "UserState{" +
                "meetingId='" + meetingId + '\'' +
                ", username='" + username + '\'' +
                ", state=" + state +
                '}';
    }

    public ByteBuffer[] serialize() {
        byte[] data = JsonUtil.toByteArray(this);
        ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(NIOUtil.SIZE_BYTES_NUM);
        sizeBuffer.put(BytesUtil.int2Bytes(data.length));
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(data.length);
        dataBuffer.put(data);

        sizeBuffer.flip();
        dataBuffer.flip();

        return new ByteBuffer[]{sizeBuffer, dataBuffer};
    }
}
