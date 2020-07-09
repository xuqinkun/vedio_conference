package dao;

import bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class UserDao {
    @Autowired
    private MongoConfig config;

    @Autowired
    MongoTemplate mongoTemplate;


    public void insert(User user) {
        mongoTemplate.insert(user);
    }

    public User findOne(String name) {
        return mongoTemplate.findOne(new Query(where("name").is(name)), User.class);
    }

    public static void main(String[] args) {

    }
}
