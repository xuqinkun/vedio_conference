package dao;

import common.bean.Meeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

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

}
