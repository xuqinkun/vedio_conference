package service;

import common.bean.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MeetingCache {
    private static final MeetingCache INSTANCE = new MeetingCache();

    private volatile Map<String, Map<String, User>> meetingUserMap = new ConcurrentHashMap<>();

    private MeetingCache() {
    }

    public static MeetingCache getInstance() {
        return INSTANCE;
    }

    public void addUser(String meetingId, User user) {
        if (meetingUserMap.get(meetingId) == null) {
            Map<String, User> userMap = new ConcurrentHashMap<>();
            meetingUserMap.put(meetingId, userMap);
        }
        Map<String, User> userMap = meetingUserMap.get(meetingId);
        if (!userMap.containsKey(user.getName())) {
            userMap.put(user.getName(), user);
        }
    }

    public User getUser(String meetingId, String username) {
        Map<String, User> userMap = meetingUserMap.get(meetingId);
        return userMap == null ? null : userMap.get(username);
    }

    public List<User> getUserList(String meetingId) {
        Map<String, User> stringUserMap = meetingUserMap.get(meetingId);
        if (stringUserMap != null) {
            Collection<User> users = stringUserMap.values();
            return new ArrayList<>(users);
        }
        return null;
    }

    public void removeUser(String meetingID, String name) {
        Map<String, User> userMap = meetingUserMap.get(meetingID);
        if (userMap != null) {
            userMap.remove(name);
        }
    }

    public void removeMeeting(String meetingID) {
        meetingUserMap.remove(meetingID);
    }
}
