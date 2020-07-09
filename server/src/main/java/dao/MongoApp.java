package dao;

import bean.User;
import com.mongodb.client.MongoClients;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class MongoApp {
    private static final Log log = LogFactory.getLog(MongoApp.class);

    public static void main(String[] args) throws Exception {

        MongoOperations mongoOps = new MongoTemplate(MongoClients.create("mongodb://localhost:27017"), "video_conference");
        mongoOps.insert(new User("Joe"));

        User user = mongoOps.findOne(new Query(where("name").is("Joe")), User.class);
        log.info(user);

//        mongoOps.dropCollection("user");
    }
}
