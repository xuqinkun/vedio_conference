package dao;

import common.bean.Meeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import util.Helper;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class MeetingDao {

    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void insert(Meeting meeting) {
        mongoTemplate.insert(meeting);
    }

    public Meeting find(String uuid) {
        return mongoTemplate.findOne(new Query(where("uuid").is(uuid)), Meeting.class);
    }

    public void endMeeting(String uuid) {
        Update update = new Update();
        update.set("ended", true);
        update.set("endTime", Helper.currentDate());
        mongoTemplate.updateFirst(new Query(where("uuid").is(uuid)), update, Meeting.class);
    }
}
