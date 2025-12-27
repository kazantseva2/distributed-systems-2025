package ru.msu.cs.nosql.nosqlapp.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import ru.msu.cs.nosql.nosqlapp.model.ModerationStatus;
import ru.msu.cs.nosql.nosqlapp.model.Review;

import java.util.List;

@Repository
public class ReviewRepository {
    public static final String COLLECTION_NAME = "Review";
    private final MongoTemplate mongoTemplate;

    public ReviewRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Review findById(String id) {
        return mongoTemplate.findById(id, Review.class, COLLECTION_NAME);
    }

    public Review save(Review review) {
        return mongoTemplate.save(review, COLLECTION_NAME);
    }

    public List<Review> findAll() {
        Query query = Query.query(new Criteria());
        return mongoTemplate.find(query, Review.class, COLLECTION_NAME);
    }

    public void deleteReview(String id) {
        mongoTemplate.remove(Query.query(new Criteria("_id").is(id)), Review.class, COLLECTION_NAME);
    }

    public List<Review> findByProductId(String productId) {
        Query query = Query.query(Criteria.where("productId").is(productId));
        return mongoTemplate.find(query, Review.class, COLLECTION_NAME);
    }

    // Найти все одобренные отзывы для пересчёта рейтинга
    public List<Review> findApprovedByProduct(String productId) {
        Query query = Query.query(
                Criteria.where("productId").is(productId)
                        .and("moderationStatus").is(ModerationStatus.APPROVED)
        );
        return mongoTemplate.find(query, Review.class, COLLECTION_NAME);
    }

    // Обновить статус модерации
    public void updateModerationStatus(String reviewId, ModerationStatus status) {
        Query query = Query.query(Criteria.where("_id").is(reviewId));
        Update update = new Update().set("moderationStatus", status);
        mongoTemplate.updateFirst(query, update, Review.class, COLLECTION_NAME);
    }

    public void deleteAll() {
        mongoTemplate.dropCollection(COLLECTION_NAME); // удаляет коллекцию полностью
    }

}
