package ru.msu.cs.nosql.nosqlapp.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import ru.msu.cs.nosql.nosqlapp.User;

import java.util.List;

@Repository
public class UserRepository {
    public static final String COLLECTION_NAME = "User";
    private final MongoTemplate mongoTemplate;

    public UserRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public User findById(Long id) {
        return mongoTemplate.findById(id, User.class, COLLECTION_NAME);
    }

    public User save(User user) {
        return mongoTemplate.save(user, COLLECTION_NAME);
    }

    public List<User> findAll() {
        Query query = Query.query(new Criteria());
        return mongoTemplate.find(query, User.class, COLLECTION_NAME);
    }

    public void deleteUser(Long id) {
        mongoTemplate.remove(Query.query(new Criteria("_id").is(id)), User.class, COLLECTION_NAME);
    }

}
