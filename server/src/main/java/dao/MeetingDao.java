package dao;

import common.bean.Meeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import util.Helper;

import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class MeetingDao {

    public static final String PRIMARY_KEY = "uuid";

    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void insert(Meeting meeting) {
        mongoTemplate.insert(meeting);
    }

    public Meeting find(String uuid) {
        return mongoTemplate.findOne(new Query(where(PRIMARY_KEY).is(uuid)), Meeting.class);
    }

    public void endMeeting(String uuid) {
        Update update = new Update();
        update.set("ended", true);
        update.set("endTime", Helper.currentDate());
        mongoTemplate.updateFirst(new Query(where(PRIMARY_KEY).is(uuid)), update, Meeting.class);
    }

    public void updateHost(String meetingID, Meeting meeting) {
        Update update = new Update();
        update.set("host", meeting.getHost());
        update.set("managers", meeting.getManagers());
        mongoTemplate.updateFirst(new Query(where(PRIMARY_KEY).is(meetingID)), update, Meeting.class);
    }

    public void updateManagers(String meetingID, Set<String> managers) {
        Update update = new Update();
        update.set("managers", managers);
        mongoTemplate.updateFirst(new Query(where(PRIMARY_KEY).is(meetingID)), update, Meeting.class);
    }
}
