package ru.msu.cs.nosql.nosqlapp.repository;


import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import ru.msu.cs.nosql.nosqlapp.model.Product;
import ru.msu.cs.nosql.nosqlapp.model.RatingOperator;

import java.util.List;

@Repository
public class ProductRepository {
    public static final String COLLECTION_NAME = "Product";
    private final MongoTemplate mongoTemplate;

    public ProductRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Product findById(String id) {
        return mongoTemplate.findById(id, Product.class, COLLECTION_NAME);
    }

    public Product save(Product product) {
        return mongoTemplate.save(product, COLLECTION_NAME);
    }

    public List<Product> findAll() {
        Query query = Query.query(new Criteria());
        return mongoTemplate.find(query, Product.class, COLLECTION_NAME);
    }

    public void deleteProduct(String id) {
        mongoTemplate.remove(Query.query(new Criteria("_id").is(id)), Product.class, COLLECTION_NAME);
    }

    // Обновить агрегированные данные
    public void updateAggregatedRating(String productId, double avgRating, int countReviews) {
        Query query = Query.query(Criteria.where("_id").is(productId));
        Update update = new Update()
                .set("aggregatedRating", avgRating)
                .set("countReviews", countReviews);
        mongoTemplate.updateFirst(query, update, Product.class, COLLECTION_NAME);
    }

    public void deleteAll() {
        mongoTemplate.dropCollection(COLLECTION_NAME);
    }

}
