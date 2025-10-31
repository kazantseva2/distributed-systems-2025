package ru.msu.cs.nosql.nosqlapp.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import ru.msu.cs.nosql.nosqlapp.model.ModerationStatus;
import ru.msu.cs.nosql.nosqlapp.model.RatingOperator;
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

    // Найти отзывы по товару с фильтрацией
    public List<Review> findByFilters(String productId, Integer rating, RatingOperator ratingOp, Boolean hasText, String sortBy, int limit) {
        Criteria criteria = Criteria.where("productId").is(productId)
                .and("moderationStatus").is(ModerationStatus.APPROVED);

        if (rating != null && ratingOp != null) {
            switch (ratingOp) {
                case EQ:
                    criteria = criteria.and("rating").is(rating);
                    break;
                case GTE:
                    criteria = criteria.and("rating").gte(rating);
                    break;
                case LTE:
                    criteria = criteria.and("rating").lte(rating);
                    break;
            }
        }

        if (hasText != null && hasText) {
            criteria = criteria.and("text").ne(null).ne("");
        }

        Query query = new Query(criteria)
                .limit(limit)
                .with(Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "date"));

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
}
