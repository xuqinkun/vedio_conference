package dao;

import common.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class UserDao {

    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void insert(User user) {
        mongoTemplate.insert(user);
    }

    public User findOne(String name) {
        return mongoTemplate.findOne(new Query(where("name").is(name)), User.class);
    }

    public static void main(String[] args) {
    }
}
